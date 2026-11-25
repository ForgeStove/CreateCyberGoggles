package io.github.forgestove.create_cyber_goggles.foundation.gui;
import net.minecraft.client.gui.*;
import net.minecraft.world.item.ItemStack;

import java.util.List;
public abstract class GhostItemInteractions {
	private static final double DRAG_THRESHOLD = 4;
	private static final int TARGET_GREEN = 0x4013C90A;
	private static final int HOVER_GREEN = 0x804CC919;
	private ItemStack picked = ItemStack.EMPTY;
	private int pickedCount = 1;
	private int pickedIndex = -1;
	private int pressSource = -1;
	private double pressX;
	private double pressY;
	private boolean dragging;
	protected abstract int hoveredIndex();
	protected abstract ItemStack getStack(int index);
	protected abstract void setStack(int index, ItemStack stack);
	protected abstract List<Integer> amounts();
	protected abstract int slotX(int index);
	protected abstract int slotY(int index);
	protected abstract boolean hasCarriedItem();
	public boolean onPress(double mouseX, double mouseY) {
		if (hasCarriedItem()) return false;
		int ghostIndex = hoveredIndex();
		if (ghostIndex < 0) {
			if (pickedIndex >= 0) {
				putBack();
				return true;
			}
			return false;
		}
		if (pickedIndex >= 0) {
			if (ghostIndex == pickedIndex) putBack();
			else dropToHand(ghostIndex);
			return true;
		}
		pressSource = ghostIndex;
		pressX = mouseX;
		pressY = mouseY;
		dragging = false;
		return true;
	}
	public boolean onRelease() {
		if (pressSource < 0) return false;
		int releaseIndex = hoveredIndex();
		if (dragging) if (releaseIndex >= 0) drop(releaseIndex);
		else putBack();
		else if (releaseIndex == pressSource) {
			ItemStack stack = getStack(pressSource);
			if (!stack.isEmpty()) pickUp(pressSource);
		} else if (releaseIndex >= 0) swapSlots(pressSource, releaseIndex);
		pressSource = -1;
		dragging = false;
		return true;
	}
	public boolean onDrag(double mouseX, double mouseY) {
		if (pressSource < 0 || dragging) return false;
		double dx = mouseX - pressX;
		double dy = mouseY - pressY;
		if (dx * dx + dy * dy >= DRAG_THRESHOLD * DRAG_THRESHOLD) {
			dragging = true;
			pickUp(pressSource);
		}
		return true;
	}
	public void renderPicked(GuiGraphics graphics, Font font, int mouseX, int mouseY) {
		if (pickedIndex < 0) return;
		int x = mouseX - 8;
		int y = mouseY - 8;
		graphics.renderItem(picked, x, y);
		graphics.renderItemDecorations(font, picked, x, y, pickedCount + "");
		if (!dragging) return;
		int sx = slotX(pickedIndex);
		int sy = slotY(pickedIndex);
		graphics.fill(sx, sy, sx + 16, sy + 16, TARGET_GREEN);
		int hoverIndex = hoveredIndex();
		if (hoverIndex >= 0 && hoverIndex != pickedIndex) {
			int hx = slotX(hoverIndex);
			int hy = slotY(hoverIndex);
			graphics.fill(hx, hy, hx + 16, hy + 16, HOVER_GREEN);
		}
	}
	protected void resetDrag() {
		picked = ItemStack.EMPTY;
		pickedCount = 1;
		pickedIndex = -1;
		pressSource = -1;
		dragging = false;
	}
	private void pickUp(int index) {
		picked = getStack(index);
		pickedCount = amounts().get(index);
		pickedIndex = index;
		setStack(index, ItemStack.EMPTY);
		amounts().set(index, 1);
	}
	private void putBack() {
		if (pickedIndex < 0) return;
		setStack(pickedIndex, picked);
		amounts().set(pickedIndex, pickedCount);
		resetDrag();
	}
	private void drop(int dst) {
		ItemStack dstStack = getStack(dst);
		Integer dstCount = amounts().get(dst);
		int srcIndex = pickedIndex;
		setStack(dst, picked);
		amounts().set(dst, pickedCount);
		if (!dstStack.isEmpty()) {
			setStack(srcIndex, dstStack);
			amounts().set(srcIndex, dstCount);
		}
		resetDrag();
	}
	private void dropToHand(int dst) {
		ItemStack dstStack = getStack(dst);
		Integer dstCount = amounts().get(dst);
		setStack(dst, picked);
		amounts().set(dst, pickedCount);
		if (dstStack.isEmpty()) resetDrag();
		else {
			picked = dstStack;
			pickedCount = dstCount;
			pickedIndex = dst;
		}
	}
	private void swapSlots(int src, int dst) {
		ItemStack srcStack = getStack(src);
		ItemStack dstStack = getStack(dst);
		List<Integer> amounts = amounts();
		Integer srcAmount = amounts.get(src);
		Integer dstAmount = amounts.get(dst);
		setStack(src, dstStack);
		setStack(dst, srcStack);
		amounts.set(src, dstAmount);
		amounts.set(dst, srcAmount);
	}
}
