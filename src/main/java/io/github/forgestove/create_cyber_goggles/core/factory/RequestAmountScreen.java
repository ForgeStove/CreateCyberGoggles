package io.github.forgestove.create_cyber_goggles.core.factory;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.logistics.box.PackageStyles;
import com.simibubi.create.foundation.gui.*;
import com.simibubi.create.foundation.gui.widget.IconButton;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.*;
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
		Window window = mc.getWindow();
		int x = (window.getGuiScaledWidth() - popupWidth) / 2;
		int y = (window.getGuiScaledHeight() - POPUPHEIGHT) / 2;
		setWindowOffset(x, y);
		super.init();
		var confirmButton = new IconButton(x + 185, y + 55, AllIcons.I_CONFIRM);
		confirmButton.withCallback(this::apply);
		confirmButton.setToolTip(Component.translatable("flexconfig.quit.confirm"));
		var cancelButton = new IconButton(x + 155, y + 55, AllIcons.I_CONFIG_BACK);
		cancelButton.withCallback(this::onClose);
		cancelButton.setToolTip(Component.translatable("gui.cancel"));
		amountInput = new EditBox(mc.font, x + 44, y + 28, 129, 9, stack.getHoverName());
		amountInput.setFilter(CCG.config.misc.expressionInput
			? ExpressionParser::isAllowed
			: value -> value.chars().allMatch(Character::isDigit));
		amountInput.setResponder(text -> {
			if (text.isEmpty()) return;
			Long value = ExpressionParser.evaluate(text);
			if (value != null && value > max) amountInput.setValue(Integer.toString(max));
		});
		amountInput.setBordered(false);
		amountInput.setValue(initial > 1 ? Integer.toString(Mth.clamp(initial, 0, max)) : "");
		amountInput.setFocused(true);
		addRenderableWidgets(confirmButton, cancelButton, amountInput);
	}
	@Override
	protected void renderWindowBackground(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		PoseStack pose = gui.pose();
		pose.pushPose();
		pose.translate(0, 0, -500);
		Screen currentScreen = mc.screen;
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
	@Override
	public void onClose() {
		removed();
		mc.screen = parentScreen;
	}
	@Override
	public void resize(@NotNull Minecraft minecraft, int width, int height) {
		parentScreen.resize(minecraft, width, height);
		super.resize(minecraft, width, height);
	}
	@Override
	protected void renderWindow(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		PoseStack pose = gui.pose();
		pose.pushPose();
		pose.translate(windowXOffset, windowYOffset, 0);
		if (amountInput.getValue().isEmpty()) gui.drawString(mc.font, String.valueOf(max), 44, 28, 0x808080, false);
		if (CCG.config.misc.expressionInput) renderPreview(gui);
		GuiGameElement.of(stack).at(16, 24).render(gui);
		MutableComponent title = CCGLang.itemName(stack).component();
		gui.drawString(mc.font, title, (210 - mc.font.width(title)) / 2, 4, 0x303030, false);
		GuiGameElement.of(packageBox).scale(3).at(215, 35, 0).render(gui);
		pose.popPose();
		if (mouseX >= windowXOffset + 16 && mouseX < windowXOffset + 32 && mouseY >= windowYOffset + 24 && mouseY < windowYOffset + 40)
			gui.renderComponentTooltip(mc.font, getTooltipFromItem(mc, stack), mouseX, mouseY);
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
	protected void apply() {
		String text = amountInput.getValue();
		Long value = text.isEmpty() ? Long.valueOf(1) : ExpressionParser.evaluate(text);
		if (value == null) return;
		onApply.accept((int) Mth.clamp(value, 0L, max));
		onClose();
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (amountInput.isHoveredOrFocused()) {
			Long current = ExpressionParser.evaluate(amountInput.getValue());
			long base = current == null ? 0L : current;
			long amount = Mth.clamp(base + (long) (getModifiedScrollAmount() * scrollY), 0L, max);
			amountInput.setValue(Long.toString(amount));
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}
	@Override
	protected void renderMenuBackground(@NotNull GuiGraphics gui, int x, int y, int width, int height) {}
	/** 输入含运算符时在右侧显示求值结果 */
	private void renderPreview(GuiGraphics gui) {
		String text = amountInput.getValue();
		if (text.isEmpty() || text.chars().allMatch(Character::isDigit)) return;
		Long value = ExpressionParser.evaluate(text);
		if (value == null) return;
		String preview = "= " + value;
		int x = 44 + 129 - mc.font.width(preview);
		if (x < 44 + mc.font.width(text) + 4) return;
		gui.drawString(mc.font, preview, x, 28, 0x808080, false);
	}
}
