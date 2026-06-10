package io.github.forgestove.create_cyber_goggles.core.event;
import com.mojang.datafixers.util.Function3;
import com.zurrtum.create.*;
import com.zurrtum.create.client.content.logistics.filter.*;
import com.zurrtum.create.client.content.logistics.stockTicker.StockKeeperRequestScreen;
import com.zurrtum.create.content.equipment.clipboard.ClipboardBlockItem;
import com.zurrtum.create.content.logistics.filter.*;
import com.zurrtum.create.content.logistics.stockTicker.*;
import com.zurrtum.create.content.logistics.tableCloth.TableClothBlockEntity;
import com.zurrtum.create.infrastructure.component.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.config.Config;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.*;

import java.util.Map;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
public class KeyInput {
	public static StockTickerBlockEntity lastSTBE;
	public static int scrollDeltaY;
	public static void register(Minecraft ignoredMc) {
		toggleGoggle();
		toggleDiving();
		openConfigScreen();
		openStockScreen();
		previewFilterScreen();
	}
	public static boolean mouseScroll(double scrollDelta) {
		if (clothStore(scrollDelta)) return true;
		return tryScrollClipboardPage(scrollDelta);
	}
	private static void toggleGoggle() {
		toggleConfig(
			CCGKey.toggleGoggle.isDown(),
			CCG.config.gameMode.enableGoggles,
			val -> CCG.config.gameMode.enableGoggles = val,
			"message.goggle"
		);
	}
	private static void toggleDiving() {
		toggleConfig(
			CCGKey.toggleDiving.isDown(),
			CCG.config.misc.allowDivingBoot,
			val -> CCG.config.misc.allowDivingBoot = val,
			"message.divingBoot"
		);
	}
	private static void openConfigScreen() {
		if (!CCGKey.openConfig.isDown()) return;
		if (isInGUI()) return;
		mc.setScreen(Config.createConfigScreen(CCG.ID));
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
		var menu = new StockKeeperRequestMenu(-1, inv, lastSTBE);
		mc.setScreen(new StockKeeperRequestScreen(menu, inv, lastSTBE.getBlockState().getBlock().getName()));
	}
	private static void previewFilterScreen() {
		if (!CCGKey.previewFilter.isDown()) return;
		if (mc.player == null) return;
		var itemStack = getSelectedFilter();
		if (itemStack == null || !(itemStack.getItem() instanceof FilterItem)) return;
		mc.setScreen(Map.<Item, Function3<Integer, Inventory, ItemStack, Screen>>of(
			AllItems.FILTER.asItem(),
			(id, inv, stack) -> new FilterScreen(new FilterMenu(id, inv, stack), inv, stack.getHoverName()),
			AllItems.ATTRIBUTE_FILTER.asItem(),
			(id, inv, stack) -> new AttributeFilterScreen(new AttributeFilterMenu(id, inv, stack), inv, stack.getHoverName()),
			AllItems.PACKAGE_FILTER.asItem(),
			(id, inv, stack) -> new PackageFilterScreen(new PackageFilterMenu(id, inv, stack), inv, stack.getHoverName())
		).get(itemStack.getItem()).apply(-1, mc.player.getInventory(), itemStack));
		playSound(SoundEvents.BOOK_PAGE_TURN, 1.0f, 1.0f);
	}
	private static boolean clothStore(double scrollDelta) {
		if (!CCG.config.goggles.betterStoreInfo) return false;
		if (!CCGKey.toggleItemOverlay.isDown()) return false;
		var tcbe = getBlockEntity(TableClothBlockEntity.class);
		if (tcbe == null) return false;
		if (TableClothUtil.getItems(tcbe).size() <= 1) return false;
		if (hasActivedValueBox()) return false;
		scrollDeltaY = (int) scrollDelta;
		return true;
	}
	private static boolean tryScrollClipboardPage(double scrollDelta) {
		if (!CCGKey.clipboardPageScroll.isDown()) return false;
		if (mc.player == null || mc.screen != null) return false;
		var stack = mc.player.getMainHandItem();
		if (!(stack.getItem() instanceof ClipboardBlockItem)) {
			stack = mc.player.getOffhandItem();
			if (!(stack.getItem() instanceof ClipboardBlockItem)) return false;
		}
		if (scrollDelta == 0) return false;
		var content = stack.getOrDefault(AllDataComponents.CLIPBOARD_CONTENT, ClipboardContent.EMPTY);
		var pages = ClipboardEntry.readAll(content);
		if (pages.isEmpty()) return false;
		var page = Mth.clamp(content.previouslyOpenedPage(), 0, pages.size() - 1);
		var target = Mth.clamp(page + (scrollDelta < 0 ? 1 : -1), 0, pages.size() - 1);
		if (target == page) return false;
		stack.set(AllDataComponents.CLIPBOARD_CONTENT, content.setPreviouslyOpenedPage(target));
		playSound(SoundEvents.BOOK_PAGE_TURN, 1.0f, 1.0f);
		return true;
	}
}
