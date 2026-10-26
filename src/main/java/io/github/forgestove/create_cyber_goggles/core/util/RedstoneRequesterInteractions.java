package io.github.forgestove.create_cyber_goggles.core.util;
import com.simibubi.create.content.logistics.AddressEditBox;
import com.simibubi.create.content.logistics.redstoneRequester.*;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterMenu.SorterProofSlot;
import com.simibubi.create.foundation.gui.menu.GhostItemSubmitPacket;
import com.simibubi.create.foundation.gui.widget.IconButton;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.factory.RequestAmountScreen;
import io.github.forgestove.create_cyber_goggles.mixin.accessor.RedstoneRequesterScreenAccessor;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.gui.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
/** 红石请求器界面的扩展交互：ghost 槽抓起/拖拽换位与撤销。 */
public final class RedstoneRequesterInteractions {
	/** 拖动判定阈值（像素） */
	private static final double DRAG_THRESHOLD = 4;
	/** 覆盖色取自 JEI GhostIngredientDrag。 */
	private static final int TARGET_GREEN = 0x4013C90A;
	private static final int HOVER_GREEN = 0x804CC919;
	private final RedstoneRequesterScreen screen;
	/** 打开界面时的快照，供撤销恢复。 */
	private final List<ItemStack> backupStacks = new ArrayList<>();
	private final List<Integer> backupAmounts = new ArrayList<>();
	private String backupAddress = "";
	private boolean backupAllowPartial;
	/** 无未保存更改时置灰。 */
	private IconButton undoButton;
	/** 抓起状态：物品与数量已离开源槽；pickedIndex 为 -1 表示空闲。 */
	private ItemStack picked = ItemStack.EMPTY;
	private int pickedCount = 1;
	private int pickedIndex = -1;
	/** 本次按下命中的幽灵槽，-1 表示未命中。 */
	private int pressSource = -1;
	private double pressX;
	private double pressY;
	private boolean dragging;
	public RedstoneRequesterInteractions(RedstoneRequesterScreen screen) {
		this.screen = screen;
	}
	/** 缓存初始内容并绑定撤销按钮。 */
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
	private List<Integer> amounts() {
		return accessor().getAmounts();
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
	/** 再次 init 时只重绑按钮；不要在这里重置拖拽状态，picked 里的物品会丢。 */
	public void retarget(IconButton undoButton) {
		this.undoButton = undoButton;
	}
	/** 恢复到打开界面时的状态并关闭界面。 */
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
		// 服务端 saveData 会压缩空槽，数量列表需按非空槽顺序提交
		var compressedAmounts = new ArrayList<Integer>();
		for (var i = 0; i < backupStacks.size(); i++)
			if (!backupStacks.get(i).isEmpty()) compressedAmounts.add(backupAmounts.get(i));
		CatnipServices.NETWORK.sendToServer(new RedstoneRequesterConfigurationPacket(
			menu.contentHolder.getBlockPos(),
			backupAddress,
			allow,
			compressedAmounts
		));
		resetDrag();
		screen.onClose();
	}
	private IconButton dontAllowPartial() {
		return accessor().getDontAllowPartial();
	}
	private void resetDrag() {
		picked = ItemStack.EMPTY;
		pickedCount = 1;
		pickedIndex = -1;
		pressSource = -1;
		dragging = false;
	}
	/** 无未保存更改时禁用撤销按钮。 */
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
	/** 拦截幽灵槽左键，避免被 GhostItemMenu 清空。 */
	public boolean onPress(double mouseX, double mouseY) {
		// 手持真实物品时点幽灵槽 → 交给上层正常放置
		if (!screen.getMenu().getCarried().isEmpty()) return false;
		int ghostIndex = hoveredGhostIndex();
		// 抓起中点非幽灵槽 → 放回源槽并拦截
		if (ghostIndex < 0) {
			if (pickedIndex >= 0) {
				putBack();
				return true;
			}
			return false;
		}
		// 抓起中：点源槽放回，点其他幽灵槽换手
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
	/** 松开：同槽拿起、跨槽换位、非幽灵槽取消。 */
	public boolean onRelease() {
		if (pressSource < 0) return false;
		int releaseIndex = hoveredGhostIndex();
		if (dragging) if (releaseIndex >= 0) drop(releaseIndex);
		else putBack();
		else if (releaseIndex == pressSource) {
			ItemStack stack = ghostStack(pressSource);
			if (!stack.isEmpty()) pickUp(pressSource);
		} else if (releaseIndex >= 0) swapSlots(pressSource, releaseIndex);
		pressSource = -1;
		dragging = false;
		return true;
	}
	/** 位移超过阈值后进入拖动，源槽物品跟随指针。 */
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
	private void pickUp(int index) {
		picked = ghostStack(index);
		pickedCount = amounts().get(index);
		pickedIndex = index;
		setGhostSlot(index, ItemStack.EMPTY);
		amounts().set(index, 1);
	}
	private ItemStack ghostStack(int index) {
		return screen.getMenu().ghostInventory.getStackInSlot(index);
	}
	private void setGhostSlot(int index, ItemStack stack) {
		screen.getMenu().ghostInventory.setStackInSlot(index, stack);
		CatnipServices.NETWORK.sendToServer(new GhostItemSubmitPacket(stack, index));
	}
	/** 绘制悬空物品与源槽高亮。 */
	public void renderPicked(GuiGraphics graphics, Font font, int mouseX, int mouseY) {
		if (pickedIndex < 0) return;
		int x = mouseX - 8;
		int y = mouseY - 8;
		graphics.renderItem(picked, x, y);
		graphics.renderItemDecorations(font, picked, x, y, pickedCount + "");
		if (!dragging) return;
		int sx = screen.getGuiLeft() + 27 + pickedIndex * 20;
		int sy = screen.getGuiTop() + 28;
		graphics.fill(sx, sy, sx + 16, sy + 16, TARGET_GREEN);
		int hoverIndex = hoveredGhostIndex();
		if (hoverIndex >= 0 && hoverIndex != pickedIndex) {
			int hx = screen.getGuiLeft() + 27 + hoverIndex * 20;
			int hy = screen.getGuiTop() + 28;
			graphics.fill(hx, hy, hx + 16, hy + 16, HOVER_GREEN);
		}
	}
	/** 指针命中的幽灵槽，-1 表示未命中。 */
	public int hoveredGhostIndex() {
		if (!(screen.getSlotUnderMouse() instanceof SorterProofSlot ghostSlot)) return -1;
		return ghostSlot.getSlotIndex();
	}
	/** 按住 stockRequestSetter 点击幽灵槽时打开数量设置。 */
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
	private void putBack() {
		if (pickedIndex < 0) return;
		setGhostSlot(pickedIndex, picked);
		amounts().set(pickedIndex, pickedCount);
		resetDrag();
	}
	/** 放下：与目标槽交换并结束拿起。 */
	private void drop(int dst) {
		ItemStack dstStack = ghostStack(dst);
		Integer dstCount = amounts().get(dst);
		int srcIndex = pickedIndex;
		setGhostSlot(dst, picked);
		amounts().set(dst, pickedCount);
		if (!dstStack.isEmpty()) {
			setGhostSlot(srcIndex, dstStack);
			amounts().set(srcIndex, dstCount);
		}
		resetDrag();
	}
	/** 放到目标槽并拿起其原物品（换手）。 */
	private void dropToHand(int dst) {
		ItemStack dstStack = ghostStack(dst);
		Integer dstCount = amounts().get(dst);
		setGhostSlot(dst, picked);
		amounts().set(dst, pickedCount);
		if (dstStack.isEmpty()) resetDrag();
		else {
			picked = dstStack;
			pickedCount = dstCount;
			pickedIndex = dst;
		}
	}
	private void swapSlots(int src, int dst) {
		ItemStackHandler ghost = screen.getMenu().ghostInventory;
		ItemStack srcStack = ghost.getStackInSlot(src);
		ItemStack dstStack = ghost.getStackInSlot(dst);
		List<Integer> amounts = amounts();
		Integer srcAmount = amounts.get(src);
		Integer dstAmount = amounts.get(dst);
		setGhostSlot(src, dstStack);
		setGhostSlot(dst, srcStack);
		amounts.set(src, dstAmount);
		amounts.set(dst, srcAmount);
	}
}
