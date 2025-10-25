package io.github.forgestove.create_cyber_goggles.core.event;
import com.mojang.datafixers.util.Function3;
import com.simibubi.create.*;
import com.simibubi.create.content.logistics.filter.*;
import com.simibubi.create.content.logistics.stockTicker.*;
import com.simibubi.create.content.logistics.tableCloth.TableClothBlockEntity;
import io.github.forgestove.create_cyber_goggles.*;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.client.event.InputEvent.*;

import java.util.Map;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
public class KeyInput {
	public static StockTickerBlockEntity lastSTBE;
	public static int scrollDeltaY;
	public static void key(Key ignoredEvent) {
		toggleGoggle();
		toggleDiving();
		openConfigScreen();
		openStockScreen();
		previewFilterScreen();
	}
	public static void mouseScroll(MouseScrollingEvent event) {
		clothStore(event);
	}
	private static void toggleGoggle() {
		toggleConfig(
			CCGKey.toggleGoggle.isDown(),
			CCG.CONFIG.gameMode.enableGoggles,
			val -> CCG.CONFIG.gameMode.enableGoggles = val,
			"message.goggle"
		);
	}
	private static void toggleDiving() {
		toggleConfig(
			CCGKey.toggleDiving.isDown(),
			CCG.CONFIG.misc.allowDivingBoot,
			val -> CCG.CONFIG.misc.allowDivingBoot = val,
			"message.divingBoot"
		);
	}
	private static void openConfigScreen() {
		if (!CCGKey.openConfig.isDown()) return;
		if (isInGUI()) return;
		mc.setScreen(AutoConfig.getConfigScreen(CCGConfig.class, null).get());
	}
	private static void openStockScreen() {
		if (!CCGKey.openStock.isDown()) return;
		if (isInGUI()) return;
		if (mc.player == null) return;
		var stbe = getBlockEntity(StockTickerBlockEntity.class);
		if (stbe != null) lastSTBE = stbe;
		if (lastSTBE == null || lastSTBE.isRemoved()) {
			CCGLang.translate("message.notStock").text("  ").translate("key.openStock").style(ChatFormatting.RED).sendStatus(mc.player);
			return;
		}
		var inv = mc.player.getInventory();
		var menu = new StockKeeperRequestMenu(AllMenuTypes.STOCK_KEEPER_REQUEST.get(), -1, inv, lastSTBE);
		mc.setScreen(new StockKeeperRequestScreen(menu, inv, lastSTBE.getBlockState().getBlock().getName()));
	}
	private static void previewFilterScreen() {
		if (!CCGKey.previewFilter.isDown()) return;
		if (mc.player == null) return;
		var itemStack = getSelectedFilter();
		if (itemStack == null || !(itemStack.getItem() instanceof FilterItem)) return;
		mc.setScreen(Map.<Item, Function3<Integer, Inventory, ItemStack, Screen>>of(
			AllItems.FILTER.get(),
			(id, inv, stack) -> new FilterScreen(FilterMenu.create(id, inv, stack), inv, stack.getHoverName()),
			AllItems.ATTRIBUTE_FILTER.get(),
			(id, inv, stack) -> new AttributeFilterScreen(AttributeFilterMenu.create(id, inv, stack), inv, stack.getHoverName()),
			AllItems.PACKAGE_FILTER.get(),
			(id, inv, stack) -> new PackageFilterScreen(PackageFilterMenu.create(id, inv, stack), inv, stack.getHoverName())
		).get(itemStack.getItem()).apply(-1, mc.player.getInventory(), itemStack));
		playSound(SoundEvents.BOOK_PAGE_TURN, 1.0f, 1.0f);
	}
	private static void clothStore(MouseScrollingEvent event) {
		if (!CCG.CONFIG.goggles.betterStoreInfo) return;
		var tcbe = getBlockEntity(TableClothBlockEntity.class);
		if (tcbe == null) return;
		if (TableClothUtil.getItems(tcbe).size() <= 1) return;
		if (hasActivedValueBox()) return;
		scrollDeltaY = (int) event.getScrollDeltaY();
		event.setCanceled(true);
	}
}
