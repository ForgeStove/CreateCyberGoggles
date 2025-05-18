package com.forgestove.create_cyber_goggles.content.event;
import com.forgestove.create_cyber_goggles.CreateCyberGoggles;
import com.forgestove.create_cyber_goggles.content.config.*;
import com.forgestove.create_cyber_goggles.content.util.SafeRun;
import com.simibubi.create.content.logistics.filter.*;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.gui.ScreenOpener;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;

import java.util.Collections;
public class KeyInput {
	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			toggleDiving(mc);
			openConfigScreen(mc);
			previewFilterScreen(mc);
		});
	}
	public static void toggleDiving(Minecraft mc) {
		if (!CCGKeyMapping.toggleDiving.consumeClick()) return;
		var player = mc.player;
		if (player == null || mc.screen != null) return;
		var enabled = CCGConfig.config.armor.removeDivingBootsAffect;
		CCGConfig.config.armor.removeDivingBootsAffect = !enabled;
		player.displayClientMessage(
			Component.translatable("message.%s.%sableDivingAffect".formatted(
				CreateCyberGoggles.ID,
				enabled ? "en" : "dis"
			)), true
		);
	}
	public static void openConfigScreen(Minecraft mc) {
		if (!CCGKeyMapping.openConfig.consumeClick()) return;
		if (mc.screen != null) return;
		mc.setScreen(AutoConfig.getConfigScreen(CCGConfigData.class, null).get());
	}
	public static void previewFilterScreen(Minecraft mc) {
		if (!CCGKeyMapping.previewFilter.isDown()) return;
		if (mc.screen != null) {
			if (!(mc.screen instanceof AbstractContainerScreen<?> screen)) return;
			var slot = screen.hoveredSlot;
			if (slot == null) return;
			openFilterScreen(slot.getItem());
		} else {
			if (mc.level == null || !(mc.hitResult instanceof BlockHitResult blockHitResult)) return;
			if (blockHitResult.getType() == Type.MISS) return;
			var be = mc.level.getBlockEntity(blockHitResult.getBlockPos());
			if (!(be instanceof SmartBlockEntity sbe)) return;
			var behavior = Collections.singleton(sbe.getBehaviour(FilteringBehaviour.TYPE));
			var first = behavior.iterator().next();
			if (!(first instanceof FilteringBehaviour)) return;
			openFilterScreen(first.getFilter(blockHitResult.getDirection()));
		}
	}
	public static void openFilterScreen(ItemStack filter) {
		SafeRun.run(() -> {
			if (!(filter.getItem() instanceof FilterItem filterItem)) return;
			var mc = Minecraft.getInstance();
			if (mc.player == null) return;
			var inv = mc.player.getInventory();
			var name = filter.getHoverName();
			var field = FilterItem.class.getDeclaredField("type");
			field.setAccessible(true);
			var ordinal = ((Enum<?>) field.get(filterItem)).ordinal();
			ScreenOpener.open(switch (ordinal) {
				case 0 -> new FilterScreen(FilterMenu.create(-1, inv, filter), inv, name);
				case 1 -> new AttributeFilterScreen(AttributeFilterMenu.create(-1, inv, filter), inv, name);
				default -> throw new IllegalStateException("Unexpected value: " + ordinal);
			});
		});
	}
}
