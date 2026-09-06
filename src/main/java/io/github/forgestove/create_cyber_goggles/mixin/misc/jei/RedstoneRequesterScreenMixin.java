package io.github.forgestove.create_cyber_goggles.mixin.misc.jei;
import com.simibubi.create.content.logistics.AddressEditBox;
import com.simibubi.create.content.logistics.redstoneRequester.*;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterMenu.SorterProofSlot;
import com.simibubi.create.content.logistics.stockTicker.LogisticalStockRequestPacket;
import com.simibubi.create.foundation.gui.*;
import com.simibubi.create.foundation.gui.menu.*;
import com.simibubi.create.foundation.gui.widget.IconButton;
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

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
@Mixin(RedstoneRequesterScreen.class)
public abstract class RedstoneRequesterScreenMixin extends AbstractSimiContainerScreen<RedstoneRequesterMenu>
	implements Self<RedstoneRequesterScreen> {
	/** 判定为拖动的位移阈值（像素） */
	@Unique private static final double DRAG_THRESHOLD = 4;
	/** 拖拽槽位覆盖层颜色，复用 JEI GhostIngredientDrag：目标绿（未悬停）与悬停绿（更亮） */
	@Unique private static final int TARGET_GREEN = 0x4013C90A;
	@Unique private static final int HOVER_GREEN = 0x804CC919;
	/** 打开界面时缓存的初始 ghost 槽内容，供撤销恢复 */
	@Unique private final List<ItemStack> ccg$backupStacks = new ArrayList<>();
	@Unique private final List<Integer> ccg$backupAmounts = new ArrayList<>();
	@Shadow private List<Integer> amounts;
	@Shadow private AddressEditBox addressBox;
	@Shadow private IconButton allowPartial;
	@Shadow private IconButton dontAllowPartial;
	/** 打开界面时缓存的地址与「允许部分请求」开关 */
	@Unique private String ccg$backupAddress = "";
	@Unique private boolean ccg$backupAllowPartial;
	/** 撤销按钮引用，用于动态置灰（无更改时不可用） */
	@Unique private IconButton ccg$undoButton;
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
	/** 在「完成」按钮左侧添加撤销按钮：点击恢复到打开界面时的初始内容 */
	@Inject(method = "init", at = @At("TAIL"))
	private void addUndoButton(CallbackInfo ci) {
		var x = getGuiLeft();
		var y = getGuiTop();
		var bgWidth = AllGuiTextures.REDSTONE_REQUESTER.getWidth();
		var bgHeight = AllGuiTextures.REDSTONE_REQUESTER.getHeight();
		// 此刻 addressBox/allowPartial/ghostInventory 均已初始化，缓存全部初始内容供撤销恢复
		var menu = thiz().getMenu();
		ccg$backupStacks.clear();
		ccg$backupAmounts.clear();
		for (var i = 0; i < menu.ghostInventory.getSlots(); i++) {
			ccg$backupStacks.add(menu.ghostInventory.getStackInSlot(i).copy());
			ccg$backupAmounts.add(amounts.get(i));
		}
		ccg$backupAddress = addressBox.getValue();
		ccg$backupAllowPartial = allowPartial.green;
		ccg$undoButton = new IconButton(x + bgWidth - 59, y + bgHeight - 25, AllIcons.I_CONFIG_RESET);
		ccg$undoButton.setToolTip(Component.translatable("config.ui.undo.tooltip"));
		ccg$undoButton.withCallback(this::ccg$undo);
		ccg$undoButton.active = false; // 刚打开时无更改，撤销不可用
		addRenderableWidget(ccg$undoButton);
	}
	/** 撤销：恢复打开界面时缓存的 ghost 槽、数量、地址与「允许部分」开关，并清空拖拽/拿起状态 */
	@Unique
	private void ccg$undo() {
		var menu = thiz().getMenu();
		var ghost = menu.ghostInventory;
		// 恢复本地 ghost 槽物品，并逐槽同步服务端（更新服务端 ghostInventory，saveData 会重编码 request）
		for (var i = 0; i < ghost.getSlots(); i++) {
			var stack = ccg$backupStacks.get(i).copy();
			ghost.setStackInSlot(i, stack);
			CatnipServices.NETWORK.sendToServer(new GhostItemSubmitPacket(stack, i));
		}
		// 恢复本地数量
		for (var i = 0; i < amounts.size(); i++)
			amounts.set(i, ccg$backupAmounts.get(i));
		// 恢复地址与「允许部分」开关的本地显示
		addressBox.setValue(ccg$backupAddress);
		var allow = ccg$backupAllowPartial;
		allowPartial.green = allow;
		dontAllowPartial.green = !allow;
		// 提交地址、allowPartial、数量到服务端。注意：saveData 会压缩掉空槽，
		// 故数量列表需与「非空槽顺序」对齐（不含空槽），写成压缩后的列表。
		var compressedAmounts = new ArrayList<Integer>();
		for (var i = 0; i < ccg$backupStacks.size(); i++)
			if (!ccg$backupStacks.get(i).isEmpty()) compressedAmounts.add(ccg$backupAmounts.get(i));
		CatnipServices.NETWORK.sendToServer(new RedstoneRequesterConfigurationPacket(
			menu.contentHolder.getBlockPos(),
			ccg$backupAddress,
			allow,
			compressedAmounts
		));
		// 清空拖拽/拿起状态
		ccg$picked = ItemStack.EMPTY;
		ccg$pickedCount = 1;
		ccg$pickedIndex = -1;
		ccg$pressSource = -1;
		ccg$dragging = false;
		// 撤销完成 → 关闭屏幕，让服务端按新配置收尾
		onClose();
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
		// 每帧评估是否有未保存的更改，决定撤销按钮是否可用
		if (ccg$undoButton != null) ccg$undoButton.active = ccg$isDirty();
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
	/** 当前内容是否与打开界面时缓存的初始内容不同（不同则撤销可用） */
	@Unique
	private boolean ccg$isDirty() {
		var menu = thiz().getMenu();
		for (var i = 0; i < menu.ghostInventory.getSlots(); i++)
			if (!ItemStack.matches(menu.ghostInventory.getStackInSlot(i), ccg$backupStacks.get(i))) return true;
		for (var i = 0; i < amounts.size(); i++)
			if (!Objects.equals(amounts.get(i), ccg$backupAmounts.get(i))) return true;
		if (!Objects.equals(addressBox.getValue(), ccg$backupAddress)) return true;
		return allowPartial.green != ccg$backupAllowPartial;
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
