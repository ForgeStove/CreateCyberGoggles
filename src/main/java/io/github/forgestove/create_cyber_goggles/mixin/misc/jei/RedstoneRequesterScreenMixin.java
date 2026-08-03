package io.github.forgestove.create_cyber_goggles.mixin.misc.jei;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.redstoneRequester.*;
import com.simibubi.create.content.logistics.stockTicker.LogisticalStockRequestPacket;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.menu.*;
import com.simibubi.create.foundation.gui.widget.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.api.*;
import io.github.forgestove.create_cyber_goggles.core.event.CCGKey;
import io.github.forgestove.create_cyber_goggles.core.factory.RequestAmountOverlay;
import io.github.forgestove.create_cyber_goggles.core.util.CCGLang;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.List;
@Mixin(value = RedstoneRequesterScreen.class, remap = false)
public abstract class RedstoneRequesterScreenMixin extends AbstractSimiContainerScreen<RedstoneRequesterMenu>
	implements Self<RedstoneRequesterScreen> {
	/** 快捷数量设置弹窗（create 原版样式，叠加渲染在当前界面） */
	@Unique private RequestAmountOverlay ccg$popup = new RequestAmountOverlay();
	@Shadow private List<Integer> amounts;
	/** 翻页按钮（无法切换时禁用） */
	@Unique private IconButton ccg$prevButton;
	@Unique private IconButton ccg$nextButton;
	public RedstoneRequesterScreenMixin(RedstoneRequesterMenu container, Inventory inv, Component title) {
		super(container, inv, title);
	}
	@Override
	public void resize(@NotNull Minecraft minecraft, int width, int height) {
		super.resize(minecraft, width, height);
		ccg$popup = new RequestAmountOverlay();
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
	/** 添加翻页按钮（9 格右侧下方） */
	@Inject(method = "init", at = @At("TAIL"))
	private void addPageButtons(CallbackInfo ci) {
		if (!CCG.config.misc.redstoneRequesterLargeRequest) return;
		var x = thiz().getGuiLeft() + 27;
		var y = thiz().getGuiTop() + 28;
		var menu = (RequestPageProvider) thiz().getMenu();
		ccg$prevButton = new IconButton(x - 18, y + 36, AllIcons.I_CONFIG_PREV).withCallback(() -> {
			menu.ccg$setRequestPage(Math.max(0, menu.ccg$getRequestPage() - 1));
			ccg$updatePageButtons();
		});
		ccg$nextButton = new IconButton(x + 9 * 20 - 4, y + 36, AllIcons.I_CONFIG_NEXT).withCallback(() -> {
			menu.ccg$setRequestPage(Math.min(8, menu.ccg$getRequestPage() + 1));
			ccg$updatePageButtons();
		});
		addRenderableWidget(ccg$prevButton);
		addRenderableWidget(ccg$nextButton);
		ccg$updatePageButtons();
	}
	/** 固定 9 页翻页：非最后页时右按钮一直可用 */
	@Unique
	private void ccg$updatePageButtons() {
		if (ccg$prevButton == null || ccg$nextButton == null) return;
		var page = ccg$page();
		ccg$prevButton.active = page > 0;
		ccg$nextButton.active = page < 8;
	}
	/** 当前页码（存于 Menu，供 Screen 渲染与 JEI 拖入共享） */
	@Unique
	private int ccg$page() {
		return ((RequestPageProvider) thiz().getMenu()).ccg$getRequestPage();
	}
	/** 突破 9 格：数量列表扩到 81（与幽灵库存一致） */
	@ModifyConstant(method = "<init>", constant = @Constant(intValue = 9))
	private int amountsSize(int constant) {
		return CCG.config.misc.redstoneRequesterLargeRequest ? 81 : constant;
	}
	/** 分页后原版数量渲染会沿 i*20 横向溢出，禁用它（数量由下方自绘当前页） */
	@WrapOperation(method = "renderForeground", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"))
	private int disableVanillaAmounts(List<?> instance, Operation<Integer> original) {
		return CCG.config.misc.redstoneRequesterLargeRequest ? 0 : original.call(instance);
	}
	/** 分页渲染当前 9 格物品（背景纹理自带槽边框）与数量，并显示 hover tooltip 与页码 */
	@Inject(method = "renderForeground", at = @At("RETURN"))
	private void renderPage(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
		if (!CCG.config.misc.redstoneRequesterLargeRequest) return;
		var x = thiz().getGuiLeft() + 27;
		var y = thiz().getGuiTop() + 28;
		var ghost = thiz().getMenu().ghostInventory;
		for (var i = 0; i < 9; i++) {
			var index = ccg$getPagedSlotIndex(i);
			var stack = ghost.getStackInSlot(index);
			if (stack.isEmpty()) continue;
			graphics.renderItem(stack, x + 20 * i, y);
			graphics.renderItemDecorations(font, stack, x + 20 * i, y, amounts.get(index) + "");
		}
		// 页码（固定 9 页；无物品不显示）
		ccg$updatePageButtons();
		if (ccg$pageCount() > 0) {
			var ms = graphics.pose();
			ms.pushPose();
			ms.scale(0.5F, 0.5F, 1F);
			graphics.drawString(font, (ccg$page() + 1) + "/9", (x + 164) * 2, (y + 21) * 2, 0xFFFFFF);
			ms.popPose();
		}
		// 提示框
		for (var i = 0; i < 9; i++) {
			var ix = x + 20 * i;
			if (mouseX < ix || mouseX >= ix + 16 || mouseY < y || mouseY >= y + 16) continue;
			renderSlotHighlight(graphics, ix, y, 0);
			var index = ccg$getPagedSlotIndex(i);
			var stack = ghost.getStackInSlot(index);
			if (stack.isEmpty()) break;
			List<Component> tooltip = List.of(
				Component.translatable(
					"create.gui.factory_panel.send_item",
					CCGLang.itemName(stack)
						.text(" x" + amounts.get(index), ScrollInput.HEADER_RGB)
						.color(ScrollInput.HEADER_RGB)
						.component()
				).withColor(ScrollInput.HEADER_RGB.getRGB()),
				Component.translatable("create.gui.factory_panel.scroll_to_change_amount")
					.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC)
			);
			graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
			break;
		}
		if (ccg$popup.open) ccg$popup.render(graphics, mouseX, mouseY, partialTicks);
	}
	@Unique
	private int ccg$getPagedSlotIndex(int i) {
		if (!CCG.config.misc.redstoneRequesterLargeRequest) return i;
		return ccg$page() * 9 + i;
	}
	/** 动态总页数（无物品返回 0） */
	@Unique
	private int ccg$pageCount() {
		var ghost = thiz().getMenu().ghostInventory;
		var last = -1;
		for (var i = 0; i < ghost.getSlots(); i++)
			if (!ghost.getStackInSlot(i).isEmpty()) last = i;
		return last < 0 ? 0 : last / 9 + 1;
	}
	/** 分页交互：当前 9 格放置/移除物品（翻页由 IconButton 处理） */
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (ccg$popup.open) {
			ccg$popup.mouseClicked(mouseX, mouseY, button);
			return true;
		}
		if (CCG.config.misc.quickRequestActions && CCGKey.stockRequestSetter.isDown() && ccg$openPopupForHoveredSlot(mouseX, mouseY)) return true;
		if (!CCG.config.misc.redstoneRequesterLargeRequest) return super.mouseClicked(mouseX, mouseY, button);
		var x = thiz().getGuiLeft() + 27;
		var y = thiz().getGuiTop() + 28;
		var gx = (int) ((mouseX - x) / 20);
		if (gx >= 0 && gx < 9 && mouseY >= y && mouseY < y + 16) {
			var slot = ccg$getPagedSlotIndex(gx);
			var ghost = thiz().getMenu().ghostInventory;
			if (button == 0) {
				var carried = thiz().getMenu().getCarried();
				var stack = carried.isEmpty() ? ItemStack.EMPTY : carried.copyWithCount(1);
				ghost.setStackInSlot(slot, stack);
				CatnipServices.NETWORK.sendToServer(new GhostItemSubmitPacket(stack, slot));
			} else if (button == 1) {
				ghost.setStackInSlot(slot, ItemStack.EMPTY);
				amounts.set(slot, 1);
				CatnipServices.NETWORK.sendToServer(new GhostItemSubmitPacket(ItemStack.EMPTY, slot));
			}
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
	@Unique
	private boolean ccg$openPopupForHoveredSlot(double mouseX, double mouseY) {
		var x = thiz().getGuiLeft() + 27;
		var y = thiz().getGuiTop() + 28;
		var gx = (int) ((mouseX - x) / 20);
		if (gx < 0 || gx >= 9 || mouseY < y || mouseY >= y + 16) return false;
		var slot = ccg$getPagedSlotIndex(gx);
		var stack = thiz().getMenu().ghostInventory.getStackInSlot(slot);
		if (stack.isEmpty()) return false;
		ccg$popup.open(stack, amounts.get(slot), ccg$getAvailableMax(stack), count -> amounts.set(slot, count));
		return true;
	}
	@Unique
	private int ccg$getAvailableMax(ItemStack stack) {
		if (thiz().getMenu().contentHolder instanceof StockSnapshotHolder holder) {
			var summary = holder.ccg$getStockSnapshot();
			if (summary != null) {
				var count = summary.getCountOf(stack);
				if (count == BigItemStack.INF) return Integer.MAX_VALUE;
				if (count > 0) return count;
			}
		}
		return CCG.config.misc.removeRequestLimit ? Integer.MAX_VALUE : 256;
	}
	/** 分页交互：当前 9 格滚轮改数量（功能关闭时放行原版，否则会覆盖 create 的 mouseScrolled） */
	@Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
	private void mouseScrolledEarlyReturn(
		double mouseX,
		double mouseY,
		double scrollX,
		double scrollY,
		CallbackInfoReturnable<Boolean> cir
	) {
		if (ccg$popup.open) cir.setReturnValue(true);
	}
	@ModifyArg(
		method = "mouseScrolled", at = @At(
		value = "INVOKE", target = "Lnet/neoforged/neoforge/items/ItemStackHandler;getStackInSlot(I)Lnet/minecraft/world/item/ItemStack;"
	)
	)
	private int shiftSlotGet(int i) {
		if (!CCG.config.misc.redstoneRequesterLargeRequest) return i;
		return ccg$getPagedSlotIndex(i);
	}
	@ModifyArg(
		method = "mouseScrolled", at = @At(value = "INVOKE", target = "Ljava/util/List;set(ILjava/lang/Object;)Ljava/lang/Object;")
	)
	private int shiftAmountSet(int i) {
		if (!CCG.config.misc.redstoneRequesterLargeRequest) return i;
		return ccg$getPagedSlotIndex(i);
	}
	@ModifyArg(
		method = "mouseScrolled", at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;")
	)
	private int shiftAmountGet(int i) {
		if (!CCG.config.misc.redstoneRequesterLargeRequest) return i;
		return ccg$getPagedSlotIndex(i);
	}
	/** 快捷设置弹窗：字符输入 */
	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		if (!ccg$popup.open) return super.charTyped(codePoint, modifiers);
		return ccg$popup.charTyped(codePoint, modifiers);
	}
	/** 快捷设置弹窗：键盘处理 */
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (!ccg$popup.open) return super.keyPressed(keyCode, scanCode, modifiers);
		return ccg$popup.keyPressed(keyCode, scanCode, modifiers);
	}
}
