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
	private static final Map<CCGKey, ItemStack> HOTKEY_ITEMS = new LinkedHashMap<>();
	private static final EnumMap<CCGKey, Boolean> wasDown = new EnumMap<>(CCGKey.class);
	/** -2 = hotbar select, -1 = creative void, >=0 = inventory swap origin */
	private static final int HOTBAR_SELECT = -2;
	private static boolean isSwapped;
	private static int swappedOriginSlot = -1;
	private static int swappedHandSlot = -1;
	private static ItemStack preSwapMainHand = ItemStack.EMPTY;
	static {
		HOTKEY_ITEMS.put(CCGKey.useSchematic, AllItems.SCHEMATIC_AND_QUILL.asStack());
		HOTKEY_ITEMS.put(CCGKey.showSuperGlue, AllItems.SUPER_GLUE.asStack());
		if (CCGMods.SIMULATED.isLoaded()) {
			HOTKEY_ITEMS.put(CCGKey.usePhysicsStaff, SimItems.PHYSICS_STAFF.asStack());
			HOTKEY_ITEMS.put(CCGKey.showHoneyGlue, SimItems.HONEY_GLUE.asStack());
		}
	}
	public static void tick() {
		var player = mc.player;
		if (player == null) return;
		var inventory = player.getInventory();
		CCGKey pressedKey = null;
		var anyHotkeyHeld = false;
		for (var entry : HOTKEY_ITEMS.entrySet()) {
			var key = entry.getKey();
			var isDown = key.isDown();
			if (isDown) anyHotkeyHeld = true;
			var prevDown = wasDown.getOrDefault(key, false);
			wasDown.put(key, isDown);
			if (isDown && !prevDown) pressedKey = key;
		}
		if (pressedKey != null) {
			if (isSwapped) releaseSwap(inventory);
			pressSwap(HOTKEY_ITEMS.get(pressedKey), player.isCreative(), inventory);
		} else if (isSwapped && !anyHotkeyHeld) releaseSwap(inventory);
	}
	private static void pressSwap(ItemStack target, boolean isCreative, Inventory inventory) {
		var handSlot = inventory.selected;
		var currentMainHand = inventory.getItem(handSlot);
		if (ItemStack.isSameItemSameComponents(currentMainHand, target)) return;
		preSwapMainHand = currentMainHand;
		swappedHandSlot = handSlot;
		// 1) Hotbar — select via packet (fully synced, works for all modes)
		for (var i = 0; i < 9; i++) {
			if (i == handSlot) continue;
			if (!ItemStack.isSameItemSameComponents(inventory.getItem(i), target)) continue;
			if (mc.player != null) mc.player.connection.send(new ServerboundSetCarriedItemPacket(i));
			inventory.selected = i;
			swappedOriginSlot = HOTBAR_SELECT;
			isSwapped = true;
			return;
		}
		// 2) Main inventory — swap via container click packet (fully synced)
		for (var i = 9; i < inventory.items.size(); i++) {
			if (!ItemStack.isSameItemSameComponents(inventory.getItem(i), target)) continue;
			var player = mc.player;
			if (player != null) {
				var container = player.containerMenu;
				var targetItem = inventory.getItem(i);
				var changedSlots = new Int2ObjectOpenHashMap<ItemStack>();
				changedSlots.put(i, preSwapMainHand);                // 物品栏格 ← 主手物品
				changedSlots.put(36 + handSlot, targetItem);            // 快捷栏格 ← 目标物品
				player.connection.send(new ServerboundContainerClickPacket(
					container.containerId, container.getStateId(), i,                    // slotNum: 物品栏容器格索引
					handSlot,                // buttonNum: 快捷栏索引 (0-8)
					ClickType.SWAP, ItemStack.EMPTY, changedSlots
				));
			}
			inventory.setItem(handSlot, inventory.getItem(i));
			inventory.setItem(i, preSwapMainHand);
			swappedOriginSlot = i;
			isSwapped = true;
			return;
		}
		// 3) Not in inventory — creative mode gets via packet (container slot 36 + hotbarIndex)
		if (isCreative && mc.gameMode != null) {
			mc.gameMode.handleCreativeModeItemAdd(target, 36 + handSlot);
			isSwapped = true;
		}
	}
	private static void releaseSwap(Inventory inventory) {
		if (swappedOriginSlot == HOTBAR_SELECT) {
			// Hotbar select — restore via packet (fully synced)
			inventory.selected = swappedHandSlot;
			if (mc.player != null) mc.player.connection.send(new ServerboundSetCarriedItemPacket(swappedHandSlot));
		} else if (swappedOriginSlot >= 0) {
			// Inventory swap — swap back via container click packet (fully synced)
			var player = mc.player;
			if (player != null) {
				var container = player.containerMenu;
				var currentMainHand = inventory.getItem(swappedHandSlot);
				var changedSlots = new Int2ObjectOpenHashMap<ItemStack>();
				changedSlots.put(swappedOriginSlot, currentMainHand);        // 物品栏格 ← 当前主手物品
				changedSlots.put(36 + swappedHandSlot, preSwapMainHand);    // 快捷栏格 ← 初始主手物品
				player.connection.send(new ServerboundContainerClickPacket(
					container.containerId, container.getStateId(), swappedOriginSlot,        // slotNum: 物品栏容器格索引
					swappedHandSlot,        // buttonNum: 快捷栏索引 (0-8)
					ClickType.SWAP, ItemStack.EMPTY, changedSlots
				));
			}
			var currentMainHand = inventory.getItem(swappedHandSlot);
			inventory.setItem(swappedHandSlot, preSwapMainHand);
			inventory.setItem(swappedOriginSlot, currentMainHand);
		} else if (mc.gameMode != null)
			mc.gameMode.handleCreativeModeItemAdd(preSwapMainHand, 36 + swappedHandSlot); // Creative void — restore via packet
		isSwapped = false;
		swappedOriginSlot = -1;
		swappedHandSlot = -1;
		preSwapMainHand = ItemStack.EMPTY;
	}
}
