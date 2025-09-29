package io.github.forgestove.create_cyber_goggles.core.event;
import com.mojang.datafixers.util.Function3;
import com.simibubi.create.*;
import com.simibubi.create.content.logistics.filter.*;
import com.simibubi.create.content.logistics.stockTicker.*;
import io.github.forgestove.create_cyber_goggles.*;
import io.github.forgestove.create_cyber_goggles.core.util.CCGLang;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.*;
import net.minecraftforge.client.event.InputEvent.Key;

import java.util.Map;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
public class KeyInput {
	public static StockTickerBlockEntity lastSTBE;
	public static void tick(Key ignoredEvent) {
		toggleGoggle();
		toggleDiving();
		openConfigScreen();
		openStockScreen();
		previewFilterScreen();
	}
	public static void toggleGoggle() {
		if (!CCGKey.toggleGoggle.isKeyDown()) return;
		var mode = CCG.CONFIG.gameMode;
		mode.enableGoggle = !mode.enableGoggle;
		var builder = CCGLang.translate("message.goggle").space().translate(mode.enableGoggle ? "message.enabled" : "message.disabled");
		displayMessage(builder);
		playSound(mode.enableGoggle ? AllSoundEvents.CONFIRM_2 : AllSoundEvents.DENY);
	}
	public static void toggleDiving() {
		if (!CCGKey.toggleDiving.isKeyDown()) return;
		var misc = CCG.CONFIG.misc;
		misc.allowDivingBoot = !misc.allowDivingBoot;
		var mc = Minecraft.getInstance();
		var player = mc.player;
		if (player == null || mc.screen != null) return;
		var builder = CCGLang.translate("message.divingBoot")
			.space()
			.translate(misc.allowDivingBoot ? "message.enabled" : "message.disabled");
		displayMessage(builder);
		playSound(misc.allowDivingBoot ? AllSoundEvents.CONFIRM_2 : AllSoundEvents.DENY);
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
		if (getBE() instanceof StockTickerBlockEntity stbe) lastSTBE = stbe;
		if (lastSTBE == null || lastSTBE.isRemoved()) {
			displayMessage(CCGLang.translate("message.notStock").text("  ").translate("key.openStock").style(ChatFormatting.RED));
			playSound(AllSoundEvents.DENY);
			return;
		}
		var inv = player.getInventory();
		var menu = new StockKeeperRequestMenu(AllMenuTypes.STOCK_KEEPER_REQUEST.get(), -1, inv, lastSTBE);
		mc.setScreen(new StockKeeperRequestScreen(menu, inv, lastSTBE.getBlockState().getBlock().getName()));
	}
	public static void previewFilterScreen() {
		if (!CCGKey.previewFilter.isKeyDown()) return;
		var mc = Minecraft.getInstance();
		var player = mc.player;
		if (player == null) return;
		var itemStack = getRelevantFilterItem();
		if (itemStack == null) return;
		if (!(itemStack.getItem() instanceof FilterItem)) {
			displayMessage(CCGLang.translate("message.notFilter").style(ChatFormatting.RED));
			playSound(AllSoundEvents.DENY);
			return;
		}
		mc.setScreen(Map.<Item, Function3<Integer, Inventory, ItemStack, Screen>>of(
			AllItems.FILTER.get(),
			(id, inv, stack) -> new FilterScreen(FilterMenu.create(id, inv, stack), inv, stack.getHoverName()),
			AllItems.ATTRIBUTE_FILTER.get(),
			(id, inv, stack) -> new AttributeFilterScreen(AttributeFilterMenu.create(id, inv, stack), inv, stack.getHoverName()),
			AllItems.PACKAGE_FILTER.get(),
			(id, inv, stack) -> new PackageFilterScreen(PackageFilterMenu.create(id, inv, stack), inv, stack.getHoverName())
		).get(itemStack.getItem()).apply(-1, player.getInventory(), itemStack));
		playSound(SoundEvents.BOOK_PAGE_TURN);
	}
}
