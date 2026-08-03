package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.InputConstants;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.stockTicker.*;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.api.Self;
import io.github.forgestove.create_cyber_goggles.core.event.CCGKey;
import io.github.forgestove.create_cyber_goggles.core.factory.RequestAmountOverlay;
import net.createmod.catnip.data.Couple;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.getModifiedScrollAmount;
@Mixin(StockKeeperRequestScreen.class)
public abstract class StockKeeperRequestScreenMixin extends AbstractSimiContainerScreen<StockKeeperRequestMenu>
	implements Self<StockKeeperRequestScreen> {
	@Shadow public List<List<BigItemStack>> displayedItems;
	@Shadow @Final int cols;
	@Shadow StockTickerBlockEntity blockEntity;
	@Shadow @Final Couple<Integer> noneHovered;
	@Unique private RequestAmountOverlay ccg$popup = new RequestAmountOverlay();
	public StockKeeperRequestScreenMixin(StockKeeperRequestMenu container, Inventory inv, Component title) {
		super(container, inv, title);
	}
	@WrapWithCondition(
		method = "containerTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;closeContainer()V")
	)
	public boolean containerTick(Player instance) {
		return thiz().getMenu().containerId != -1;
	}
	@Override
	public void resize(@NotNull Minecraft minecraft, int width, int height) {
		super.resize(minecraft, width, height);
		ccg$popup = new RequestAmountOverlay();
	}
	@Override
	public void render(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		if (!CCG.config.misc.quickRequestActions) {
			if (ccg$popup.open) ccg$popup.close();
		} else if (ccg$popup.open) {
			super.render(gui, 0, 0, partialTick);
			ccg$popup.render(gui, mouseX, mouseY, partialTick);
			return;
		}
		super.render(gui, mouseX, mouseY, partialTick);
	}
	/** 滚动条分支仅在无修饰键时启用，使 ctrl/alt 滚动也能修改数量（原版只放行 shift） */
	@WrapOperation(
		method = "mouseScrolled",
		at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/logistics/stockTicker/StockKeeperRequestScreen;hasShiftDown()Z")
	)
	private boolean modifyMaxScroll(Operation<Boolean> original) {
		if (!CCG.config.misc.quickRequestActions) return original.call();
		return hasControlDown() || original.call() || hasAltDown();
	}
	@ModifyVariable(method = "mouseScrolled", at = @At("STORE"), name = "transfer")
	private int mouseScrolled(int transfer, @Local(name = "scrollY") double scrollY) {
		if (!CCG.config.misc.quickRequestActions) return transfer;
		return Mth.ceil(Math.abs(scrollY)) * getModifiedScrollAmount();
	}
	/** 值恰为 1 时修正步进：shift +63 / ctrl +9，结果正好对齐 64 / 10 */
	@WrapOperation(method = "mouseScrolled", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"))
	private int adjustFromOne(int transfer, int stockAvailable, Operation<Integer> original, @Local(name = "current") int current) {
		if (!CCG.config.misc.quickRequestActions) return original.call(transfer, stockAvailable);
		if (current == 1 && transfer > 1) transfer--;
		return original.call(transfer, stockAvailable);
	}
	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		if (!CCG.config.misc.quickRequestActions) {
			if (ccg$popup.open) ccg$popup.close();
			return;
		}
		if (ccg$popup.open) {
			ccg$popup.mouseClicked(mouseX, mouseY, button);
			cir.setReturnValue(true);
			return;
		}
		if (button == InputConstants.MOUSE_BUTTON_LEFT && CCGKey.stockRequestSelectAll.isDown() && ccg$applyFullAmount(mouseX, mouseY)) {
			cir.setReturnValue(true);
			return;
		}
		if (!CCGKey.stockRequestSetter.isDown()) return;
		if (ccg$openPopupForHoveredItem(mouseX, mouseY)) cir.setReturnValue(true);
	}
	@Unique
	private boolean ccg$applyFullAmount(double mouseX, double mouseY) {
		var hoveredSlot = getHoveredSlot((int) mouseX, (int) mouseY);
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
	private boolean ccg$openPopupForHoveredItem(double mouseX, double mouseY) {
		BigItemStack result = null;
		var hoveredSlot = getHoveredSlot((int) mouseX, (int) mouseY);
		if (hoveredSlot != noneHovered) {
			int group = hoveredSlot.getFirst();
			int index = hoveredSlot.getSecond();// 配方条使用自己的数量语义
			if (group != -2) {
				var entry1 = group == -1 ? thiz().itemsToOrder.get(index) : displayedItems.get(group).get(index);
				result = entry1 == null || entry1.stack.isEmpty() ? null : entry1;
			}
		}
		final var entry = result;
		if (entry == null) return false;
		var max = ccg$getAvailableMax(entry);
		var existing = getOrderForItem(entry.stack);
		ccg$popup.open(entry.stack, existing == null ? 0 : existing.count, max, count -> ccg$setOrRemoveOrder(entry.stack, count));
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
		if (!CCG.config.misc.quickRequestActions) return;
		if (!ccg$popup.open) return;
		ccg$popup.charTyped(codePoint, modifiers);
		cir.setReturnValue(true);
	}
	@Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
	private void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
		if (!CCG.config.misc.quickRequestActions) return;
		if (!ccg$popup.open) return;
		ccg$popup.keyPressed(keyCode, scanCode, modifiers);
		cir.setReturnValue(true);
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
