package io.github.forgestove.create_cyber_goggles.core.factory;
import com.google.common.collect.Lists;
import com.simibubi.create.content.logistics.box.PackageStyles;
import com.simibubi.create.foundation.gui.*;
import com.simibubi.create.foundation.gui.widget.IconButton;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.*;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.Consumer;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
public class RequestAmountOverlay extends AbstractContainerEventHandler implements Renderable {
	protected static final int POPUP_WIDTH = 218, POPUP_HEIGHT = 79;
	public final List<Renderable> renderables = Lists.newArrayList();
	protected final List<GuiEventListener> children = Lists.newArrayList();
	protected final IconButton confirmButton;
	protected final IconButton cancelButton;
	protected final ScreenRectangle popupRect;
	protected final EditBox amountInput;
	protected final int x = (mc.getWindow().getGuiScaledWidth() - POPUP_WIDTH) / 2;
	protected final int y = (mc.getWindow().getGuiScaledHeight() - POPUP_HEIGHT) / 2;
	public boolean open;
	protected Consumer<Integer> onApply;
	protected ItemStack stack = ItemStack.EMPTY;
	protected int max = 1;
	protected ItemStack packageBox = ItemStack.EMPTY;
	public RequestAmountOverlay() {
		popupRect = new ScreenRectangle(x, y, POPUP_WIDTH, POPUP_HEIGHT);
		confirmButton = new IconButton(x + 185, y + 55, AllIcons.I_CONFIRM);
		confirmButton.withCallback(this::apply);
		confirmButton.setToolTip(Component.translatable("config.ui.quit.confirm"));
		addRenderableWidget(confirmButton);
		cancelButton = new IconButton(x + 155, y + 55, AllIcons.I_CONFIG_BACK);
		cancelButton.withCallback(this::close);
		cancelButton.setToolTip(Component.translatable("gui.cancel"));
		addRenderableWidget(cancelButton);
		amountInput = new EditBox(mc.font, x + 44, y + 28, 129, 9, Component.translatable("create_cyber_goggles.gui.stockRequest.amount"));
		amountInput.setMaxLength(10);
		amountInput.setFilter(value -> value.chars().allMatch(Character::isDigit));
		amountInput.setBordered(false);
		addRenderableWidget(amountInput);
	}
	protected void apply() {
		var amount = 1;
		try {
			amount = Mth.clamp(Integer.parseInt(amountInput.getValue()), 0, max);
		} catch (NumberFormatException ignored) {}
		onApply.accept(amount);
		close();
	}
	protected <T extends GuiEventListener & Renderable & NarratableEntry> void addRenderableWidget(T widget) {
		renderables.add(widget);
		children.add(widget);
	}
	public void close() {
		open = false;
		stack = ItemStack.EMPTY;
	}
	public void open(ItemStack stack, int initial, int max, Consumer<Integer> onApply) {
		this.onApply = onApply;
		open = true;
		this.stack = stack.copyWithCount(1);
		this.max = Math.max(0, max);
		amountInput.setValue(initial > 1 ? Integer.toString(Mth.clamp(initial, 0, this.max)) : "");
		amountInput.setFocused(true);
		packageBox = PackageStyles.getRandomBox();
		mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1));
	}
	@Override
	public @NotNull List<? extends GuiEventListener> children() {
		return children;
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!open) return false;
		if (amountInput.mouseClicked(mouseX, mouseY, button)) return true;
		if (confirmButton.mouseClicked(mouseX, mouseY, button)) return true;
		if (cancelButton.mouseClicked(mouseX, mouseY, button)) return true;
		if (!getRectangle().containsPoint((int) mouseX, (int) mouseY)) {
			close();
			mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
			return true;
		}
		return false;
	}
	@Override
	public @NotNull ScreenRectangle getRectangle() {
		return popupRect;
	}
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}
	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		return open ? amountInput.charTyped(codePoint, modifiers) : super.charTyped(codePoint, modifiers);
	}
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (!open) return super.keyReleased(keyCode, scanCode, modifiers);
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			close();
			mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
			return super.keyReleased(keyCode, scanCode, modifiers);
		}
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			apply();
			return super.keyReleased(keyCode, scanCode, modifiers);
		}
		return amountInput.keyPressed(keyCode, scanCode, modifiers);
	}
	@Override
	public void render(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		if (!open) return;
		var font = mc.font;
		var pose = gui.pose();
		pose.pushPose();
		pose.translate(0, 0, 500);
		AllGuiTextures.PACKAGE_FILTER.render(gui, x, y);
		if (!stack.isEmpty()) gui.renderItem(stack, x + 16, y + 24);
		var title = Component.translatable("create_cyber_goggles.gui.request.title");
		gui.drawString(font, title, x + (210 - font.width(title)) / 2, y + 4, 0x303030, false);
		renderables.forEach(renderable -> renderable.render(gui, mouseX, mouseY, partialTick));
		if (!packageBox.isEmpty()) GuiGameElement.of(packageBox).scale(3).at(x + 215, y + 35, 0).render(gui);
		pose.popPose();
	}
}
