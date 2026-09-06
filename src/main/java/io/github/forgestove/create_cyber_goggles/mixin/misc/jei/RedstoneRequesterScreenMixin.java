package io.github.forgestove.create_cyber_goggles.mixin.misc.jei;
import com.simibubi.create.content.logistics.redstoneRequester.*;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterMenu.SorterProofSlot;
import com.simibubi.create.content.logistics.stockTicker.LogisticalStockRequestPacket;
import com.simibubi.create.foundation.gui.menu.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.compat.jei.ScreenReferenced;
import io.github.forgestove.create_cyber_goggles.core.event.CCGKey;
import io.github.forgestove.create_cyber_goggles.core.factory.RequestAmountScreen;
import io.github.forgestove.create_cyber_goggles.core.util.contract.Self;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
@Mixin(RedstoneRequesterScreen.class)
public abstract class RedstoneRequesterScreenMixin extends AbstractSimiContainerScreen<RedstoneRequesterMenu>
	implements Self<RedstoneRequesterScreen> {
	/** 判定为拖动的位移阈值（像素） */
	@Unique private static final double DRAG_THRESHOLD = 4;
	/** 拖拽槽位覆盖层颜色，复用 JEI GhostIngredientDrag：目标绿（未悬停）与悬停绿（更亮） */
	@Unique private static final int TARGET_GREEN = 0x4013C90A;
	@Unique private static final int HOVER_GREEN = 0x804CC919;
	@Shadow private List<Integer> amounts;
	/** 抓取式「拿起」：物品与数量已从源槽移除、悬空待放。pickedIndex=-1 表示空闲 */
	@Unique private ItemStack ccg$picked = ItemStack.EMPTY;
	@Unique private int ccg$pickedCount = 1;
	@Unique private int ccg$pickedIndex = -1;
	/** 单次「按下→松开」手势中，按下时命中的幽灵槽索引；-1 表示按下不在幽灵槽上 */
	@Unique private int ccg$pressSource = -1;
	/** 按下时的绝对坐标与是否已判定为拖动（移动超过阈值） */
	@Unique private double ccg$pressX;
	@Unique private double ccg$pressY;
	@Unique private boolean ccg$dragging;
	public RedstoneRequesterScreenMixin(RedstoneRequesterMenu container, Inventory inv, Component title) {
		super(container, inv, title);
	}
	/** 打开界面时把当前 Screen 关联到菜单，供 JEI 转移读取 */
	@Inject(method = "init", at = @At("HEAD"))
	private void linkScreen(CallbackInfo ci) {
		var requesterMenu = thiz().getMenu();
		((ScreenReferenced) requesterMenu).ccg$setScreenReference(thiz());
		// 打开界面时请求一次网络库存，供 JEI 转移按库存选择原料
		if (CCG.config.misc.jei.redstoneRequesterJEIRequest && requesterMenu.contentHolder != null)
			CatnipServices.NETWORK.sendToServer(new LogisticalStockRequestPacket(requesterMenu.contentHolder.getBlockPos()));
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!CCG.config.misc.quickRequestActions) return super.mouseClicked(mouseX, mouseY, button);
		if (CCGKey.stockRequestSetter.isDown() && ccg$openPopupForHoveredSlot()) return true;
		if (button == 0 && ccg$handleGhostSlotPress(mouseX, mouseY)) return true;
		if (button == 0 && ccg$pickedIndex >= 0) {
			// 抓取中点到非幽灵槽 → 取消（放回源槽），并拦截，避免落向背包/清空
			ccg$putBack();
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (!CCG.config.misc.quickRequestActions) return super.mouseReleased(mouseX, mouseY, button);
		if (button == 0 && ccg$handleGhostSlotRelease()) return true;
		return super.mouseReleased(mouseX, mouseY, button);
	}
	/** 幽灵槽左键松开：拖动中落下/放回；空闲单击同槽=拿起、跨槽=换位、落非幽灵槽=取消 */
	@Unique
	private boolean ccg$handleGhostSlotRelease() {
		// 非我方手势（如抓取中点上一次的尾随释放）→ 交给上层
		if (ccg$pressSource < 0) return false;
		var releaseIndex = ccg$hoveredGhostIndex();
		// 单击：同槽=拿起；跨槽=换位；非幽灵槽=取消
		// 长按拖动：pickUp 已把源槽物品拿在手里，落幽灵槽=放下/交换，落非幽灵槽=放回源槽
		if (ccg$dragging) if (releaseIndex >= 0) ccg$drop(releaseIndex);
		else ccg$putBack();
		else if (releaseIndex == ccg$pressSource) {
			var stack = ccg$ghostStack(ccg$pressSource);
			if (!stack.isEmpty()) ccg$pickUp(ccg$pressSource);
		} else if (releaseIndex >= 0) ccg$swapSlots(ccg$pressSource, releaseIndex);
		ccg$pressSource = -1;
		ccg$dragging = false;
		return true;
	}
	/** 返回鼠标下命中的幽灵槽索引，未命中返回 -1 */
	@Unique
	private int ccg$hoveredGhostIndex() {
		if (!(hoveredSlot instanceof SorterProofSlot ghostSlot)) return -1;
		return ghostSlot.getSlotIndex();
	}
	/** 把拿起物品与数量放入 dst：dst 为空则放下并结束；非空则交换，dst 原物品转为拿起 */
	@Unique
	private void ccg$drop(int dst) {
		var dstStack = ccg$ghostStack(dst);
		var dstCount = amounts.get(dst);
		ccg$setGhostSlot(dst, ccg$picked);
		amounts.set(dst, ccg$pickedCount);
		if (dstStack.isEmpty()) {
			ccg$picked = ItemStack.EMPTY;
			ccg$pickedCount = 1;
			ccg$pickedIndex = -1;
		} else {
			ccg$picked = dstStack;
			ccg$pickedCount = dstCount;
			ccg$pickedIndex = dst; // 源槽至此为空，置为 dst 以便放回/再交换
		}
	}
	/** 拿起物品放回源槽，结束拿起 */
	@Unique
	private void ccg$putBack() {
		if (ccg$pickedIndex < 0) return;
		ccg$setGhostSlot(ccg$pickedIndex, ccg$picked);
		amounts.set(ccg$pickedIndex, ccg$pickedCount);
		ccg$picked = ItemStack.EMPTY;
		ccg$pickedCount = 1;
		ccg$pickedIndex = -1;
	}
	/** 读取某幽灵槽当前物品 */
	@Unique
	private ItemStack ccg$ghostStack(int index) {
		return thiz().getMenu().ghostInventory.getStackInSlot(index);
	}
	/** 拿起 i 槽物品与数量，槽清空 */
	@Unique
	private void ccg$pickUp(int index) {
		ccg$picked = ccg$ghostStack(index);
		ccg$pickedCount = amounts.get(index);
		ccg$pickedIndex = index;
		ccg$setGhostSlot(index, ItemStack.EMPTY);
		amounts.set(index, 1);
	}
	/** 交换两个幽灵槽的物品与数量 */
	@Unique
	private void ccg$swapSlots(int src, int dst) {
		var ghost = thiz().getMenu().ghostInventory;
		var srcStack = ghost.getStackInSlot(src);
		var dstStack = ghost.getStackInSlot(dst);
		var srcAmount = amounts.get(src);
		var dstAmount = amounts.get(dst);
		ccg$setGhostSlot(src, dstStack);
		ccg$setGhostSlot(dst, srcStack);
		amounts.set(src, dstAmount);
		amounts.set(dst, srcAmount);
	}
	/** 设置某幽灵槽物品并同步服务端 */
	@Unique
	private void ccg$setGhostSlot(int index, ItemStack stack) {
		thiz().getMenu().ghostInventory.setStackInSlot(index, stack);
		CatnipServices.NETWORK.sendToServer(new GhostItemSubmitPacket(stack, index));
	}
	/** 按住移动超过阈值即判定为拖动：源槽物品移除（指针跟随）并显示源槽绿框 */
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (!CCG.config.misc.quickRequestActions) return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
		if (button == 0 && ccg$pressSource >= 0 && !ccg$dragging) {
			var dx = mouseX - ccg$pressX;
			var dy = mouseY - ccg$pressY;
			if (dx * dx + dy * dy >= DRAG_THRESHOLD * DRAG_THRESHOLD) {
				ccg$dragging = true;
				ccg$pickUp(ccg$pressSource); // 源槽物品移到指针、源槽清空
			}
			return true;
		}
		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}
	/**
	 * 绘制悬空物品与源槽高亮（屏幕坐标）。
	 * - 抓起中（pickedIndex≥0）：指针处画拿起的物品与数量；
	 * - 长按拖动（dragging）：源槽覆盖绿色高亮层（复用 JEI GhostIngredientDrag 的目标绿），
	 * 并在指针命中的幽灵槽上叠加更亮的悬停绿，提示落点。
	 */
	@Inject(method = "renderForeground", at = @At("TAIL"))
	private void renderPicked(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
		if (!CCG.config.misc.quickRequestActions || ccg$pickedIndex < 0) return;
		var x = mouseX - 8;
		var y = mouseY - 8;
		graphics.renderItem(ccg$picked, x, y);
		graphics.renderItemDecorations(font, ccg$picked, x, y, ccg$pickedCount + "");
		if (!ccg$dragging) return;
		// 源槽覆盖 JEI 目标绿（物品已拿走，仅位置提示）
		var sx = leftPos + 27 + ccg$pickedIndex * 20;
		var sy = topPos + 28;
		graphics.fill(sx, sy, sx + 16, sy + 16, TARGET_GREEN);
		// 指针命中的幽灵槽（非源槽）叠加更亮的悬停绿，提示落点
		var hoverIndex = ccg$hoveredGhostIndex();
		if (hoverIndex >= 0 && hoverIndex != ccg$pickedIndex) {
			var hx = leftPos + 27 + hoverIndex * 20;
			var hy = topPos + 28;
			graphics.fill(hx, hy, hx + 16, hy + 16, HOVER_GREEN);
		}
	}
	/** 幽灵槽左键按下：拦截避免被 GhostItemMenu 清空。记录按下源和坐标，供拖动判定 */
	@Unique
	private boolean ccg$handleGhostSlotPress(double mouseX, double mouseY) {
		var ghostIndex = ccg$hoveredGhostIndex();
		if (ghostIndex < 0) return false;
		// 抓取中：点回源槽→放回；点另一幽灵槽→放下/交换
		if (ccg$pickedIndex >= 0) {
			if (ghostIndex == ccg$pickedIndex) ccg$putBack();
			else ccg$drop(ghostIndex);
			return true;
		}
		// 空闲按下 → 记录源和坐标，等待 mouseDragged/mouseReleased 判定单击或拖动
		ccg$pressSource = ghostIndex;
		ccg$pressX = mouseX;
		ccg$pressY = mouseY;
		ccg$dragging = false;
		return true;
	}
	@Unique
	private boolean ccg$openPopupForHoveredSlot() {
		if (!(hoveredSlot instanceof SorterProofSlot ghostSlot)) return false;
		var index = ghostSlot.getSlotIndex();
		var stack = ghostSlot.getItem();
		if (stack.isEmpty()) return false;
		mc.setScreen(new RequestAmountScreen(
			thiz(),
			stack,
			amounts.get(index),
			CCG.config.misc.removeRequestLimit ? Integer.MAX_VALUE : 256,
			count -> amounts.set(index, count)
		));
		return true;
	}
}
