package com.forgestove.create_cyber_goggles.content.event;
import com.forgestove.create_cyber_goggles.*;
import com.simibubi.create.content.logistics.filter.*;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.InputEvent.Key;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.Collections;
public class KeyInput {
	public static void openConfigScreen(Key event) {
		if (!KeyBind.isAction(event, KeyBind.OPEN_CONFIG, GLFW.GLFW_PRESS)) return;
		var mc = Minecraft.getInstance();
		if (mc.screen != null) return;
		var modContainerById = ModList.get().getModContainerById(CreateCyberGoggles.ID);
		if (modContainerById.isEmpty()) return;
		var modContainer = modContainerById.get();
		mc.setScreen(new ConfigurationScreen(modContainer, mc.screen));
	}
	public static void openFilterScreen(Key event) {
		if (!Config.enableOpenFilterScreen.get()) return;
		if (!KeyBind.isAction(event, KeyBind.PREVIEW_FILTER, GLFW.GLFW_PRESS)) return;
		var mc = Minecraft.getInstance();
		if (mc.screen != null) {
			if (!(mc.screen instanceof AbstractContainerScreen<?> screen)) return;
			var slot = screen.getSlotUnderMouse();
			if (slot == null) return;
			var item = slot.getItem();
			if (!(item.getItem() instanceof FilterItem)) return;
			setFilterScreen(item);
		} else {
			if (mc.level == null || !(mc.hitResult instanceof BlockHitResult blockHitResult)) return;
			if (blockHitResult.getType() == Type.MISS) return;
			var blockEntity = mc.level.getBlockEntity(blockHitResult.getBlockPos());
			if (!(blockEntity instanceof SmartBlockEntity smartBlockEntity)) return;
			var behavior = Collections.singleton(smartBlockEntity.getBehaviour(FilteringBehaviour.TYPE));
			var first = behavior.iterator().next();
			if (!(first instanceof FilteringBehaviour)) return;
			var item = first.getFilter(blockHitResult.getDirection());
			if (!(item.getItem() instanceof FilterItem)) return;
			setFilterScreen(item);
		}
	}
	public static void setFilterScreen(@NotNull ItemStack filter) {
		try {
			var field = FilterItem.class.getDeclaredField("type");
			field.setAccessible(true);
			if (!(filter.getItem() instanceof FilterItem filterItem)) return;
			var type = field.get(filterItem);
			if (!field.getType().isEnum()) return;
			var mc = Minecraft.getInstance();
			var player = mc.player;
			if (player == null) return;
			var inv = player.getInventory();
			var name = filter.getHoverName();
			Screen screen;
			switch (((Enum<?>) type).ordinal()) {
				case 0 -> screen = new FilterScreen(FilterMenu.create(-1, inv, filter), inv, name);
				case 1 -> screen = new AttributeFilterScreen(AttributeFilterMenu.create(-1, inv, filter), inv, name);
				case 2 -> screen = new PackageFilterScreen(PackageFilterMenu.create(-1, inv, filter), inv, name);
				default -> {
					return;
				}
			}
			mc.setScreen(screen);
		} catch (Exception ignored) {
		}
	}
}
