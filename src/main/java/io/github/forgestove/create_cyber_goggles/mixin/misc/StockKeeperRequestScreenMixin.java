package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.mojang.blaze3d.platform.InputConstants;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.stockTicker.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.api.Self;
import io.github.forgestove.create_cyber_goggles.core.event.CCGKey;
import io.github.forgestove.create_cyber_goggles.core.factory.StockRequestAmountOverlay;
import net.createmod.catnip.data.Couple;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.awt.Rectangle;
import java.util.List;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
@Mixin(StockKeeperRequestScreen.class)
public abstract class StockKeeperRequestScreenMixin implements Self<StockKeeperRequestScreen> {
	@Unique private final StockRequestAmountOverlay ccg$popup = new StockRequestAmountOverlay();
	@Shadow public List<BigItemStack> itemsToOrder;
	@Shadow public List<List<BigItemStack>> displayedItems;
	@Shadow @Final int cols, colWidth;
	@Shadow int itemsX, itemsY, windowWidth, windowHeight;
	@Shadow StockTickerBlockEntity blockEntity;
	@Shadow @Final Couple<Integer> noneHovered;
	@WrapWithCondition(
		method = "containerTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;closeContainer()V")
	)
	public boolean containerTick(Player instance) {
		return thiz().getMenu().containerId != -1;
	}
	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void ccg$mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		if (!CCG.config.misc.stockRequestQuickActions) {
			if (ccg$popup.isOpen()) ccg$popup.close();
			return;
		}
		if (ccg$popup.isOpen()) {
			switch (ccg$popup.mouseClicked(mouseX, mouseY, button, ccg$popupX(), ccg$popupY())) {
				case APPLY -> ccg$applyPopupAmount();
				case CLOSE -> ccg$popup.close();
			}
			cir.setReturnValue(true);
			return;
		}
		if (button == InputConstants.MOUSE_BUTTON_LEFT && CCGKey.stockRequestSelectAll.isDown() && ccg$applyAltFullAmount(
			(int) mouseX,
			(int) mouseY
		)) {
			cir.setReturnValue(true);
			return;
		}
		if (!CCGKey.stockRequestSetter.isDown()) return;
		if (ccg$openPopupForHoveredItem((int) mouseX, (int) mouseY)) cir.setReturnValue(true);
	}
	@Unique
	private boolean ccg$applyAltFullAmount(int mouseX, int mouseY) {
		var hoveredSlot = getHoveredSlot(mouseX, mouseY);
		if (hoveredSlot == noneHovered) return false;
		int group = hoveredSlot.getFirst();
		int index = hoveredSlot.getSecond();
		if (group == -2) return false;
		var entry = group == -1 ? itemsToOrder.get(index) : displayedItems.get(group).get(index);
		if (entry == null || entry.stack.isEmpty()) return false;
		if (group == -1) {
			ccg$setOrRemoveOrder(entry.stack, 0);
			return true;
		}
		ccg$setOrRemoveOrder(entry.stack, ccg$getAvailableMax(entry));
		return true;
	}
	@Unique
	private BigItemStack ccg$getHoveredEntry(int mouseX, int mouseY) {
		var hoveredSlot = getHoveredSlot(mouseX, mouseY);
		if (hoveredSlot == noneHovered) return null;
		int group = hoveredSlot.getFirst();
		int index = hoveredSlot.getSecond();
		if (group == -2) return null; // 配方条使用自己的数量语义
		var entry = group == -1 ? itemsToOrder.get(index) : displayedItems.get(group).get(index);
		return entry == null || entry.stack.isEmpty() ? null : entry;
	}
	@Unique
	private boolean ccg$openPopupForHoveredItem(int mouseX, int mouseY) {
		var entry = ccg$getHoveredEntry(mouseX, mouseY);
		if (entry == null) return false;
		var max = ccg$getAvailableMax(entry);
		var existing = getOrderForItem(entry.stack);
		ccg$popup.open(entry.stack, existing == null ? 1 : existing.count, max, mc.font, ccg$popupX(), ccg$popupY());
		return true;
	}
	@Shadow
	protected abstract Couple<Integer> getHoveredSlot(int x, int y);
	@Unique
	private int ccg$getAvailableMax(BigItemStack entry) {
		var max = 0;
		var summary = blockEntity.getLastClientsideStockSnapshotAsSummary();
		if (summary != null) {
			var summaryCount = summary.getCountOf(entry.stack);
			if (summaryCount == BigItemStack.INF) return Integer.MAX_VALUE;
			if (summaryCount > 0) max = summaryCount;
		}
		if (max == 0) return Math.max(1, entry.count);
		return max;
	}
	@Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
	private void ccg$mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY, CallbackInfoReturnable<Boolean> cir) {
		if (!CCG.config.misc.stockRequestQuickActions) return;
		if (ccg$popup.isOpen()) cir.setReturnValue(true);
	}
	@Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
	private void ccg$charTyped(char codePoint, int modifiers, CallbackInfoReturnable<Boolean> cir) {
		if (!CCG.config.misc.stockRequestQuickActions) return;
		if (!ccg$popup.isOpen()) return;
		ccg$popup.charTyped(codePoint, modifiers);
		cir.setReturnValue(true);
	}
	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
	private void ccg$keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
		if (!CCG.config.misc.stockRequestQuickActions) return;
		if (!ccg$popup.isOpen()) return;
		switch (ccg$popup.keyPressed(keyCode, scanCode, modifiers)) {
			case APPLY -> ccg$applyPopupAmount();
			case CLOSE -> ccg$popup.close();
		}
		cir.setReturnValue(true);
	}
	@Unique
	private void ccg$applyPopupAmount() {
		if (ccg$popup.getStack().isEmpty()) {
			ccg$popup.close();
			return;
		}
		ccg$setOrRemoveOrder(ccg$popup.getStack(), ccg$popup.getRequestedAmount());
		ccg$popup.close();
	}
	@Unique
	private void ccg$setOrRemoveOrder(ItemStack stack, int count) {
		var existing = getOrderForItem(stack);
		if (count <= 0) {
			if (existing != null) itemsToOrder.remove(existing);
			return;
		}
		if (existing == null) {
			if (itemsToOrder.size() < cols) itemsToOrder.add(new BigItemStack(stack.copyWithCount(1), count));
		} else existing.count = count;
	}
	@Shadow
	protected abstract BigItemStack getOrderForItem(ItemStack stack);
	@Inject(method = "renderForeground", at = @At("TAIL"))
	private void ccg$renderPopup(GuiGraphics gui, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
		if (!CCG.config.misc.stockRequestQuickActions) {
			if (ccg$popup.isOpen()) ccg$popup.close();
			return;
		}
		if (!ccg$popup.isOpen()) return;
		ccg$popup.render(gui, mc.font, mouseX, mouseY, partialTicks, ccg$popupX(), ccg$popupY());
	}
	@Unique
	private int ccg$popupX() {
		var guiLeft = itemsX - (windowWidth - cols * colWidth) / 2 - 1;
		return guiLeft + (windowWidth - 120) / 2;
	}
	@Unique
	private int ccg$popupY() {
		var guiTop = itemsY - 33;
		return guiTop + (windowHeight - 82) / 2;
	}
	@WrapOperation(
		method = "renderForeground", at = @At(
		value = "INVOKE",
		target = "Lcom/simibubi/create/content/logistics/stockTicker/StockKeeperRequestScreen;getHoveredSlot(II)"
			+ "Lnet/createmod/catnip/data/Couple;"
	)
	)
	private Couple<Integer> ccg$suppressTooltipsWhenPopup(
		StockKeeperRequestScreen instance,
		int x,
		int y,
		Operation<Couple<Integer>> original
	) {
		if (ccg$popup.isOpen()) {
			var popupArea = new Rectangle(ccg$popupX(), ccg$popupY(), 120, 82);
			if (popupArea.contains(x, y)) return noneHovered;
		}
		return original.call(instance, x, y);
	}
}
