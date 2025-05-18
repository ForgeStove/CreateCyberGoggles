package com.forgestove.create_cyber_goggles.content.event;
import com.forgestove.create_cyber_goggles.CreateCyberGoggles;
import com.forgestove.create_cyber_goggles.content.config.*;
import com.forgestove.create_cyber_goggles.content.util.Common;
import com.simibubi.create.AllMenuTypes;
import com.simibubi.create.content.logistics.filter.*;
import com.simibubi.create.content.logistics.stockTicker.*;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import me.shedaniel.autoconfig.AutoConfig;
import net.createmod.catnip.gui.ScreenOpener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;

import java.util.Collections;
public class KeyInput {
	public static void toggleDiving() {
		if (!CCGKeyMapping.toggleDiving.isDown()) return;
		var mc = Minecraft.getInstance();
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
	public static void openConfigScreen() {
		if (!CCGKeyMapping.openConfig.isDown()) return;
		var mc = Minecraft.getInstance();
		if (mc.screen != null) return;
		mc.setScreen(AutoConfig.getConfigScreen(CCGConfigData.class, null).get());
	}
	public static void openStockScreen() {
		if (!CCGKeyMapping.openStock.isDown()) return;
		var mc = Minecraft.getInstance();
		if (mc.screen != null) return;
		if (mc.player == null) return;
		if (Common.laststbe == null && Common.getSelectedBE() instanceof StockTickerBlockEntity stbe) Common.laststbe = stbe;
		if (Common.laststbe == null) return;
		var inv = mc.player.getInventory();
		var menu = new StockKeeperRequestMenu(AllMenuTypes.STOCK_KEEPER_REQUEST.get(), -1, inv, Common.laststbe);
		mc.setScreen(new StockKeeperRequestScreen(menu, inv, Common.laststbe.getBlockState().getBlock().getName()));
	}
	public static void previewFilterScreen() {
		if (!(CCGKeyMapping.previewFilter.isDown())) return;
		var mc = Minecraft.getInstance();
		if (mc.screen != null) {
			if (!(mc.screen instanceof AbstractContainerScreen<?> screen)) return;
			var slot = screen.getSlotUnderMouse();
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
		if (!(filter.getItem() instanceof FilterItem filterItem)) return;
		var player = Minecraft.getInstance().player;
		if (player == null) return;
		var inv = player.getInventory();
		var name = filter.getHoverName();
		ScreenOpener.open(switch (filterItem.type) {
			case REGULAR -> new FilterScreen(FilterMenu.create(-1, inv, filter), inv, name);
			case ATTRIBUTE -> new AttributeFilterScreen(AttributeFilterMenu.create(-1, inv, filter), inv, name);
			case PACKAGE -> new PackageFilterScreen(PackageFilterMenu.create(-1, inv, filter), inv, name);
		});
	}
}
