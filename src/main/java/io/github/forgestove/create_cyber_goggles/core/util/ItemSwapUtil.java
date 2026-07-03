package io.github.forgestove.create_cyber_goggles.core.util;
import com.simibubi.create.AllItems;
import dev.simulated_team.simulated.index.SimItems;
import io.github.forgestove.create_cyber_goggles.core.event.CCGKey;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
public class ItemSwapUtil {
	private static final EnumMap<CCGKey, Boolean> wasDown = new EnumMap<>(CCGKey.class);
	/** -2 = 快捷栏选择, -1 = 创造模式销毁, >=0 = 背包交换原点 */
	private static final int HOTBAR_SELECT = -2;
	private static final int LOCAL_SPAWN = -3;
	private static boolean isSwapped;
	private static int swappedOriginSlot = -1;
	private static int swappedHandSlot = -1;
	private static ItemStack preSwapMainHand = ItemStack.EMPTY;
	public static void tick() {
		var player = mc.player;
		if (player == null) return;
		var inventory = player.getInventory();
		CCGKey pressedKey = null;
		var anyHotkeyHeld = false;
		for (var entry : getHotkeyItems().entrySet()) {
			var key = entry.getKey();
			var isDown = key.isDown();
			if (isDown) anyHotkeyHeld = true;
			var prevDown = wasDown.getOrDefault(key, false);
			wasDown.put(key, isDown);
			if (isDown && !prevDown) pressedKey = key;
		}
		if (pressedKey != null) {
			if (isSwapped) releaseSwap(inventory);
			pressSwap(getHotkeyItems().get(pressedKey), player.isCreative(), inventory);
		} else if (isSwapped && !anyHotkeyHeld) releaseSwap(inventory);
	}
	private static Map<CCGKey, ItemStack> getHotkeyItems() {
		var items = getDefaultItems();
		var result = new LinkedHashMap<CCGKey, ItemStack>();
		for (var entry : items.entrySet()) {
			var key = entry.getKey();
			var stack = entry.getValue();
			if (key != null && !stack.isEmpty()) result.put(key, stack);
		}
		return result;
	}
	private static void releaseSwap(Inventory inventory) {
		if (swappedOriginSlot == HOTBAR_SELECT) {
			// 快捷栏选择 —— 通过数据包恢复（完全同步）
			inventory.selected = swappedHandSlot;
			if (mc.player != null) mc.player.connection.send(new ServerboundSetCarriedItemPacket(swappedHandSlot));
		} else if (swappedOriginSlot >= 0) {
			// 背包交换 —— 通过容器点击数据包换回（完全同步）
			var player = mc.player;
			if (player != null) {
				var container = player.containerMenu;
				var currentMainHand = inventory.getItem(swappedHandSlot);
				var changedSlots = new Int2ObjectOpenHashMap<ItemStack>();
				changedSlots.put(swappedOriginSlot, currentMainHand);
				changedSlots.put(36 + swappedHandSlot, preSwapMainHand);
				player.connection.send(new ServerboundContainerClickPacket(
					container.containerId,
					container.getStateId(),
					swappedOriginSlot,
					swappedHandSlot,
					ClickType.SWAP,
					ItemStack.EMPTY,
					changedSlots
				));
			}
			var currentMainHand = inventory.getItem(swappedHandSlot);
			inventory.setItem(swappedHandSlot, preSwapMainHand);
			inventory.setItem(swappedOriginSlot, currentMainHand);
		} else // 本地生成 —— 恢复原始物品（仅客户端）
			if (swappedOriginSlot == LOCAL_SPAWN) inventory.setItem(swappedHandSlot, preSwapMainHand);
			else if (mc.gameMode != null)
				mc.gameMode.handleCreativeModeItemAdd(preSwapMainHand, 36 + swappedHandSlot); // 创造模式销毁 —— 通过数据包恢复
		isSwapped = false;
		swappedOriginSlot = -1;
		swappedHandSlot = -1;
		preSwapMainHand = ItemStack.EMPTY;
	}
	private static void pressSwap(ItemStack target, boolean isCreative, Inventory inventory) {
		var handSlot = inventory.selected;
		var current = inventory.getItem(handSlot);
		if (ItemStack.isSameItemSameComponents(current, target)) return;
		var offhand = inventory.offhand.getFirst();
		if (ItemStack.isSameItemSameComponents(offhand, target)) return;
		preSwapMainHand = current;
		swappedHandSlot = handSlot;
		// 1) 快捷栏 —— 通过数据包选择（完全同步，适用于所有模式）
		for (var i = 0; i < 9; i++) {
			if (i == handSlot) continue;
			if (!ItemStack.isSameItemSameComponents(inventory.getItem(i), target)) continue;
			if (mc.player != null) mc.player.connection.send(new ServerboundSetCarriedItemPacket(i));
			inventory.selected = i;
			swappedOriginSlot = HOTBAR_SELECT;
			isSwapped = true;
			return;
		}
		// 2) 主背包 —— 通过容器点击数据包交换（完全同步）
		for (var i = 9; i < inventory.items.size(); i++) {
			if (!ItemStack.isSameItemSameComponents(inventory.getItem(i), target)) continue;
			var player = mc.player;
			if (player != null) {
				var container = player.containerMenu;
				var targetItem = inventory.getItem(i);
				var changedSlots = new Int2ObjectOpenHashMap<ItemStack>();
				changedSlots.put(i, preSwapMainHand);
				changedSlots.put(36 + handSlot, targetItem);
				player.connection.send(new ServerboundContainerClickPacket(
					container.containerId,
					container.getStateId(),
					i,
					handSlot,
					ClickType.SWAP,
					ItemStack.EMPTY,
					changedSlots
				));
			}
			inventory.setItem(handSlot, inventory.getItem(i));
			inventory.setItem(i, preSwapMainHand);
			swappedOriginSlot = i;
			isSwapped = true;
			return;
		}
		// 3) 背包中未找到 —— 回退处理
		// 创造模式：所有物品通过数据包；生存模式：非法杖物品本地生成（释放时消失）
		if (isCreative && mc.gameMode != null) {
			mc.gameMode.handleCreativeModeItemAdd(target, 36 + handSlot);
			isSwapped = true;
		} else if (!(CCGMods.SIMULATED.isLoaded() && target.is(SimItems.PHYSICS_STAFF.get()))) {
			inventory.setItem(handSlot, target.copy());
			swappedOriginSlot = LOCAL_SPAWN;
			isSwapped = true;
		}
	}
	private static LinkedHashMap<CCGKey, ItemStack> getDefaultItems() {
		var items = new LinkedHashMap<CCGKey, ItemStack>();
		items.put(CCGKey.useSchematic, AllItems.SCHEMATIC_AND_QUILL.asStack());
		items.put(CCGKey.showSuperGlue, AllItems.SUPER_GLUE.asStack());
		CCGMods.SIMULATED.executeIfInstalled(() -> {
			items.put(CCGKey.usePhysicsStaff, SimItems.PHYSICS_STAFF.asStack());
			items.put(CCGKey.showHoneyGlue, SimItems.HONEY_GLUE.asStack());
		});
		return items;
	}
}
