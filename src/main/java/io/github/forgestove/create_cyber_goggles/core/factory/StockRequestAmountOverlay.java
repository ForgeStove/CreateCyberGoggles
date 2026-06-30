package io.github.forgestove.create_cyber_goggles.core.factory;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.awt.Rectangle;
public final class StockRequestAmountOverlay {
	private static final int POPUP_WIDTH = 120, POPUP_HEIGHT = 82, POPUP_PADDING = 8;
	private static final int BUTTON_WIDTH = POPUP_WIDTH / 2 - 7, BUTTON_HEIGHT = 14, BUTTON_Y = POPUP_HEIGHT - BUTTON_HEIGHT - 6;
	private static final int CONFIRM_X = 6, CANCEL_X = POPUP_WIDTH / 2 + 1;
	private static final String PREFIX = CCG.ID + ".screen.stockKeeperRequest.popup.";
	private final Rectangle confirmRect = new Rectangle(BUTTON_WIDTH, BUTTON_HEIGHT);
	private final Rectangle cancelRect = new Rectangle(BUTTON_WIDTH, BUTTON_HEIGHT);
	private final Rectangle popupRect = new Rectangle(POPUP_WIDTH, POPUP_HEIGHT);
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
		open = true;
		this.stack = stack.copyWithCount(1);
		this.max = Math.max(0, max);
		ensureInput(font, popupX, popupY);
		amountInput.setValue(Integer.toString(Mth.clamp(initial, 0, this.max)));
		amountInput.setFocused(true);
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
	public void close() {
		open = false;
		stack = ItemStack.EMPTY;
	}
	public PopupResult mouseClicked(double mouseX, double mouseY, int button, int popupX, int popupY) {
		if (!open) return PopupResult.NONE;
		updateRects(popupX, popupY);
		if (amountInput.mouseClicked(mouseX, mouseY, button)) return PopupResult.CONSUMED;
		if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			if (confirmRect.contains(mouseX, mouseY)) return PopupResult.APPLY;
			if (cancelRect.contains(mouseX, mouseY)) return PopupResult.CLOSE;
		}
		if (!popupRect.contains(mouseX, mouseY)) return PopupResult.CLOSE;
		return PopupResult.CONSUMED;
	}
	private void updateRects(int popupX, int popupY) {
		var buttonY = popupY + BUTTON_Y;
		confirmRect.setLocation(popupX + CONFIRM_X, buttonY);
		cancelRect.setLocation(popupX + CANCEL_X, buttonY);
		popupRect.setLocation(popupX, popupY);
	}
	public void charTyped(char codePoint, int modifiers) {
		if (open) amountInput.charTyped(codePoint, modifiers);
	}
	public PopupResult keyPressed(int keyCode, int scanCode, int modifiers) {
		if (!open) return PopupResult.NONE;
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) return PopupResult.CLOSE;
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) return PopupResult.APPLY;
		amountInput.keyPressed(keyCode, scanCode, modifiers);
		return PopupResult.CONSUMED;
	}
	public int getRequestedAmount() {
		if (!open) return 0;
		try {
			return Mth.clamp(Integer.parseInt(amountInput.getValue()), 0, max);
		} catch (NumberFormatException ignored) {
			return 0;
		}
	}
	public void render(GuiGraphics gui, Font font, int mouseX, int mouseY, float partialTicks, int popupX, int popupY) {
		if (!open) return;
		ensureInput(font, popupX, popupY);
		updateRects(popupX, popupY);
		gui.fill(popupX - 3, popupY - 3, popupX + POPUP_WIDTH + 3, popupY + POPUP_HEIGHT + 3, 0xB0101010);
		gui.fill(popupX, popupY, popupX + POPUP_WIDTH, popupY + POPUP_HEIGHT, 0xE02D2D2D);
		gui.renderOutline(popupX, popupY, POPUP_WIDTH, POPUP_HEIGHT, 0xFF666666);
		gui.drawString(font, Component.translatable(PREFIX + "title"), popupX + POPUP_PADDING, popupY + 6, 0xFFFFFF, false);
		var maxText = max == Integer.MAX_VALUE ? Component.translatable(PREFIX + "infinite") : Component.literal(Integer.toString(max));
		gui.drawString(font, Component.translatable(PREFIX + "max", maxText), popupX + POPUP_PADDING, popupY + 20, 0xC8C8C8, false);
		gui.drawString(font, Component.translatable(PREFIX + "amount"), popupX + POPUP_PADDING, popupY + 34, 0xD8D8D8, false);
		amountInput.render(gui, mouseX, mouseY, partialTicks);
		var confirmColor = confirmRect.contains(mouseX, mouseY) ? 0xFF4A7A4A : 0xFF3A5F3A;
		var cancelColor = cancelRect.contains(mouseX, mouseY) ? 0xFF7A4A4A : 0xFF5F3A3A;
		gui.fill(confirmRect.x, confirmRect.y, confirmRect.x + BUTTON_WIDTH, confirmRect.y + BUTTON_HEIGHT, confirmColor);
		gui.fill(cancelRect.x, cancelRect.y, cancelRect.x + BUTTON_WIDTH, cancelRect.y + BUTTON_HEIGHT, cancelColor);
		gui.drawCenteredString(
			font,
			Component.translatable(PREFIX + "confirm"),
			confirmRect.x + BUTTON_WIDTH / 2,
			confirmRect.y + 3,
			0xFFFFFF
		);
		gui.drawCenteredString(
			font,
			Component.translatable(PREFIX + "cancel"),
			cancelRect.x + BUTTON_WIDTH / 2,
			cancelRect.y + 3,
			0xFFFFFF
		);
	}
	public enum PopupResult {
		NONE,
		CONSUMED,
		APPLY,
		CLOSE
	}
}
