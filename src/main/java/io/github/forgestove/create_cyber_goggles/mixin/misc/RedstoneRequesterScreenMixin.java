package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.simibubi.create.content.logistics.redstoneRequester.*;
import com.simibubi.create.content.logistics.stockTicker.LogisticalStockRequestPacket;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.menu.*;
import com.simibubi.create.foundation.gui.widget.*;
import com.simibubi.create.foundation.utility.CreateLang;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.api.*;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
@Mixin(RedstoneRequesterScreen.class)
public abstract class RedstoneRequesterScreenMixin extends AbstractSimiContainerScreen<RedstoneRequesterMenu>
	implements Self<RedstoneRequesterScreen> {
	@Shadow private List<Integer> amounts;
	/** 翻页按钮（无法切换时禁用） */
	@Unique private IconButton ccg$prevButton;
	@Unique private IconButton ccg$nextButton;
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
	/** 添加翻页按钮（9 格右侧下方） */
	@Inject(method = "init", at = @At("TAIL"))
	private void addPageButtons(CallbackInfo ci) {
		if (!CCG.config.misc.jei.redstoneRequesterLargeRequest) return;
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
		int page = ccg$page();
		ccg$prevButton.active = page > 0;
		ccg$nextButton.active = page < 8;
	}
	/** 动态总页数（无物品返回 0） */
	@Unique
	private int ccg$pageCount() {
		var ghost = thiz().getMenu().ghostInventory;
		var last = -1;
		for (var i = 0; i < ghost.getSlots(); i++)
			if (!ghost.getStackInSlot(i).isEmpty()) last = i;
		return last / 9 + 1;
	}
	/** 当前页码（存于 Menu，供 Screen 渲染与 JEI 拖入共享） */
	@Unique
	private int ccg$page() {
		return ((RequestPageProvider) thiz().getMenu()).ccg$getRequestPage();
	}
	/** 突破 9 格：数量列表扩到 81（与幽灵库存一致） */
	@ModifyConstant(method = "<init>", constant = @Constant(intValue = 9))
	private int amountsSize(int constant) {
		return CCG.config.misc.jei.redstoneRequesterLargeRequest ? 81 : constant;
	}
	/** 分页后原版数量渲染会沿 i*20 横向溢出，禁用它（数量由下方自绘当前页） */
	@WrapOperation(method = "renderForeground", at = @At(value = "INVOKE", target = "Ljava/util/List;size()I"))
	private int disableVanillaAmounts(List<?> instance, Operation<Integer> original) {
		return CCG.config.misc.jei.redstoneRequesterLargeRequest ? 0 : original.call(instance);
	}
	/** 分页渲染当前 9 格物品（背景纹理自带槽边框）与数量，并显示 hover tooltip 与页码 */
	@Inject(method = "renderForeground", at = @At("RETURN"))
	private void renderPage(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
		if (!CCG.config.misc.jei.redstoneRequesterLargeRequest) return;
		var x = thiz().getGuiLeft() + 27;
		var y = thiz().getGuiTop() + 28;
		var ghost = thiz().getMenu().ghostInventory;
		var page = ccg$page();
		for (var i = 0; i < 9; i++) {
			// 槽框由背景纹理 REDSTONE_REQUESTER 自带，这里只画物品与数量
			var stack = ghost.getStackInSlot(page * 9 + i);
			if (stack.isEmpty()) continue;
			graphics.renderItem(stack, x + 20 * i, y);
			graphics.renderItemDecorations(font, stack, x + 20 * i, y, amounts.get(page * 9 + i) + "");
		}
		// 页码（缩小一半，固定 9 页；无物品不显示）
		ccg$updatePageButtons();
		if (ccg$pageCount() > 0) {
			var ms = graphics.pose();
			ms.pushPose();
			ms.scale(0.5F, 0.5F, 1F);
			graphics.drawString(font, (page + 1) + "/9", (x + 164) * 2, (y + 21) * 2, 0xFFFFFF);
			ms.popPose();
		}
		// hover 当前页物品显示 create 风格 tooltip
		for (var i = 0; i < 9; i++) {
			var ix = x + 20 * i;
			if (mouseX < ix || mouseX >= ix + 16 || mouseY < y || mouseY >= y + 16) continue;
			// create 原版悬停任意槽（含空槽）都显示高亮背景
			renderSlotHighlight(graphics, ix, y, 0);
			var stack = ghost.getStackInSlot(page * 9 + i);
			if (stack.isEmpty()) break;
			List<Component> tooltip = List.of(
				CreateLang.translate(
						"gui.factory_panel.send_item",
						CreateLang.itemName(stack).add(CreateLang.text(" x" + amounts.get(page * 9 + i)))
					)
					.color(ScrollInput.HEADER_RGB)
					.component(),
				CreateLang.translate("gui.factory_panel.scroll_to_change_amount")
					.style(ChatFormatting.DARK_GRAY)
					.style(ChatFormatting.ITALIC)
					.component(),
				CreateLang.translate("gui.scrollInput.shiftScrollsFaster")
					.style(ChatFormatting.DARK_GRAY)
					.style(ChatFormatting.ITALIC)
					.component()
			);
			graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
			break;
		}
	}
	/** 分页交互：当前 9 格放置/移除物品（翻页由 IconButton 处理） */
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!CCG.config.misc.jei.redstoneRequesterLargeRequest) return super.mouseClicked(mouseX, mouseY, button);
		var x = thiz().getGuiLeft() + 27;
		var y = thiz().getGuiTop() + 28;
		var gx = (int) ((mouseX - x) / 20);
		if (gx >= 0 && gx < 9 && mouseY >= y && mouseY < y + 16) {
			var slot = ccg$page() * 9 + gx;
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
	/** 分页交互：当前 9 格滚轮改数量（功能关闭时放行原版，否则会覆盖 create 的 mouseScrolled） */
	@Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
	private void mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY, CallbackInfoReturnable<Boolean> cir) {
		if (!CCG.config.misc.jei.redstoneRequesterLargeRequest) return;
		var x = thiz().getGuiLeft() + 27;
		var y = thiz().getGuiTop() + 28;
		for (var i = 0; i < 9; i++)
			if (mouseX >= x + 20 * i && mouseX < x + 20 * i + 16 && mouseY >= y && mouseY < y + 16) {
				var slot = ccg$page() * 9 + i;
				if (thiz().getMenu().ghostInventory.getStackInSlot(slot).isEmpty()) {
					cir.setReturnValue(true);
					return;
				}
				var base = hasShiftDown() ? 10 : 1;
				var max = CCG.config.misc.removeRequestLimit ? Integer.MAX_VALUE : 256;
				amounts.set(slot, Mth.clamp(amounts.get(slot) + (int) Math.signum(scrollY) * base, 1, max));
				cir.setReturnValue(true);
				return;
			}
	}
}
