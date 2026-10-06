package io.github.forgestove.create_cyber_goggles.compat;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.redstoneRequester.*;
import com.simibubi.create.foundation.gui.menu.GhostItemSubmitPacket;
import io.github.forgestove.create_cyber_goggles.compat.jei.ScreenReferenced;
import io.github.forgestove.create_cyber_goggles.core.event.CCGKey;
import io.github.forgestove.create_cyber_goggles.mixin.accessor.RedstoneRequesterScreenAccessor;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

import java.util.*;
/**
 * 红石请求器填充逻辑，JEI 与 EMI 侧共用。
 */
public final class RedstoneRequesterFill {
	private RedstoneRequesterFill() {}
	/** 从原版配方取每格代表物品，空位为 {@link ItemStack#EMPTY}，有原料但无候选项为 {@code null} */
	public static List<ItemStack> representatives(Recipe<?> recipe) {
		List<ItemStack> reps = new ArrayList<>();
		for (var ingredient : recipe.getIngredients()) {
			if (ingredient.isEmpty()) {
				reps.add(ItemStack.EMPTY);
				continue;
			}
			var matches = ingredient.getItems();
			reps.add(matches.length == 0 ? null : matches[0]);
		}
		return reps;
	}
	/** 每格独立成组 */
	public static List<BigItemStack> vanillaStyleGroups(List<ItemStack> reps, boolean maxTransfer) {
		var count = maxTransfer ? 64 : 1;
		List<BigItemStack> groups = new ArrayList<>();
		for (var rep : reps) {
			if (rep == null || rep.isEmpty()) {
				groups.add(null);
				continue;
			}
			groups.add(new BigItemStack(rep.copyWithCount(1), count));
		}
		return groups;
	}
	/** 相邻同类原料合并成组；空位跳过不打断，无候选项断开当前组。组的数量=连续出现次数，按 Shift 时再乘一整组 */
	public static List<BigItemStack> mechanicalStyleGroups(List<ItemStack> reps, boolean maxTransfer) {
		var step = maxTransfer ? 64 : 1;
		List<BigItemStack> groups = new ArrayList<>();
		BigItemStack currentGroup = null;
		for (var rep : reps) {
			if (rep != null && rep.isEmpty()) continue;
			if (rep == null) {
				currentGroup = null;
				continue;
			}
			if (currentGroup == null || !ItemStack.isSameItemSameComponents(currentGroup.stack, rep)) {
				currentGroup = new BigItemStack(rep.copyWithCount(1), step);
				groups.add(currentGroup);
			} else currentGroup.count += step;
		}
		return groups;
	}
	public static int slotCount(RedstoneRequesterMenu container) {
		return container.ghostInventory.getSlots();
	}
	/** 每行一条；每次调用都按当前按键状态重新求值 */
	public static List<Component> hints() {
		return List.of(
			CCGKey.hint(Component.translatable(
				"create_cyber_goggles.gui.redstoneRequester.shiftHint",
				CCGKey.keyName("Shift", Screen.hasShiftDown())
			)),
			CCGKey.hint(Component.translatable(
				"create_cyber_goggles.gui.redstoneRequester.altHint",
				CCGKey.keyName("Alt", Screen.hasAltDown())
			))
		);
	}
	public static void applyGroups(RedstoneRequesterMenu container, List<BigItemStack> groups) {
		var slots = container.ghostInventory.getSlots();
		for (var i = 0; i < slots; i++) {
			var group = i < groups.size() ? groups.get(i) : null;
			var stack = group != null ? group.stack.copyWithCount(1) : ItemStack.EMPTY;
			container.ghostInventory.setStackInSlot(i, stack);
			CatnipServices.NETWORK.sendToServer(new GhostItemSubmitPacket(stack, i));
		}
		if (container instanceof ScreenReferenced referenced
			&& referenced.ccg$getScreenReference() instanceof RedstoneRequesterScreen screen) {
			var amounts = ((RedstoneRequesterScreenAccessor) screen).getAmounts();
			for (var i = 0; i < amounts.size(); i++)
				amounts.set(i, i < groups.size() && groups.get(i) != null ? groups.get(i).count : 1);
		}
	}
}
