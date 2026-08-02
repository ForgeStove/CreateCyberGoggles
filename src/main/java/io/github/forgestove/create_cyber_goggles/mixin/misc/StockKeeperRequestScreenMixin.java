package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.platform.InputConstants;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.stockTicker.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.api.Self;
import io.github.forgestove.create_cyber_goggles.core.event.CCGKey;
import io.github.forgestove.create_cyber_goggles.core.factory.RequestAmountOverlay;
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
	@Unique private final RequestAmountOverlay ccg$popup = new RequestAmountOverlay();
	@Shadow public List<List<BigItemStack>> displayedItems;
	@Shadow @Final int cols;
	@Shadow StockTickerBlockEntity blockEntity;
	@Shadow @Final Couple<Integer> noneHovered;
	@WrapWithCondition(
		method = "containerTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;closeContainer()V")
	)
	public boolean containerTick(Player instance) {
		return thiz().getMenu().containerId != -1;
	}
	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		if (!CCG.config.misc.stockRequestQuickActions) {
			if (ccg$popup.isOpen()) ccg$popup.close();
			return;
		}
		if (ccg$popup.isOpen()) {
			ccg$popup.mouseClicked(mouseX, mouseY, button);
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
		var entry = group == -1 ? thiz().itemsToOrder.get(index) : displayedItems.get(group).get(index);
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
		var entry = group == -1 ? thiz().itemsToOrder.get(index) : displayedItems.get(group).get(index);
		return entry == null || entry.stack.isEmpty() ? null : entry;
	}
	@Unique
	private boolean ccg$openPopupForHoveredItem(int mouseX, int mouseY) {
		var entry = ccg$getHoveredEntry(mouseX, mouseY);
		if (entry == null) return false;
		var max = ccg$getAvailableMax(entry);
		var existing = getOrderForItem(entry.stack);
		ccg$popup.open(entry.stack, existing == null ? 1 : existing.count, max, mc.font, count -> ccg$setOrRemoveOrder(entry.stack,
			count));
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
	@Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
	private void charTyped(char codePoint, int modifiers, CallbackInfoReturnable<Boolean> cir) {
		if (!CCG.config.misc.stockRequestQuickActions) return;
		if (!ccg$popup.isOpen()) return;
		ccg$popup.charTyped(codePoint, modifiers);
		cir.setReturnValue(true);
	}
	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
	private void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
		if (!CCG.config.misc.stockRequestQuickActions) return;
		if (!ccg$popup.isOpen()) return;
		ccg$popup.keyPressed(keyCode, scanCode, modifiers);
		cir.setReturnValue(true);
	}
	@Inject(method = "renderForeground", at = @At("TAIL"))
	private void renderPopup(GuiGraphics gui, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
		if (!CCG.config.misc.stockRequestQuickActions) {
			if (ccg$popup.isOpen()) ccg$popup.close();
			return;
		}
		if (!ccg$popup.isOpen()) return;
		ccg$popup.render(gui, mouseX, mouseY, partialTicks);
	}
	@Unique
	private void ccg$setOrRemoveOrder(ItemStack stack, int count) {
		var existing = getOrderForItem(stack);
		var thiz = thiz();
		if (count <= 0) {
			if (existing != null) thiz.itemsToOrder.remove(existing);
			return;
		}
		if (existing == null) {
			if (thiz.itemsToOrder.size() < cols) thiz.itemsToOrder.add(new BigItemStack(stack.copyWithCount(1), count));
		} else existing.count = count;
	}
	@Shadow
	protected abstract BigItemStack getOrderForItem(ItemStack stack);
}
