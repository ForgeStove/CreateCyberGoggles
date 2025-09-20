package io.github.forgestove.create_cyber_goggles.event;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.client.content.logistics.filter.*;
import com.zurrtum.create.client.content.logistics.stockTicker.StockKeeperRequestScreen;
import com.zurrtum.create.content.logistics.filter.*;
import com.zurrtum.create.content.logistics.stockTicker.*;
import io.github.forgestove.create_cyber_goggles.*;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
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
		if (itemStack == null || !(itemStack.getItem() instanceof FilterItem filterItem)) {
			Common.displayMessage(CCGLang.translate("message.notFilter").style(ChatFormatting.RED));
			Common.playSound(AllSoundEvents.DENY);
			return;
		}
		try {
			var field = FilterItem.class.getDeclaredField("type");
			field.setAccessible(true);
			var ordinal = ((Enum<?>) field.get(filterItem)).ordinal();
			var inv = player.getInventory();
			var name = itemStack.getHoverName();
			mc.setScreen(switch (ordinal) {
				case 0 -> new FilterScreen(new FilterMenu(-1, inv, itemStack), inv, name);
				case 1 -> new AttributeFilterScreen(new AttributeFilterMenu(-1, inv, itemStack), inv, name);
				case 2 -> new PackageFilterScreen(new PackageFilterMenu(-1, inv, itemStack), inv, name);
				default -> throw new IllegalStateException("Unexpected value: " + ordinal);
			});
		} catch (Exception ignored) {}
		Common.playSound(SoundEvents.BOOK_PAGE_TURN);
	}
}
