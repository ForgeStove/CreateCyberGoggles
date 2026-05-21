package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.platform.InputConstants;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.stockTicker.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.api.Self;
import io.github.forgestove.create_cyber_goggles.core.event.CCGKey;
import io.github.forgestove.create_cyber_goggles.core.gui.StockRequestAmountOverlay;
import io.github.forgestove.create_cyber_goggles.core.gui.StockRequestAmountOverlay.*;
import net.createmod.catnip.data.Couple;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.List;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
@Mixin(StockKeeperRequestScreen.class)
public abstract class StockKeeperRequestScreenMixin implements Self<StockKeeperRequestScreen> {
	@Unique private final StockRequestAmountOverlay ccg$popup = new StockRequestAmountOverlay();
	@Shadow public List<BigItemStack> itemsToOrder;
	@Shadow public List<List<BigItemStack>> displayedItems;
	@Shadow
	@Final
	int cols;
	@Shadow
	@Final
	int colWidth;
	@Shadow int itemsX;
	@Shadow int itemsY;
	@Shadow int windowWidth;
	@Shadow int windowHeight;
	@Shadow StockTickerBlockEntity blockEntity;
	@Shadow
	@Final
	Couple<Integer> noneHovered;
	@Shadow
	protected abstract Couple<Integer> getHoveredSlot(int x, int y);
	@Shadow
	protected abstract BigItemStack getOrderForItem(ItemStack stack);
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
			var result = ccg$popup.mouseClicked(mouseX, mouseY, button, ccg$popupX(), ccg$popupY());
			if (result == ClickResult.APPLY) ccg$applyPopupAmount();
			if (result == ClickResult.CLOSE) ccg$popup.close();
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
		var result = ccg$popup.keyPressed(keyCode, scanCode, modifiers);
		if (result == KeyResult.APPLY) ccg$applyPopupAmount();
		if (result == KeyResult.CLOSE) ccg$popup.close();
		cir.setReturnValue(true);
	}
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
	private boolean ccg$openPopupForHoveredItem(int mouseX, int mouseY) {
		var hoveredSlot = getHoveredSlot(mouseX, mouseY);
		if (hoveredSlot == noneHovered) return false;
		int group = hoveredSlot.getFirst();
		int index = hoveredSlot.getSecond();
		if (group == -2) return false; // recipe strip uses its own amount semantics
		var entry = group == -1 ? itemsToOrder.get(index) : displayedItems.get(group).get(index);
		if (entry == null || entry.stack.isEmpty()) return false;
		var max = ccg$getAvailableMax(entry);
		var existingOrder = getOrderForItem(entry.stack);
		var initial = existingOrder == null ? 1 : existingOrder.count;
		ccg$popup.open(entry.stack, initial, max, mc.font, ccg$popupX(), ccg$popupY());
		return true;
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
		var existingOrder = getOrderForItem(entry.stack);
		// Bottom order row (-1) should reduce/remove on Alt+LMB; stock list rows should fill to max.
		if (group == -1) {
			if (existingOrder != null) itemsToOrder.remove(existingOrder);
			return true;
		}
		var max = ccg$getAvailableMax(entry);
		if (max <= 0) {
			if (existingOrder != null) itemsToOrder.remove(existingOrder);
			return true;
		}
		if (existingOrder == null) {
			if (itemsToOrder.size() < cols) itemsToOrder.add(new BigItemStack(entry.stack.copyWithCount(1), max));
		} else existingOrder.count = max;
		return true;
	}
	@Unique
	private void ccg$applyPopupAmount() {
		if (ccg$popup.getStack().isEmpty()) {
			ccg$popup.close();
			return;
		}
		var requested = ccg$popup.getRequestedAmount();
		var existingOrder = getOrderForItem(ccg$popup.getStack());
		if (requested <= 0) {
			if (existingOrder != null) itemsToOrder.remove(existingOrder);
			ccg$popup.close();
			return;
		}
		if (existingOrder == null) {
			if (itemsToOrder.size() < cols) itemsToOrder.add(new BigItemStack(ccg$popup.getStack().copyWithCount(1), requested));
		} else existingOrder.count = requested;
		ccg$popup.close();
	}
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
}
