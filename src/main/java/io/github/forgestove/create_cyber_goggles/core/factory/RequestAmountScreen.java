package io.github.forgestove.create_cyber_goggles.core.factory;
import com.simibubi.create.content.logistics.box.PackageStyles;
import com.simibubi.create.foundation.gui.*;
import com.simibubi.create.foundation.gui.widget.IconButton;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.CCGLang;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
public class RequestAmountScreen extends AbstractSimiScreen {
	private final Screen parentScreen;
	private final ItemStack stack;
	private final int initial;
	private final int max;
	private final Consumer<Integer> onApply;
	private final ItemStack packageBox;
	private EditBox amountInput;
	public RequestAmountScreen(Screen parentScreen, ItemStack stack, int initial, int max, Consumer<Integer> onApply) {
		this.parentScreen = parentScreen;
		this.stack = stack.copy();
		this.initial = initial;
		this.max = Math.max(0, max);
		this.onApply = onApply;
		packageBox = PackageStyles.getRandomBox();
		mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1));
	}
	@Override
	protected void init() {
		int popupWidth = 218, POPUPHEIGHT = 79;
		setWindowSize(popupWidth, POPUPHEIGHT);
		var window = mc.getWindow();
		var x = (window.getGuiScaledWidth() - popupWidth) / 2;
		var y = (window.getGuiScaledHeight() - POPUPHEIGHT) / 2;
		setWindowOffset(x, y);
		super.init();
		var confirmButton = new IconButton(x + 185, y + 55, AllIcons.I_CONFIRM);
		confirmButton.withCallback(this::apply);
		confirmButton.setToolTip(Component.translatable("config.ui.quit.confirm"));
		var cancelButton = new IconButton(x + 155, y + 55, AllIcons.I_CONFIG_BACK);
		cancelButton.withCallback(this::onClose);
		cancelButton.setToolTip(Component.translatable("gui.cancel"));
		amountInput = new EditBox(mc.font, x + 44, y + 28, 129, 9, stack.getHoverName());
		amountInput.setFilter(value -> value.chars().allMatch(Character::isDigit));
		amountInput.setResponder(text -> {
			if (text.isEmpty()) return;
			try {
				if (Long.parseLong(text) > max) amountInput.setValue(Integer.toString(max));
			} catch (NumberFormatException e) {
				CCG.LOGGER.warn(e.getLocalizedMessage(), e);
			}
		});
		amountInput.setBordered(false);
		amountInput.setValue(initial > 1 ? Integer.toString(Mth.clamp(initial, 0, max)) : "");
		amountInput.setFocused(true);
		addRenderableWidgets(confirmButton, cancelButton, amountInput);
	}
	protected void apply() {
		var amount = Mth.clamp(toInt(amountInput.getValue()), 0, max);
		onApply.accept(amount);
		onClose();
	}
	@Override
	public void onClose() {
		removed();
		mc.screen = parentScreen;
	}
	protected static int toInt(String amount) {
		try {
			return Integer.parseInt(amount);
		} catch (NumberFormatException e) {
			CCG.LOGGER.warn(e.getLocalizedMessage(), e);
			return 1;
		}
	}
	@Override
	public void resize(@NotNull Minecraft minecraft, int width, int height) {
		parentScreen.resize(minecraft, width, height);
		super.resize(minecraft, width, height);
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (amountInput.isHoveredOrFocused()) {
			var amount = Mth.clamp(toInt(amountInput.getValue()) + (long) (getModifiedScrollAmount() * scrollY), 0L, max);
			amountInput.setValue(Long.toString(amount));
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}
	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		return amountInput.charTyped(codePoint, modifiers);
	}
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			onClose();
			mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
			return super.keyReleased(keyCode, scanCode, modifiers);
		}
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			apply();
			mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
			return super.keyReleased(keyCode, scanCode, modifiers);
		}
		return amountInput.keyPressed(keyCode, scanCode, modifiers);
	}
	@Override
	protected void renderWindow(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		var pose = gui.pose();
		pose.pushPose();
		pose.translate(windowXOffset, windowYOffset, 0);
		if (amountInput.getValue().isEmpty()) gui.drawString(mc.font, String.valueOf(max), 44, 28, 0x808080, false);
		GuiGameElement.of(stack).at(16, 24).render(gui);
		var title = CCGLang.itemName(stack).component();
		gui.drawString(mc.font, title, (210 - mc.font.width(title)) / 2, 4, 0x303030, false);
		GuiGameElement.of(packageBox).scale(3).at(215, 35, 0).render(gui);
		pose.popPose();
		if (mouseX >= windowXOffset + 16 && mouseX < windowXOffset + 32 && mouseY >= windowYOffset + 24 && mouseY < windowYOffset + 40)
			gui.renderComponentTooltip(mc.font, getTooltipFromItem(mc, stack), mouseX, mouseY);
	}
	@Override
	protected void renderWindowBackground(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		var pose = gui.pose();
		pose.pushPose();
		pose.translate(0, 0, -500);
		super.renderWindowBackground(gui, mouseX, mouseY, partialTick);
		var currentScreen = mc.screen;
		mc.screen = parentScreen;
		parentScreen.renderBackground(gui, -1, -1, partialTick);
		parentScreen.render(gui, -1, -1, partialTick);
		mc.screen = currentScreen;
		pose.popPose();
		pose.pushPose();
		pose.translate(windowXOffset, windowYOffset, 0);
		AllGuiTextures.PACKAGE_FILTER.render(gui, 0, 0);
		pose.popPose();
	}
}
