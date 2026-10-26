package io.github.forgestove.create_cyber_goggles.event;
import com.simibubi.create.*;
import com.simibubi.create.content.equipment.clipboard.*;
import com.simibubi.create.content.logistics.tableCloth.TableClothBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.api.FilterPreview;
import io.github.forgestove.create_cyber_goggles.util.TableClothUtil;
import io.github.forgestove.flexconfig.client.FlexConfigScreenFactory;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.client.event.InputEvent.*;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.util.CCGUtil.*;
public final class KeyInput {
	public static int scrollDeltaY;
	public static void key(Key ignoredEvent) {
		openConfigScreen();
		previewFilterScreen();
	}
	private static void openConfigScreen() {
		if (!CCGKey.openConfig.isDown()) return;
		if (isInGUI()) return;
		mc.setScreen(FlexConfigScreenFactory.createConfigScreen(CCG.ID));
	}
	private static void previewFilterScreen() {
		if (!CCGKey.previewFilter.isDown()) return;
		if (mc.player == null) return;
		ItemStack itemStack = getSelectedFilter();
		if (itemStack == null) return;
		Screen screen = FilterPreview.create(-1, mc.player.getInventory(), itemStack.copy());
		if (screen == null) {
			mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BASS, 0.5F));
			return;
		}
		mc.tell(() -> mc.setScreen(screen));
		mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1));
	}
	public static void mouseScroll(MouseScrollingEvent event) {
		clothStore(event);
		tryScrollClipboardPage(event);
	}
	private static void clothStore(MouseScrollingEvent event) {
		if (!CCG.config.goggles.betterStoreInfo) return;
		if (!CCGKey.toggleItemOverlay.isDown()) return;
		TableClothBlockEntity tcbe = getBlockEntity(TableClothBlockEntity.class);
		if (tcbe == null) return;
		if (TableClothUtil.getItems(tcbe).size() <= 1) return;
		if (hasActivedValueBox()) return;
		scrollDeltaY = (int) event.getScrollDeltaY();
		event.setCanceled(true);
	}
	private static void tryScrollClipboardPage(MouseScrollingEvent event) {
		if (!CCGKey.clipboardPageScroll.isDown()) return;
		if (mc.player == null || mc.screen != null) return;
		ItemStack stack = mc.player.getMainHandItem();
		if (!(stack.getItem() instanceof ClipboardBlockItem)) {
			stack = mc.player.getOffhandItem();
			if (!(stack.getItem() instanceof ClipboardBlockItem)) return;
		}
		double delta = event.getScrollDeltaY();
		if (delta == 0) return;
		ClipboardContent content = stack.getOrDefault(AllDataComponents.CLIPBOARD_CONTENT, ClipboardContent.EMPTY);
		List<List<ClipboardEntry>> pages = ClipboardEntry.readAll(content);
		if (pages.isEmpty()) return;
		event.setCanceled(true);
		int page = Mth.clamp(content.previouslyOpenedPage(), 0, pages.size() - 1);
		int target = Mth.clamp(page + (delta < 0 ? 1 : -1), 0, pages.size() - 1);
		if (target == page) return;
		stack.set(AllDataComponents.CLIPBOARD_CONTENT, content.setPreviouslyOpenedPage(target));
		mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1));
	}
}
