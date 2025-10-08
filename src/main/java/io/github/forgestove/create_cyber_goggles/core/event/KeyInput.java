package io.github.forgestove.create_cyber_goggles.core.event;
import com.mojang.datafixers.util.Function3;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.logistics.filter.*;
import io.github.forgestove.create_cyber_goggles.*;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.*;
import net.minecraftforge.client.event.InputEvent.Key;

import java.util.Map;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
public class KeyInput {
	public static void key(Key ignoredEvent) {
		toggleGoggle();
		toggleDiving();
		openConfigScreen();
		previewFilterScreen();
	}
	private static void toggleGoggle() {
		toggleConfig(
			CCGKey.toggleGoggle.isDown(),
			CCG.CONFIG.gameMode.enableGoggle,
			val -> CCG.CONFIG.gameMode.enableGoggle = val,
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
	private static void previewFilterScreen() {
		if (!CCGKey.previewFilter.isDown()) return;
		if (mc.player == null) return;
		var itemStack = getRelevantFilterItem();
		if (itemStack == null) return;
		if (!(itemStack.getItem() instanceof FilterItem)) return;
		mc.setScreen(Map.<Item, Function3<Integer, Inventory, ItemStack, Screen>>of(
			AllItems.FILTER.get(),
			(id, inv, stack) -> new FilterScreen(FilterMenu.create(id, inv, stack), inv, stack.getHoverName()),
			AllItems.ATTRIBUTE_FILTER.get(),
			(id, inv, stack) -> new AttributeFilterScreen(AttributeFilterMenu.create(id, inv, stack), inv, stack.getHoverName())
		).get(itemStack.getItem()).apply(-1, mc.player.getInventory(), itemStack));
		playSound(SoundEvents.BOOK_PAGE_TURN, 1, 1);
	}
}
