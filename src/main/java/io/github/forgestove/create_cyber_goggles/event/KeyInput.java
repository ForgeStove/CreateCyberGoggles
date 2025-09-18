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
public class KeyInput {
	public static void tick(Key ignoredEvent) {
		toggleDiving();
		openConfigScreen();
		openStockScreen();
		previewFilterScreen();
	}
	public static void toggleDiving() {
		if (!CCGKey.toggleDiving.isKeyDown()) return;
		var misc = CCG.CONFIG.misc;
		misc.removeDivingFunction = !misc.removeDivingFunction;
		var mc = Minecraft.getInstance();
		var player = mc.player;
		if (player == null || mc.screen != null) return;
		var component = CCGLang.translate("message.divingFunction")
			.space()
			.translate(misc.removeDivingFunction ? "message.disabled" : "message.enabled")
			.component();
		player.displayClientMessage(component, true);
	}
	public static void openConfigScreen() {
		if (!CCGKey.openConfig.isKeyDown()) return;
		var mc = Minecraft.getInstance();
		if (mc.screen != null) return;
		mc.setScreen(AutoConfig.getConfigScreen(CCGConfig.class, null).get());
	}
	public static void openStockScreen() {
		if (!CCGKey.openStock.isKeyDown()) return;
		var mc = Minecraft.getInstance();
		if (mc.screen != null) return;
		var player = mc.player;
		if (player == null) return;
		if (Common.getSelectedBE() instanceof StockTickerBlockEntity stbe) Common.lastSTBE = stbe;
		if (Common.lastSTBE == null || Common.lastSTBE.isRemoved()) {
			player.displayClientMessage(CCGLang.translate("message.notStock").text("  ").translate("key.openStock").component(), true);
			return;
		}
		var inv = player.getInventory();
		var menu = new StockKeeperRequestMenu(AllMenuTypes.STOCK_KEEPER_REQUEST.get(), -1, inv, Common.lastSTBE);
		mc.setScreen(new StockKeeperRequestScreen(menu, inv, Common.lastSTBE.getBlockState().getBlock().getName()));
	}
	public static void previewFilterScreen() {
		if (!CCGKey.previewFilter.isKeyDown()) return;
		var mc = Minecraft.getInstance();
		if (mc.screen != null) {
			if (!(mc.screen instanceof AbstractContainerScreen<?> screen)) return;
			var slot = screen.getSlotUnderMouse();
			if (slot == null) return;
			Common.openFilterScreen(slot.getItem());
		} else {
			if (mc.level == null || !(mc.hitResult instanceof BlockHitResult blockHitResult)) return;
			if (blockHitResult.getType() == Type.MISS) return;
			if (!(Common.getSelectedBE() instanceof SmartBlockEntity sbe)) return;
			Common.openFilterScreen(sbe.getBehaviour(FilteringBehaviour.TYPE).getFilter(blockHitResult.getDirection()));
		}
	}
}
