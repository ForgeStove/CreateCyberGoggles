package com.ForgeStove.create_cyber_goggles.event;
import com.ForgeStove.create_cyber_goggles.Config;
import com.simibubi.create.content.logistics.filter.*;
import com.simibubi.create.content.logistics.tableCloth.TableClothBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.client.event.InputEvent.*;
import org.lwjgl.glfw.GLFW;

import java.util.*;
public class KeyInputEvent {
	public static int index = 1;
	public static int scrollDeltaY = 0;
	public static void onMouseScroll(MouseScrollingEvent event) {
		if (!Config.enhancedInfo.get()) return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || !(mc.hitResult instanceof BlockHitResult blockHitResult)) return;
		if (blockHitResult.getType() == HitResult.Type.MISS) return;
		BlockEntity blockEntity = mc.level.getBlockEntity(blockHitResult.getBlockPos());
		KeyBinds.isKeyDown(KeyBinds.FILTER_MENU.getBoundCode());
		if (!(blockEntity instanceof TableClothBlockEntity)) return;
		event.setCanceled(true);
		if (event.getScrollDeltaY() == 0) scrollDeltaY = 0;
		else scrollDeltaY = event.getScrollDeltaY() > 0 ? -1 : 1;
	}
	public static void onKeyInput(Key event) {
		if (event.getKey() != KeyBinds.FILTER_MENU.getBoundCode() || event.getAction() != GLFW.GLFW_PRESS) return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null || !(mc.hitResult instanceof BlockHitResult blockHitResult)) return;
		if (blockHitResult.getType() == HitResult.Type.MISS) return;
		BlockEntity blockEntity = mc.level.getBlockEntity(blockHitResult.getBlockPos());
		if (!(blockEntity instanceof SmartBlockEntity smartBlockEntity)) return;
		if (mc.player == null) return;
		Collection<BlockEntityBehaviour> behavior = Collections.singleton(smartBlockEntity.getBehaviour(
				FilteringBehaviour.TYPE));
		BlockEntityBehaviour first = behavior.iterator().next();
		if (!(first instanceof FilteringBehaviour filteringBehaviour)) return;
		Inventory inventory = mc.player.getInventory();
		ItemStack filter = filteringBehaviour.getFilter(blockHitResult.getDirection());
		if (filter.isEmpty()) return;
		try {
			switch (filter.getDescriptionId()) {
				case "item.create.filter" -> mc.setScreen(new FilterScreen(
						FilterMenu.create(-1, inventory, filter),
						inventory,
						filter.getHoverName()
				));
				case "item.create.attribute_filter" ->
						mc.setScreen(new AttributeFilterScreen(
								AttributeFilterMenu.create(-1, inventory, filter),
								inventory,
								filter.getHoverName()
						));
				case "item.create.package_filter" ->
						mc.setScreen(new PackageFilterScreen(
								PackageFilterMenu.create(-1, inventory, filter),
								inventory,
								filter.getHoverName()
						));
			}
		} catch (Exception error) {
			mc.player.sendSystemMessage(Component.nullToEmpty(error.getMessage()));
		}
	}
}
