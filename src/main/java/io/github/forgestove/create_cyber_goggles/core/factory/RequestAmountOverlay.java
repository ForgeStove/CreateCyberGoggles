package io.github.forgestove.create_cyber_goggles.core.factory;
import com.simibubi.create.content.logistics.box.PackageStyles;
import com.simibubi.create.foundation.gui.*;
import com.simibubi.create.foundation.gui.widget.IconButton;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.awt.Rectangle;
import java.util.function.Consumer;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
public final class RequestAmountOverlay implements Renderable {
	private static final int POPUP_WIDTH = 218, POPUP_HEIGHT = 79;
	private final Rectangle popupRect = new Rectangle(POPUP_WIDTH, POPUP_HEIGHT);
	private final IconButton confirmButton;
	private final IconButton abortButton;
	private final int x = (mc.getWindow().getGuiScaledWidth() - POPUP_WIDTH) / 2;
	private final int y = (mc.getWindow().getGuiScaledHeight() - POPUP_HEIGHT) / 2;
	private Consumer<Integer> onApply;
	private boolean open;
	private EditBox amountInput;
	private ItemStack stack = ItemStack.EMPTY;
	private int max = 1;
	private ItemStack packageBox = ItemStack.EMPTY;
	public RequestAmountOverlay() {
		confirmButton = new IconButton(0, 0, AllIcons.I_CONFIRM);
		confirmButton.withCallback(this::apply);
		confirmButton.setToolTip(Component.translatable("config.ui.quit.confirm"));
		abortButton = new IconButton(0, 0, AllIcons.I_CONFIG_BACK);
		abortButton.withCallback(this::close);
		abortButton.setToolTip(Component.translatable("gui.cancel"));
	}
	private void apply() {
		var amount = 0;
		try {
			amount = Mth.clamp(Integer.parseInt(amountInput.getValue()), 0, max);
		} catch (NumberFormatException ignored) {
		}
		onApply.accept(amount);
		close();
	}
	public void close() {
		open = false;
		stack = ItemStack.EMPTY;
	}
	public boolean isOpen() {
		return open;
	}
	public ItemStack getStack() {
		return stack;
	}
	public void open(ItemStack stack, int initial, int max, Font font, Consumer<Integer> onApply) {
		this.onApply = onApply;
		open = true;
		this.stack = stack.copyWithCount(1);
		this.max = Math.max(0, max);
		ensureInput(font);
		amountInput.setValue(Integer.toString(Mth.clamp(initial, 0, this.max)));
		amountInput.setFocused(true);
		confirmButton.setX(x + 185);
		confirmButton.setY(y + 55);
		abortButton.setX(x + 155);
		abortButton.setY(y + 55);
		packageBox = PackageStyles.getRandomBox();
		mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1));
	}
	private void ensureInput(Font font) {
		if (amountInput == null) {
			amountInput = new EditBox(font, 0, 0, 129, 9, Component.translatable("create_cyber_goggles.gui.stockRequest.amount"));
			amountInput.setMaxLength(9);
			amountInput.setFilter(value -> value.chars().allMatch(Character::isDigit));
			amountInput.setBordered(false);
		}
		amountInput.setPosition(x + 44, y + 28);
	}
	public void mouseClicked(double mouseX, double mouseY, int button) {
		if (!open) return;
		if (amountInput.mouseClicked(mouseX, mouseY, button)) return;
		if (confirmButton.mouseClicked(mouseX, mouseY, button)) return;
		if (abortButton.mouseClicked(mouseX, mouseY, button)) return;
		if (!popupRect.contains(mouseX, mouseY)) close();
	}
	public void charTyped(char codePoint, int modifiers) {
		if (open) amountInput.charTyped(codePoint, modifiers);
	}
	public void keyPressed(int keyCode, int scanCode, int modifiers) {
		if (!open) return;
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			close();
			return;
		}
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			apply();
			return;
		}
		amountInput.keyPressed(keyCode, scanCode, modifiers);
	}
	@Override
	public void render(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		if (!open) return;
		var font = mc.font;
		ensureInput(font);
		popupRect.setLocation(x, y);
		AllGuiTextures.PACKAGE_FILTER.render(gui, x, y);
		if (!stack.isEmpty()) gui.renderItem(stack, x + 16, y + 24);
		var title = Component.translatable("create_cyber_goggles.gui.stockRequest.title");
		gui.drawString(font, title, x + (210 - font.width(title)) / 2, y + 4, 0x303030, false);
		amountInput.render(gui, mouseX, mouseY, partialTick);
		confirmButton.render(gui, mouseX, mouseY, partialTick);
		abortButton.render(gui, mouseX, mouseY, partialTick);
		// 确认按钮右侧渲染随机外观的包裹实体
		if (!packageBox.isEmpty()) GuiGameElement.of(packageBox).scale(3).at(x + 215, y + 35, 0).render(gui);
	}
}
