package io.github.forgestove.create_cyber_goggles.event;
import com.simibubi.create.AllMenuTypes;
import com.simibubi.create.content.logistics.stockTicker.*;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import io.github.forgestove.create_cyber_goggles.*;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.neoforge.client.event.InputEvent.Key;

import java.util.Collections;
public class KeyInput {
	public static void tick(Key ignoredEvent) {
		toggleDiving();
		openConfigScreen();
		openStockScreen();
		previewFilterScreen();
	}
	public static void toggleDiving() {
		if (!CCGKey.toggleDiving.get().isDown()) return;
		var misc = CCG.CONFIG.misc;
		misc.removeDivingBootsAffect = !misc.removeDivingBootsAffect;
		Common.displayClientMessage(misc.removeDivingBootsAffect);
	}
	public static void openConfigScreen() {
		if (!CCGKey.openConfig.get().isDown()) return;
		var mc = Minecraft.getInstance();
		if (mc.screen != null) return;
		mc.setScreen(AutoConfig.getConfigScreen(CCGConfig.class, null).get());
	}
	public static void openStockScreen() {
		if (!CCGKey.openStock.get().isDown()) return;
		var mc = Minecraft.getInstance();
		if (mc.screen != null) return;
		if (mc.player == null) return;
		if (Common.getSelectedBE() instanceof StockTickerBlockEntity stbe) Common.laststbe = stbe;
		if (Common.laststbe == null) return;
		var inv = mc.player.getInventory();
		var menu = new StockKeeperRequestMenu(AllMenuTypes.STOCK_KEEPER_REQUEST.get(), -1, inv, Common.laststbe);
		mc.setScreen(new StockKeeperRequestScreen(menu, inv, Common.laststbe.getBlockState().getBlock().getName()));
	}
	public static void previewFilterScreen() {
		if (!CCGKey.previewFilter.get().isDown()) return;
		var mc = Minecraft.getInstance();
		if (mc.screen != null) {
			if (!(mc.screen instanceof AbstractContainerScreen<?> screen)) return;
			var slot = screen.getSlotUnderMouse();
			if (slot == null) return;
			Common.openFilterScreen(slot.getItem());
		} else {
			if (mc.level == null || !(mc.hitResult instanceof BlockHitResult blockHitResult)) return;
			if (blockHitResult.getType() == Type.MISS) return;
			var be = mc.level.getBlockEntity(blockHitResult.getBlockPos());
			if (!(be instanceof SmartBlockEntity sbe)) return;
			var first = Collections.singleton(sbe.getBehaviour(FilteringBehaviour.TYPE)).iterator().next();
			if (!(first instanceof FilteringBehaviour)) return;
			Common.openFilterScreen(first.getFilter(blockHitResult.getDirection()));
		}
	}
}
