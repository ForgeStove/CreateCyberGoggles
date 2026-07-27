package io.github.forgestove.create_cyber_goggles.core.event;
import com.mojang.datafixers.util.Function3;
import com.simibubi.create.*;
import com.simibubi.create.content.equipment.clipboard.*;
import com.simibubi.create.content.logistics.filter.*;
import com.simibubi.create.content.logistics.stockTicker.*;
import com.simibubi.create.content.logistics.tableCloth.TableClothBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import io.github.forgestove.flexconfig.client.ConfigScreenFactory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.client.event.InputEvent.*;

import java.util.Map;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
public final class KeyInput {
	public static StockTickerBlockEntity lastSTBE;
	public static int scrollDeltaY;
	public static void key(Key ignoredEvent) {
		openConfigScreen();
		openStockScreen();
		previewFilterScreen();
	}
	private static void openConfigScreen() {
		if (!CCGKey.openConfig.isDown()) return;
		if (isInGUI()) return;
		mc.setScreen(ConfigScreenFactory.createConfigScreen(CCG.ID));
	}
	private static void openStockScreen() {
		if (!CCGKey.openStock.isDown()) return;
		if (isInGUI()) return;
		if (mc.player == null) return;
		var stbe = getBlockEntity(StockTickerBlockEntity.class);
		if (stbe != null) lastSTBE = stbe;
		if (lastSTBE == null || lastSTBE.isRemoved()) {
			CCGLang.add(Component.translatable("create_cyber_goggles.message.notStock"))
				.text("  ")
				.add(Component.translatable("create_cyber_goggles.key.openStock").withStyle(ChatFormatting.RED))
				.sendStatus(mc.player);
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
	public static void mouseScroll(MouseScrollingEvent event) {
		clothStore(event);
		tryScrollClipboardPage(event);
	}
	private static void clothStore(MouseScrollingEvent event) {
		if (!CCG.config.goggles.betterStoreInfo) return;
		if (!CCGKey.toggleItemOverlay.isDown()) return;
		var tcbe = getBlockEntity(TableClothBlockEntity.class);
		if (tcbe == null) return;
		if (TableClothUtil.getItems(tcbe).size() <= 1) return;
		if (hasActivedValueBox()) return;
		scrollDeltaY = (int) event.getScrollDeltaY();
		event.setCanceled(true);
	}
	private static void tryScrollClipboardPage(MouseScrollingEvent event) {
		if (!CCGKey.clipboardPageScroll.isDown()) return;
		if (mc.player == null || mc.screen != null) return;
		var stack = mc.player.getMainHandItem();
		if (!(stack.getItem() instanceof ClipboardBlockItem)) {
			stack = mc.player.getOffhandItem();
			if (!(stack.getItem() instanceof ClipboardBlockItem)) return;
		}
		var delta = event.getScrollDeltaY();
		if (delta == 0) return;
		var content = stack.getOrDefault(AllDataComponents.CLIPBOARD_CONTENT, ClipboardContent.EMPTY);
		var pages = ClipboardEntry.readAll(content);
		if (pages.isEmpty()) return;
		event.setCanceled(true);
		var page = Mth.clamp(content.previouslyOpenedPage(), 0, pages.size() - 1);
		var target = Mth.clamp(page + (delta < 0 ? 1 : -1), 0, pages.size() - 1);
		if (target == page) return;
		stack.set(AllDataComponents.CLIPBOARD_CONTENT, content.setPreviouslyOpenedPage(target));
		playSound(SoundEvents.BOOK_PAGE_TURN, 1.0f, 1.0f);
	}
}
