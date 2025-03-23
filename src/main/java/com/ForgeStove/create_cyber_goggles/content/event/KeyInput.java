package com.ForgeStove.create_cyber_goggles.content.event;
import com.ForgeStove.create_cyber_goggles.Config;
import com.simibubi.create.content.logistics.filter.*;
import com.simibubi.create.content.logistics.tableCloth.TableClothBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.neoforge.client.event.InputEvent.*;
import org.lwjgl.glfw.GLFW;

import java.util.Collections;
public class KeyInput {
	public static int index = 1;
	public static int scrollDeltaY = 0;
	public static void onMouseScroll(MouseScrollingEvent event) {
		if (!Config.enhancedStoreRender.get()) return;
		var mc = Minecraft.getInstance();
		if (mc.level == null || !(mc.hitResult instanceof BlockHitResult blockHitResult)) return;
		if (blockHitResult.getType() == Type.MISS) return;
		var blockEntity = mc.level.getBlockEntity(blockHitResult.getBlockPos());
		if (!(blockEntity instanceof TableClothBlockEntity)) return;
		if (event.getScrollDeltaY() == 0) scrollDeltaY = 0;
		else scrollDeltaY = event.getScrollDeltaY() > 0 ? -1 : 1;
		event.setCanceled(true);
	}
	public static void onKeyInput(Key event) {
		if (!Config.enableOpenFilterScreen.get()) return;
		if (!KeyBind.isAction(event, KeyBind.PREVIEW_FILTER, GLFW.GLFW_PRESS)) return;
		var mc = Minecraft.getInstance();
		if (mc.screen != null) {
			if (!(mc.screen instanceof AbstractContainerScreen<?> screen)) return;
			var slot = screen.getSlotUnderMouse();
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
		if (filter.isEmpty()) return;
		var mc = Minecraft.getInstance();
		var player = mc.player;
		if (player == null) return;
		var inventory = player.getInventory();
		var hoverName = filter.getHoverName();
		switch (filter.getDescriptionId()) {
			case "item.create.filter" -> {
				var menu = FilterMenu.create(0, inventory, filter);
				mc.setScreen(new FilterScreen(menu, inventory, hoverName));
			}
			case "item.create.attribute_filter" -> {
				var menu = AttributeFilterMenu.create(0, inventory, filter);
				mc.setScreen(new AttributeFilterScreen(menu, inventory, hoverName));
			}
			case "item.create.package_filter" -> {
				var menu = PackageFilterMenu.create(0, inventory, filter);
				mc.setScreen(new PackageFilterScreen(menu, inventory, hoverName));
			}
		}
	}
}
