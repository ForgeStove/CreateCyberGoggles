package io.github.forgestove.create_cyber_goggles.content.redstoneRequester;
import com.simibubi.create.content.logistics.AddressEditBox;
import com.simibubi.create.content.logistics.redstoneRequester.*;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterMenu.SorterProofSlot;
import com.simibubi.create.foundation.gui.menu.GhostItemSubmitPacket;
import com.simibubi.create.foundation.gui.widget.IconButton;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.content.factoryPanel.RequestAmountScreen;
import io.github.forgestove.create_cyber_goggles.foundation.gui.GhostItemInteractions;
import io.github.forgestove.create_cyber_goggles.mixin.accessor.RedstoneRequesterScreenAccessor;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.foundation.util.CCGUtil.mc;
public class RedstoneRequesterInteractions extends GhostItemInteractions {
	private final RedstoneRequesterScreen screen;
	private final List<ItemStack> backupStacks = new ArrayList<>();
	private final List<Integer> backupAmounts = new ArrayList<>();
	private String backupAddress = "";
	private boolean backupAllowPartial;
	private IconButton undoButton;
	public RedstoneRequesterInteractions(RedstoneRequesterScreen screen) {
		this.screen = screen;
	}
	public void init(IconButton undoButton) {
		this.undoButton = undoButton;
		RedstoneRequesterMenu menu = screen.getMenu();
		backupStacks.clear();
		backupAmounts.clear();
		for (var i = 0; i < menu.ghostInventory.getSlots(); i++) {
			backupStacks.add(menu.ghostInventory.getStackInSlot(i).copy());
			backupAmounts.add(amounts().get(i));
		}
		backupAddress = addressBox().getValue();
		backupAllowPartial = allowPartial().green;
		undoButton.active = false;
	}
	@Override
	protected int hoveredIndex() {
		if (!(screen.getSlotUnderMouse() instanceof SorterProofSlot ghostSlot)) return -1;
		return ghostSlot.getSlotIndex();
	}
	private AddressEditBox addressBox() {
		return accessor().getAddressBox();
	}
	private IconButton allowPartial() {
		return accessor().getAllowPartial();
	}
	private RedstoneRequesterScreenAccessor accessor() {
		return (RedstoneRequesterScreenAccessor) screen;
	}
	public void retarget(IconButton undoButton) {
		this.undoButton = undoButton;
	}
	public void undo() {
		RedstoneRequesterMenu menu = screen.getMenu();
		ItemStackHandler ghost = menu.ghostInventory;
		for (var i = 0; i < ghost.getSlots(); i++) {
			ItemStack stack = backupStacks.get(i).copy();
			ghost.setStackInSlot(i, stack);
			CatnipServices.NETWORK.sendToServer(new GhostItemSubmitPacket(stack, i));
		}
		List<Integer> amounts = amounts();
		for (var i = 0; i < amounts.size(); i++)
			amounts.set(i, backupAmounts.get(i));
		addressBox().setValue(backupAddress);
		boolean allow = backupAllowPartial;
		allowPartial().green = allow;
		dontAllowPartial().green = !allow;
		resetDrag();
		screen.onClose();
	}
	private IconButton dontAllowPartial() {
		return accessor().getDontAllowPartial();
	}
	public void tickUndoActive() {
		if (undoButton != null) undoButton.active = isDirty();
	}
	private boolean isDirty() {
		RedstoneRequesterMenu menu = screen.getMenu();
		List<Integer> amounts = amounts();
		for (var i = 0; i < menu.ghostInventory.getSlots(); i++)
			if (!ItemStack.matches(menu.ghostInventory.getStackInSlot(i), backupStacks.get(i))) return true;
		for (var i = 0; i < amounts.size(); i++)
			if (!Objects.equals(amounts.get(i), backupAmounts.get(i))) return true;
		if (!Objects.equals(addressBox().getValue(), backupAddress)) return true;
		return allowPartial().green != backupAllowPartial;
	}
	public boolean openPopupForHoveredSlot() {
		if (!(screen.getSlotUnderMouse() instanceof SorterProofSlot ghostSlot)) return false;
		int index = ghostSlot.getSlotIndex();
		ItemStack stack = ghostSlot.getItem();
		if (stack.isEmpty()) return false;
		List<Integer> amounts = amounts();
		mc.setScreen(new RequestAmountScreen(
			screen,
			stack,
			amounts.get(index),
			CCG.config.misc.removeRequestLimit ? Integer.MAX_VALUE : 256,
			count -> amounts.set(index, count)
		));
		return true;
	}
	@Override
	protected ItemStack getStack(int index) {
		return screen.getMenu().ghostInventory.getStackInSlot(index);
	}
	@Override
	protected void setStack(int index, ItemStack stack) {
		screen.getMenu().ghostInventory.setStackInSlot(index, stack);
		CatnipServices.NETWORK.sendToServer(new GhostItemSubmitPacket(stack, index));
	}
	@Override
	protected List<Integer> amounts() {
		return accessor().getAmounts();
	}
	@Override
	protected int slotX(int index) {
		return screen.getGuiLeft() + 27 + index * 20;
	}
	@Override
	protected int slotY(int index) {
		return screen.getGuiTop() + 28;
	}
	@Override
	protected boolean hasCarriedItem() {
		return !screen.getMenu().getCarried().isEmpty();
	}
}
