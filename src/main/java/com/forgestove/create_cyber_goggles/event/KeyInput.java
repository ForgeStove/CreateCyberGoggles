package com.forgestove.create_cyber_goggles.event;
import com.forgestove.create_cyber_goggles.CreateCyberGoggles;
import com.forgestove.create_cyber_goggles.config.ModConfig;
import com.simibubi.create.AllMenuTypes;
import com.simibubi.create.content.logistics.filter.*;
import com.simibubi.create.content.logistics.stockTicker.*;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import me.shedaniel.autoconfig.AutoConfig;
import net.createmod.catnip.gui.ScreenOpener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraftforge.client.event.InputEvent.Key;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.Collections;
public class KeyInput {
	public static StockTickerBlockEntity lastBlockEntity = null;
	public static void toggleDiving(Key event) {
		if (!KeyBind.isAction(event, KeyBind.TOGGLE_DIVING, GLFW.GLFW_PRESS)) return;
		var mc = Minecraft.getInstance();
		var player = mc.player;
		if (player == null || mc.screen != null) return;
		var enabled = CreateCyberGoggles.config.armor.removeDivingBootsAffect;
		CreateCyberGoggles.config.armor.removeDivingBootsAffect = !enabled;
		player.displayClientMessage(
				Component.translatable("message.%s.%s".formatted(
						CreateCyberGoggles.ID,
						enabled ? "enableDivingAffect" : "disableDivingAffect"
				)), true
		);
	}
	public static void openConfigScreen(Key event) {
		if (!KeyBind.isAction(event, KeyBind.OPEN_CONFIG, GLFW.GLFW_PRESS)) return;
		var mc = Minecraft.getInstance();
		if (mc.screen != null) return;
		mc.setScreen(AutoConfig.getConfigScreen(ModConfig.class, null).get());
	}
	public static void openStockScreen(Key event) {
		if (!KeyBind.isAction(event, KeyBind.OPEN_STOCK, GLFW.GLFW_PRESS)) return;
		var mc = Minecraft.getInstance();
		if (mc.screen != null) return;
		var player = mc.player;
		if (player == null) return;
		if (mc.hitResult == null) return;
		if (mc.hitResult instanceof BlockHitResult blockHitResult && (
				lastBlockEntity == null || blockHitResult.getType() == Type.BLOCK
		)) {
			if (mc.level == null) return;
			if ((mc.level.getBlockEntity(blockHitResult.getBlockPos()) instanceof StockTickerBlockEntity stockTickerBlockEntity))
				lastBlockEntity = stockTickerBlockEntity;
		}
		if (lastBlockEntity == null) return;
		var type = AllMenuTypes.STOCK_KEEPER_REQUEST.get();
		var inv = player.getInventory();
		var menu = new StockKeeperRequestMenu(type, -1, inv, lastBlockEntity);
		mc.setScreen(new StockKeeperRequestScreen(menu, inv, lastBlockEntity.getBlockState().getBlock().getName()));
	}
	public static void openFilterScreen(Key event) {
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
			if (!field.getType().isEnum()) return;
			var mc = Minecraft.getInstance();
			var player = mc.player;
			if (player == null) return;
			var inv = player.getInventory();
			var name = filter.getHoverName();
			Screen screen;
			switch (((Enum<?>) field.get(filterItem)).ordinal()) {
				case 0 -> screen = new FilterScreen(FilterMenu.create(-1, inv, filter), inv, name);
				case 1 -> screen = new AttributeFilterScreen(AttributeFilterMenu.create(-1, inv, filter), inv, name);
				case 2 -> screen = new PackageFilterScreen(PackageFilterMenu.create(-1, inv, filter), inv, name);
				default -> {
					return;
				}
			}
			ScreenOpener.open(screen);
		} catch (Exception ignored) {
		}
	}
}
