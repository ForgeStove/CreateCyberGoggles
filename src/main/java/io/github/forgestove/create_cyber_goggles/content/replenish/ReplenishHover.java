package io.github.forgestove.create_cyber_goggles.content.replenish;
import io.github.forgestove.create_cyber_goggles.content.replenish.ReplenishGroup.Node;
import net.minecraft.world.item.ItemStack;
class ReplenishHover {
	private int mouseX, mouseY;
	private Node node;
	private ItemStack sendItem;
	private Node popup;
	private int popupX, popupY, popupW, popupH;
	private int popupRow = -1;
	void setMouse(int mouseX, int mouseY) {
		this.mouseX = mouseX;
		this.mouseY = mouseY;
	}
	int mouseX() {
		return mouseX;
	}
	int mouseY() {
		return mouseY;
	}
	void clearTargets() {
		node = null;
		sendItem = null;
	}
	Node node() {
		return node;
	}
	void node(Node node) {
		this.node = node;
	}
	ItemStack sendItem() {
		return sendItem;
	}
	void sendItem(ItemStack sendItem) {
		this.sendItem = sendItem;
	}
	Node popup() {
		return popup;
	}
	void popup(Node popup) {
		this.popup = popup;
	}
	void popupRect(int x, int y, int w, int h) {
		popupX = x;
		popupY = y;
		popupW = w;
		popupH = h;
	}
	int popupX() {
		return popupX;
	}
	int popupY() {
		return popupY;
	}
	int popupW() {
		return popupW;
	}
	int popupH() {
		return popupH;
	}
	int popupRow() {
		return popupRow;
	}
	void popupRow(int popupRow) {
		this.popupRow = popupRow;
	}
	void closePopup() {
		popup = null;
		popupRow = -1;
	}
}
