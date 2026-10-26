package io.github.forgestove.create_cyber_goggles.core.factory;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.ExpressionParser;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.function.DoubleConsumer;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
/** 倍数额度输入弹窗：确认后把倍数交给调用方。 */
public class MultiplyScreen extends AbstractSimiScreen {
	private static final int WIDTH = 218;
	private static final int HEIGHT = 79;
	/** 上次成功应用的输入，跨界面保留 */
	private static String lastValue = "2";
	private final Screen parentScreen;
	private final DoubleConsumer onApply;
	private EditBox factorInput;
	public MultiplyScreen(Screen parentScreen, DoubleConsumer onApply) {
		this.parentScreen = parentScreen;
		this.onApply = onApply;
		mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1));
	}
	/** 各界面统一用这个按钮打开弹窗 */
	public static IconButton createButton(int x, int y, DoubleConsumer onApply) {
		var button = new IconButton(x, y, AllIcons.I_MTD_CLOSE);
		button.setToolTip(Component.translatable("create_cyber_goggles.gui.multiplier.tooltip"));
		button.withCallback(() -> mc.setScreen(new MultiplyScreen(mc.screen, onApply)));
		return button;
	}
	public static int scale(int value, double factor, int min, int max) {
		if (value <= 0) return value;
		var result = value * factor;
		if (result >= max) return max;
		if (result <= min) return min;
		return (int) Math.round(result);
	}
	@Override
	protected void init() {
		setWindowSize(WIDTH, HEIGHT);
		var x = (mc.getWindow().getGuiScaledWidth() - WIDTH) / 2;
		var y = (mc.getWindow().getGuiScaledHeight() - HEIGHT) / 2;
		setWindowOffset(x, y);
		super.init();
		var confirmButton = new IconButton(x + 185, y + 55, AllIcons.I_CONFIRM);
		confirmButton.withCallback(this::apply);
		confirmButton.setToolTip(Component.translatable("flexconfig.quit.confirm"));
		var cancelButton = new IconButton(x + 155, y + 55, AllIcons.I_CONFIG_BACK);
		cancelButton.withCallback(this::onClose);
		cancelButton.setToolTip(Component.translatable("gui.cancel"));
		factorInput = new EditBox(mc.font, x + 44, y + 28, 129, 9, Component.empty());
		factorInput.setFilter(CCG.config.misc.expressionInput ? ExpressionParser::isAllowed : value -> value.chars().allMatch(c -> Character.isDigit(c) || c == '.'));
		factorInput.setBordered(false);
		factorInput.setValue(lastValue);
		factorInput.setFocused(true);
		addRenderableWidgets(confirmButton, cancelButton, factorInput);
	}
	protected void apply() {
		var text = factorInput.getValue();
		var factor = ExpressionParser.evaluateDouble(text);
		if (factor == null || factor <= 0) return;
		lastValue = text;
		onApply.accept(factor);
		onClose();
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
	public boolean charTyped(char codePoint, int modifiers) {
		return factorInput.charTyped(codePoint, modifiers);
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
		return factorInput.keyPressed(keyCode, scanCode, modifiers);
	}
	@Override
	protected void renderWindow(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		var pose = gui.pose();
		pose.pushPose();
		pose.translate(windowXOffset, windowYOffset, 0);
		var title = Component.translatable("create_cyber_goggles.gui.multiplier.title");
		gui.drawString(mc.font, title, (210 - mc.font.width(title)) / 2, 4, 0x303030, false);
		AllIcons.I_MTD_CLOSE.render(gui, 16, 24);
		pose.popPose();
	}
	@Override
	protected void renderMenuBackground(@NotNull GuiGraphics gui, int x, int y, int width, int height) {}
	@Override
	protected void renderWindowBackground(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		var pose = gui.pose();
		pose.pushPose();
		pose.translate(0, 0, -500);
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
