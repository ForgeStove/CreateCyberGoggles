package io.github.forgestove.create_cyber_goggles.event;
import com.simibubi.create.*;
import com.simibubi.create.content.logistics.filter.*;
import com.simibubi.create.content.logistics.stockTicker.*;
import io.github.forgestove.create_cyber_goggles.*;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
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
		var menu = new StockKeeperRequestMenu(AllMenuTypes.STOCK_KEEPER_REQUEST.get(), -1, inv, Common.lastSTBE);
		mc.setScreen(new StockKeeperRequestScreen(menu, inv, Common.lastSTBE.getBlockState().getBlock().getName()));
	}
	public static void previewFilterScreen() {
		if (!CCGKey.previewFilter.isKeyDown()) return;
		var mc = Minecraft.getInstance();
		var player = mc.player;
		if (player == null) return;
		var itemStack = Common.getRelevantFilterItem();
		if (itemStack == null) return;
		if (itemStack.is(ItemStack.EMPTY.getItem()) || !(itemStack.getItem() instanceof FilterItem filterItem)) {
			Common.displayMessage(CCGLang.translate("message.notFilter").style(ChatFormatting.RED));
			Common.playSound(AllSoundEvents.DENY);
			return;
		}
		var inv = player.getInventory();
		var name = itemStack.getHoverName();
		mc.setScreen(switch (filterItem.type) {
			case REGULAR -> new FilterScreen(FilterMenu.create(-1, inv, itemStack), inv, name);
			case ATTRIBUTE -> new AttributeFilterScreen(AttributeFilterMenu.create(-1, inv, itemStack), inv, name);
			case PACKAGE -> new PackageFilterScreen(PackageFilterMenu.create(-1, inv, itemStack), inv, name);
		});
		Common.playSound(SoundEvents.BOOK_PAGE_TURN);
	}
}
