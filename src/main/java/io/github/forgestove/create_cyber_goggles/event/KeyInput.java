package io.github.forgestove.create_cyber_goggles.event;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.filter.*;
import io.github.forgestove.create_cyber_goggles.*;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.client.event.InputEvent.Key;
public class KeyInput {
	public static void tick(Key ignoredEvent) {
		toggleDiving();
		openConfigScreen();
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
		var inv = player.getInventory();
		var name = itemStack.getHoverName();
		mc.setScreen(switch (filterItem.type) {
			case REGULAR -> new FilterScreen(FilterMenu.create(-1, inv, itemStack), inv, name);
			case ATTRIBUTE -> new AttributeFilterScreen(AttributeFilterMenu.create(-1, inv, itemStack), inv, name);
		});
		Common.playSound(SoundEvents.BOOK_PAGE_TURN);
	}
}
