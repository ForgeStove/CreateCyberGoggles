package com.forgestove.create_cyber_goggles.content.event;
import com.forgestove.create_cyber_goggles.CreateCyberGoggles;
import com.forgestove.create_cyber_goggles.content.config.CCGConfig;
import com.forgestove.create_cyber_goggles.content.util.SafeRun;
import com.simibubi.create.content.logistics.filter.*;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.gui.ScreenOpener;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;

import java.util.Collections;
public class KeyInput {
	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			KeyInput.toggleDiving();
			KeyInput.openConfigScreen();
			KeyInput.previewFilterScreen();
		});
	}
	public static void toggleDiving() {
		if (!CCGKeyMapping.toggleDiving.consumeClick()) return;
		var mc = Minecraft.getInstance();
		var player = mc.player;
		if (player == null || mc.screen != null) return;
		var enabled = CreateCyberGoggles.config.armor.removeDivingBootsAffect;
		CreateCyberGoggles.config.armor.removeDivingBootsAffect = !enabled;
		var component = Component.translatable("message.%s.%sableDivingAffect".formatted(CreateCyberGoggles.ID, enabled ? "en" : "dis"));
		player.displayClientMessage(component, true);
	}
	public static void openConfigScreen() {
		if (!CCGKeyMapping.openConfig.consumeClick()) return;
		var mc = Minecraft.getInstance();
		if (mc.screen != null) return;
		mc.setScreen(AutoConfig.getConfigScreen(CCGConfig.class, null).get());
	}
	public static void previewFilterScreen() {
		if (!CCGKeyMapping.previewFilter.consumeClick()) return;
		var mc = Minecraft.getInstance();
		if (mc.screen != null) {
			if (!(mc.screen instanceof AbstractContainerScreen<?> screen)) return;
			var slot = screen.hoveredSlot;
			if (slot == null) return;
			setFilterScreen(slot.getItem());
		} else {
			if (mc.level == null || !(mc.hitResult instanceof BlockHitResult blockHitResult)) return;
			if (blockHitResult.getType() == Type.MISS) return;
			var blockEntity = mc.level.getBlockEntity(blockHitResult.getBlockPos());
			if (!(blockEntity instanceof SmartBlockEntity smartBlockEntity)) return;
			var behavior = Collections.singleton(smartBlockEntity.getBehaviour(FilteringBehaviour.TYPE));
			var first = behavior.iterator().next();
			if (!(first instanceof FilteringBehaviour)) return;
			setFilterScreen(first.getFilter(blockHitResult.getDirection()));
		}
	}
	public static void setFilterScreen(ItemStack filter) {
		SafeRun.run(() -> {
			if (!(filter.getItem() instanceof FilterItem filterItem)) return;
			var mc = Minecraft.getInstance();
			var player = mc.player;
			if (player == null) return;
			var inv = player.getInventory();
			var name = filter.getHoverName();
			Screen screen;
			var field = FilterItem.class.getDeclaredField("type");
			field.setAccessible(true);
			switch (((Enum<?>) field.get(filterItem)).ordinal()) {
				case 0 -> screen = new FilterScreen(FilterMenu.create(-1, inv, filter), inv, name);
				case 1 -> screen = new AttributeFilterScreen(AttributeFilterMenu.create(-1, inv, filter), inv, name);
				default -> {
					return;
				}
			}
			ScreenOpener.open(screen);
		});
	}
}
