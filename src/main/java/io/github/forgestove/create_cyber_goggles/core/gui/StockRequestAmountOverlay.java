package io.github.forgestove.create_cyber_goggles.core.gui;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;
public final class StockRequestAmountOverlay {
	private static final int POPUP_WIDTH = 120;
	private static final int POPUP_HEIGHT = 82;
	private static final int POPUP_PADDING = 8;
	private static final int BUTTON_BOTTOM = 6;
	private static final int BUTTON_HEIGHT = 14;
	private static final String PREFIX = CCG.ID + ".screen.stockKeeperRequest.popup.";
	private boolean open;
	private EditBox amountInput;
	private ItemStack stack = ItemStack.EMPTY;
	private int max = 1;
	public boolean isOpen() {
		return open;
	}
	public ItemStack getStack() {
		return stack;
	}
	public void open(ItemStack stack, int initial, int max, Font font, int popupX, int popupY) {
		this.open = true;
		this.stack = stack.copyWithCount(1);
		this.max = Math.max(0, max);
		ensureInput(font, popupX, popupY);
		amountInput.setValue(Integer.toString(Mth.clamp(initial, 0, this.max)));
		amountInput.setFocused(true);
	}
	public void close() {
		open = false;
		stack = ItemStack.EMPTY;
	}
	public void relayout(Font font, int popupX, int popupY) {
		if (!open) return;
		ensureInput(font, popupX, popupY);
	}
	public ClickResult mouseClicked(double mouseX, double mouseY, int button, int popupX, int popupY) {
		if (!open) return ClickResult.NONE;
		if (amountInput != null && amountInput.mouseClicked(mouseX, mouseY, button)) return ClickResult.CONSUMED;
		if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			if (isOnConfirm(mouseX, mouseY, popupX, popupY)) return ClickResult.APPLY;
			if (isOnCancel(mouseX, mouseY, popupX, popupY)) return ClickResult.CLOSE;
		}
		if (!isInside(mouseX, mouseY, popupX, popupY)) return ClickResult.CLOSE;
		return ClickResult.CONSUMED;
	}
	public void charTyped(char codePoint, int modifiers) {
		if (!open) return;
		if (amountInput != null) amountInput.charTyped(codePoint, modifiers);
	}
	public KeyResult keyPressed(int keyCode, int scanCode, int modifiers) {
		if (!open) return KeyResult.NONE;
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) return KeyResult.CLOSE;
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) return KeyResult.APPLY;
		if (amountInput != null) amountInput.keyPressed(keyCode, scanCode, modifiers);
		return KeyResult.CONSUMED;
	}
	public int getRequestedAmount() {
		if (amountInput != null) try {
			return Mth.clamp(Integer.parseInt(amountInput.getValue()), 0, max);
		} catch (NumberFormatException ignored) {
			return 0;
		}
		return 0;
	}
	public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY, float partialTicks, int popupX, int popupY) {
		if (!open) return;
		relayout(font, popupX, popupY);
		var confirmX = popupX + 6;
		var cancelX = popupX + POPUP_WIDTH / 2 + 1;
		var buttonY = popupY + POPUP_HEIGHT - BUTTON_HEIGHT - BUTTON_BOTTOM;
		var buttonW = POPUP_WIDTH / 2 - 7;
		graphics.fill(popupX - 3, popupY - 3, popupX + POPUP_WIDTH + 3, popupY + POPUP_HEIGHT + 3, 0xB0101010);
		graphics.fill(popupX, popupY, popupX + POPUP_WIDTH, popupY + POPUP_HEIGHT, 0xE02D2D2D);
		graphics.renderOutline(popupX, popupY, POPUP_WIDTH, POPUP_HEIGHT, 0xFF666666);
		graphics.drawString(font, Component.translatable(PREFIX + "title"), popupX + POPUP_PADDING, popupY + 6, 0xFFFFFF, false);
		Component maxText;
		maxText = max == Integer.MAX_VALUE ? Component.translatable(PREFIX + "infinite") : Component.literal(Integer.toString(max));
		graphics.drawString(font, Component.translatable(PREFIX + "max", maxText), popupX + POPUP_PADDING, popupY + 20, 0xC8C8C8, false);
		graphics.drawString(font, Component.translatable(PREFIX + "amount"), popupX + POPUP_PADDING, popupY + 34, 0xD8D8D8, false);
		if (amountInput != null) amountInput.render(graphics, mouseX, mouseY, partialTicks);
		var confirmColor = isOnConfirm(mouseX, mouseY, popupX, popupY) ? 0xFF4A7A4A : 0xFF3A5F3A;
		var cancelColor = isOnCancel(mouseX, mouseY, popupX, popupY) ? 0xFF7A4A4A : 0xFF5F3A3A;
		graphics.fill(confirmX, buttonY, confirmX + buttonW, buttonY + BUTTON_HEIGHT, confirmColor);
		graphics.fill(cancelX, buttonY, cancelX + buttonW, buttonY + BUTTON_HEIGHT, cancelColor);
		graphics.drawCenteredString(font, Component.translatable(PREFIX + "confirm"), confirmX + buttonW / 2, buttonY + 3, 0xFFFFFF);
		graphics.drawCenteredString(font, Component.translatable(PREFIX + "cancel"), cancelX + buttonW / 2, buttonY + 3, 0xFFFFFF);
	}
	private void ensureInput(Font font, int popupX, int popupY) {
		if (amountInput == null) {
			amountInput = new EditBox(font, 0, 0, POPUP_WIDTH - 16, 12, Component.translatable(PREFIX + "amount"));
			amountInput.setMaxLength(9);
			amountInput.setFilter(value -> value.chars().allMatch(Character::isDigit));
		}
		amountInput.setX(popupX + POPUP_PADDING);
		amountInput.setY(popupY + 45);
	}
	private boolean isInside(double mouseX, double mouseY, int popupX, int popupY) {
		return mouseX >= popupX && mouseX < popupX + POPUP_WIDTH && mouseY >= popupY && mouseY < popupY + POPUP_HEIGHT;
	}
	private boolean isOnConfirm(double mouseX, double mouseY, int popupX, int popupY) {
		var x = popupX + 6;
		var y = popupY + POPUP_HEIGHT - BUTTON_HEIGHT - BUTTON_BOTTOM;
		var w = POPUP_WIDTH / 2 - 7;
		return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + BUTTON_HEIGHT;
	}
	private boolean isOnCancel(double mouseX, double mouseY, int popupX, int popupY) {
		var x = popupX + POPUP_WIDTH / 2 + 1;
		var y = popupY + POPUP_HEIGHT - BUTTON_HEIGHT - BUTTON_BOTTOM;
		var w = POPUP_WIDTH / 2 - 7;
		return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + BUTTON_HEIGHT;
	}
	public enum ClickResult {
		NONE,
		CONSUMED,
		APPLY,
		CLOSE
	}
	public enum KeyResult {
		NONE,
		CONSUMED,
		APPLY,
		CLOSE
	}
}
