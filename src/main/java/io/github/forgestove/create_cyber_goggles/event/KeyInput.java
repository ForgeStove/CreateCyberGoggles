package io.github.forgestove.create_cyber_goggles.event;
import com.mojang.datafixers.util.Function3;
import com.zurrtum.create.*;
import com.zurrtum.create.client.content.logistics.filter.*;
import com.zurrtum.create.client.content.logistics.stockTicker.StockKeeperRequestScreen;
import com.zurrtum.create.content.logistics.filter.*;
import com.zurrtum.create.content.logistics.stockTicker.*;
import io.github.forgestove.create_cyber_goggles.*;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.*;

import java.util.Map;
public class KeyInput {
	public static void register(Minecraft ignoredMc) {
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
		var builder = CCGLang.translate("message.divingFunction")
			.space()
			.translate(misc.removeDivingFunction ? "message.disabled" : "message.enabled");
		Common.displayMessage(builder);
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
		if (Common.getBE() instanceof StockTickerBlockEntity stbe) Common.lastSTBE = stbe;
		if (Common.lastSTBE == null || Common.lastSTBE.isRemoved()) {
			Common.displayMessage(CCGLang.translate("message.notStock").text("  ").translate("key.openStock").style(ChatFormatting.RED));
			Common.playSound(AllSoundEvents.DENY);
			return;
		}
		var inv = player.getInventory();
		var menu = new StockKeeperRequestMenu(-1, inv, Common.lastSTBE);
		mc.setScreen(new StockKeeperRequestScreen(menu, inv, Common.lastSTBE.getBlockState().getBlock().getName()));
	}
	public static void previewFilterScreen() {
		if (!CCGKey.previewFilter.isKeyDown()) return;
		var mc = Minecraft.getInstance();
		var player = mc.player;
		if (player == null) return;
		var itemStack = Common.getRelevantFilterItem();
		if (itemStack == null) return;
		if (!(itemStack.getItem() instanceof FilterItem)) {
			Common.displayMessage(CCGLang.translate("message.notFilter").style(ChatFormatting.RED));
			Common.playSound(AllSoundEvents.DENY);
			return;
		}
		mc.setScreen(Map.<Item, Function3<Integer, Inventory, ItemStack, Screen>>of(
			AllItems.FILTER.asItem(),
			(id, inv, stack) -> new FilterScreen(new FilterMenu(id, inv, stack), inv, stack.getHoverName()),
			AllItems.ATTRIBUTE_FILTER.asItem(),
			(id, inv, stack) -> new AttributeFilterScreen(new AttributeFilterMenu(id, inv, stack), inv, stack.getHoverName()),
			AllItems.PACKAGE_FILTER.asItem(),
			(id, inv, stack) -> new PackageFilterScreen(new PackageFilterMenu(id, inv, stack), inv, stack.getHoverName())
		).get(itemStack.getItem()).apply(-1, player.getInventory(), itemStack));
		Common.playSound(SoundEvents.BOOK_PAGE_TURN);
	}
}
