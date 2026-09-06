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

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
/**
 * 红石请求器界面（RedstoneRequesterScreen）的扩展交互：ghost 槽抓起/拖拽换位、初始内容缓存与撤销。
 * 通过 screen 的 public 方法 + {@link RedstoneRequesterScreenAccessor} 访问受保护/私有字段，与 mixin 解耦。
 */
public final class RedstoneRequesterInteractions {
	/** 判定为拖动的位移阈值（像素） */
	private static final double DRAG_THRESHOLD = 4;
	/** 拖拽槽位覆盖层颜色，复用 JEI GhostIngredientDrag：目标绿（未悬停）与悬停绿（更亮） */
	private static final int TARGET_GREEN = 0x4013C90A;
	private static final int HOVER_GREEN = 0x804CC919;
	private final RedstoneRequesterScreen screen;
	/** 打开界面时缓存的初始 ghost 槽内容，供撤销恢复 */
	private final List<ItemStack> backupStacks = new ArrayList<>();
	private final List<Integer> backupAmounts = new ArrayList<>();
	/** 打开界面时缓存的地址与「允许部分请求」开关 */
	private String backupAddress = "";
	private boolean backupAllowPartial;
	/** 撤销按钮引用，用于动态置灰（无更改时不可用） */
	private IconButton undoButton;
	/** 抓取式「拿起」：物品与数量已从源槽移除、悬空待放。pickedIndex=-1 表示空闲 */
	private ItemStack picked = ItemStack.EMPTY;
	private int pickedCount = 1;
	private int pickedIndex = -1;
	/** 单次「按下→松开」手势中，按下时命中的幽灵槽索引；-1 表示按下不在幽灵槽上 */
	private int pressSource = -1;
	/** 按下时的绝对坐标与是否已判定为拖动（移动超过阈值） */
	private double pressX;
	private double pressY;
	private boolean dragging;
	public RedstoneRequesterInteractions(RedstoneRequesterScreen screen) {
		this.screen = screen;
	}
	/** 首次进入时缓存初始内容并绑定撤销按钮 */
	public void init(IconButton undoButton) {
		this.undoButton = undoButton;
		var menu = screen.getMenu();
		backupStacks.clear();
		backupAmounts.clear();
		for (var i = 0; i < menu.ghostInventory.getSlots(); i++) {
			backupStacks.add(menu.ghostInventory.getStackInSlot(i).copy());
			backupAmounts.add(amounts().get(i));
		}
		backupAddress = addressBox().getValue();
		backupAllowPartial = allowPartial().green;
		undoButton.active = false; // 刚打开时无更改，撤销不可用
	}
	/**
	 * 从其他屏返回或窗口重排再次 init 时只重绑重建的撤销按钮，不得覆盖已缓存的初始内容。
	 * 注意：不能在此 resetDrag()——pickUp 已把源槽清空、物品暂存 picked，直接重置会让物品凭空丢失。
	 */
	public void retarget(IconButton undoButton) {
		this.undoButton = undoButton;
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
	/** 撤销：恢复打开界面时缓存的 ghost 槽、数量、地址与「允许部分」开关，并清空拖拽/拿起状态 */
	public void undo() {
		var menu = screen.getMenu();
		var ghost = menu.ghostInventory;
		// 恢复本地 ghost 槽物品，并逐槽同步服务端（更新服务端 ghostInventory，saveData 会重编码 request）
		for (var i = 0; i < ghost.getSlots(); i++) {
			var stack = backupStacks.get(i).copy();
			ghost.setStackInSlot(i, stack);
			CatnipServices.NETWORK.sendToServer(new GhostItemSubmitPacket(stack, i));
		}
		// 恢复本地数量
		var amounts = amounts();
		for (var i = 0; i < amounts.size(); i++)
			amounts.set(i, backupAmounts.get(i));
		// 恢复地址与「允许部分」开关的本地显示
		addressBox().setValue(backupAddress);
		var allow = backupAllowPartial;
		allowPartial().green = allow;
		dontAllowPartial().green = !allow;
		// 提交地址、allowPartial、数量到服务端。注意：saveData 会压缩掉空槽，
		// 故数量列表需与「非空槽顺序」对齐（不含空槽），写成压缩后的列表。
		var compressedAmounts = new ArrayList<Integer>();
		for (var i = 0; i < backupStacks.size(); i++)
			if (!backupStacks.get(i).isEmpty()) compressedAmounts.add(backupAmounts.get(i));
		CatnipServices.NETWORK.sendToServer(new RedstoneRequesterConfigurationPacket(
			menu.contentHolder.getBlockPos(),
			backupAddress,
			allow,
			compressedAmounts
		));
		// 清空拖拽/拿起状态
		resetDrag();
		// 撤销完成 → 关闭屏幕，让服务端按新配置收尾
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
	/** 每帧评估是否有未保存的更改，决定撤销按钮是否可用 */
	public void tickUndoActive() {
		if (undoButton != null) undoButton.active = isDirty();
	}
	/** 当前内容是否与打开界面时缓存的初始内容不同（不同则撤销可用） */
	private boolean isDirty() {
		var menu = screen.getMenu();
		var amounts = amounts();
		for (var i = 0; i < menu.ghostInventory.getSlots(); i++)
			if (!ItemStack.matches(menu.ghostInventory.getStackInSlot(i), backupStacks.get(i))) return true;
		for (var i = 0; i < amounts.size(); i++)
			if (!Objects.equals(amounts.get(i), backupAmounts.get(i))) return true;
		if (!Objects.equals(addressBox().getValue(), backupAddress)) return true;
		return allowPartial().green != backupAllowPartial;
	}
	/** 幽灵槽左键按下：拦截避免被 GhostItemMenu 清空。记录按下源和坐标，供拖动判定 */
	public boolean onPress(double mouseX, double mouseY) {
		// 手持真实物品时点幽灵槽 → 交给上层正常放置（GhostItemMenu.clicked 持物放入），不做拖拽
		if (!screen.getMenu().getCarried().isEmpty()) return false;
		var ghostIndex = hoveredGhostIndex();
		// 抓取中点到非幽灵槽 → 取消（放回源槽），拦截该次点击，避免其作用于背包/清空
		if (ghostIndex < 0) {
			if (pickedIndex >= 0) {
				putBack();
				return true;
			}
			return false;
		}
		// 抓起中：点回源槽→放回；点另一幽灵槽→指针与目标槽换手（继续拿取）
		if (pickedIndex >= 0) {
			if (ghostIndex == pickedIndex) putBack();
			else dropToHand(ghostIndex);
			return true;
		}
		// 空闲按下 → 记录源和坐标，等待 mouseDragged/mouseReleased 判定单击或拖动
		pressSource = ghostIndex;
		pressX = mouseX;
		pressY = mouseY;
		dragging = false;
		return true;
	}
	/** 幽灵槽左键松开：拖动中落下/放回；空闲单击同槽=拿起、跨槽=换位、落非幽灵槽=取消 */
	public boolean onRelease() {
		// 非我方手势（如抓取中点上一次的尾随释放）→ 交给上层
		if (pressSource < 0) return false;
		var releaseIndex = hoveredGhostIndex();
		// 单击：同槽=拿起；跨槽=换位；非幽灵槽=取消
		// 长按拖动：pickUp 已把源槽物品拿在手里，落幽灵槽=放下/交换，落非幽灵槽=放回源槽
		if (dragging) if (releaseIndex >= 0) drop(releaseIndex);
		else putBack();
		else if (releaseIndex == pressSource) {
			var stack = ghostStack(pressSource);
			if (!stack.isEmpty()) pickUp(pressSource);
		} else if (releaseIndex >= 0) swapSlots(pressSource, releaseIndex);
		pressSource = -1;
		dragging = false;
		return true;
	}
	/** 按住移动超过阈值即判定为拖动：源槽物品移除（指针跟随）并显示源槽绿框 */
	public boolean onDrag(double mouseX, double mouseY) {
		if (pressSource < 0 || dragging) return false;
		var dx = mouseX - pressX;
		var dy = mouseY - pressY;
		if (dx * dx + dy * dy >= DRAG_THRESHOLD * DRAG_THRESHOLD) {
			dragging = true;
			pickUp(pressSource); // 源槽物品移到指针、源槽清空
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
	/** 绘制悬空物品与源槽高亮（屏幕坐标）。由 mixin 传入 font */
	public void renderPicked(GuiGraphics graphics, Font font, int mouseX, int mouseY) {
		if (pickedIndex < 0) return;
		var x = mouseX - 8;
		var y = mouseY - 8;
		graphics.renderItem(picked, x, y);
		graphics.renderItemDecorations(font, picked, x, y, pickedCount + "");
		if (!dragging) return;
		// 源槽覆盖 JEI 目标绿（物品已拿走，仅位置提示）
		var sx = screen.getGuiLeft() + 27 + pickedIndex * 20;
		var sy = screen.getGuiTop() + 28;
		graphics.fill(sx, sy, sx + 16, sy + 16, TARGET_GREEN);
		// 指针命中的幽灵槽（非源槽）叠加更亮的悬停绿，提示落点
		var hoverIndex = hoveredGhostIndex();
		if (hoverIndex >= 0 && hoverIndex != pickedIndex) {
			var hx = screen.getGuiLeft() + 27 + hoverIndex * 20;
			var hy = screen.getGuiTop() + 28;
			graphics.fill(hx, hy, hx + 16, hy + 16, HOVER_GREEN);
		}
	}
	/** 当前指针命中的幽灵槽索引，未命中返回 -1 */
	public int hoveredGhostIndex() {
		if (!(screen.getSlotUnderMouse() instanceof SorterProofSlot ghostSlot)) return -1;
		return ghostSlot.getSlotIndex();
	}
	/** 按住 stockRequestSetter 时点击幽灵槽 → 弹出数量设置界面 */
	public boolean openPopupForHoveredSlot() {
		if (!(screen.getSlotUnderMouse() instanceof SorterProofSlot ghostSlot)) return false;
		var index = ghostSlot.getSlotIndex();
		var stack = ghostSlot.getItem();
		if (stack.isEmpty()) return false;
		var amounts = amounts();
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
	/** 拖动结束：源槽与目标槽真实交换（源槽物品已在指针/picked，目标原物品回源槽），结束拿取 */
	private void drop(int dst) {
		var dstStack = ghostStack(dst);
		var dstCount = amounts().get(dst);
		var srcIndex = pickedIndex;
		setGhostSlot(dst, picked);
		amounts().set(dst, pickedCount);
		if (!dstStack.isEmpty()) {
			// 目标非空 → 交换：dst 原物品放回源槽
			setGhostSlot(srcIndex, dstStack);
			amounts().set(srcIndex, dstCount);
		}
		resetDrag();
	}
	/** 抓起后点另一幽灵槽：把指针物品放目标，目标原物品拿进手（换手，继续拿取） */
	private void dropToHand(int dst) {
		var dstStack = ghostStack(dst);
		var dstCount = amounts().get(dst);
		setGhostSlot(dst, picked);
		amounts().set(dst, pickedCount);
		if (dstStack.isEmpty()) resetDrag(); // 目标空 → 放下结束
		else {
			picked = dstStack; // 目标原物品拿进手，继续拿取
			pickedCount = dstCount;
			pickedIndex = dst;
		}
	}
	private void swapSlots(int src, int dst) {
		var ghost = screen.getMenu().ghostInventory;
		var srcStack = ghost.getStackInSlot(src);
		var dstStack = ghost.getStackInSlot(dst);
		var amounts = amounts();
		var srcAmount = amounts.get(src);
		var dstAmount = amounts.get(dst);
		setGhostSlot(src, dstStack);
		setGhostSlot(dst, srcStack);
		amounts.set(src, dstAmount);
		amounts.set(dst, srcAmount);
	}
}
