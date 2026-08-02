package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.mojang.blaze3d.platform.InputConstants;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.*;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts.CraftingEntry;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.api.Self;
import io.github.forgestove.create_cyber_goggles.core.event.CCGKey;
import io.github.forgestove.create_cyber_goggles.core.factory.StockRequestAmountOverlay;
import net.createmod.catnip.data.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.CraftingRecipe;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.awt.Rectangle;
import java.util.*;
import java.util.function.Function;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
@Mixin(StockKeeperRequestScreen.class)
public abstract class StockKeeperRequestScreenMixin implements Self<StockKeeperRequestScreen> {
	@Unique private final StockRequestAmountOverlay ccg$popup = new StockRequestAmountOverlay();
	@Shadow public List<BigItemStack> itemsToOrder;
	@Shadow public List<CraftableBigItemStack> recipesToOrder;
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
	private void mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
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
	private void mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY, CallbackInfoReturnable<Boolean> cir) {
		if (!CCG.config.misc.stockRequestQuickActions) return;
		if (ccg$popup.isOpen()) cir.setReturnValue(true);
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
	private void renderPopup(GuiGraphics gui, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
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
	private Couple<Integer> suppressTooltipsWhenPopup(
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
	/** 仓库管理员发包默认去掉 ordered_crafts（改普通物品请求），使理包机不吞物品 */
	@ModifyArg(
		method = "sendIt", at = @At(
		value = "INVOKE",
		target = "Lcom/simibubi/create/content/logistics/stockTicker/PackageOrderWithCrafts;<init>("
			+ "Lcom/simibubi/create/content/logistics/stockTicker/PackageOrder;Ljava/util/List;)V"
	), index = 1
	)
	private List<CraftingEntry> plainRequest(List<CraftingEntry> orderedCrafts) {
		return CCG.config.misc.jei.plainRequest ? List.of() : orderedCrafts;
	}
	/** 普通请求模式下 ordered_stacks 按配方连续组生成（与红石请求器一致，理包机输出有序） */
	@ModifyArg(
		method = "sendIt", at = @At(
		value = "INVOKE",
		target = "Lcom/simibubi/create/content/logistics/stockTicker/PackageOrderWithCrafts;<init>("
			+ "Lcom/simibubi/create/content/logistics/stockTicker/PackageOrder;Ljava/util/List;)V"
	), index = 0
	)
	private PackageOrder orderedByRecipe(PackageOrder orderedStacks) {
		if (!CCG.config.misc.jei.plainRequest) return orderedStacks;
		var groups = ccg$recipeGroups();
		return groups.isEmpty() ? orderedStacks : new PackageOrder(groups);
	}
	@Unique
	private List<BigItemStack> ccg$recipeGroups() {
		List<BigItemStack> groups = new ArrayList<>();
		BigItemStack currentGroup = null;
		for (var cbis : recipesToOrder) {
			if (!(cbis.recipe instanceof CraftingRecipe cr)) continue;
			for (var ingredient : cr.getIngredients()) {
				if (ingredient.isEmpty()) continue;
				// 优先用实际请求的物品（与包裹内容一致，避免理包机按 tag 首个物品匹配不上而漏物品）
				ItemStack rep = null;
				for (var ordered : itemsToOrder) {
					if (!ingredient.test(ordered.stack)) continue;
					rep = ordered.stack;
					break;
				}
				if (rep == null) {
					var matches = ingredient.getItems();
					if (matches.length > 0) rep = matches[0];
				}
				if (rep == null) continue;
				if (currentGroup != null && ItemStack.isSameItemSameComponents(currentGroup.stack, rep)) currentGroup.count++;
				else {
					currentGroup = new BigItemStack(rep.copyWithCount(1), 1);
					groups.add(currentGroup);
				}
			}
		}
		return groups;
	}
	/** 网络物品过多时 maxCraftable 遍历全量会卡顿；把快照过滤为配方相关物品再计算 */
	@WrapOperation(
		method = "requestCraftable", at = @At(
		value = "INVOKE",
		target = "Lcom/simibubi/create/content/logistics/stockTicker/StockKeeperRequestScreen;maxCraftable("
			+ "Lcom/simibubi/create/content/logistics/stockTicker/CraftableBigItemStack;"
			+ "Lcom/simibubi/create/content/logistics/packager/InventorySummary;"
			+ "Ljava/util/function/Function;I)"
			+ "Lnet/createmod/catnip/data/Pair;"
	)
	)
	private Pair<Integer, List<List<BigItemStack>>> filterMaxCraftable(
		StockKeeperRequestScreen instance,
		CraftableBigItemStack cbis,
		InventorySummary availableItems,
		Function<ItemStack, Integer> countModifier,
		int newTypeLimit,
		Operation<Pair<Integer, List<List<BigItemStack>>>> original
	) {
		Set<Item> items = new HashSet<>();
		for (var ingredient : cbis.getIngredients()) {
			if (ingredient.isEmpty()) continue;
			for (var match : ingredient.getItems()) items.add(match.getItem());
		}
		if (items.isEmpty()) return original.call(instance, cbis, availableItems, countModifier, newTypeLimit);
		var filtered = new InventorySummary();
		for (var item : items) {
			var list = availableItems.getItemMap().get(item);
			if (list == null) continue;
			for (var entry : list) filtered.add(entry.stack, entry.count);
		}
		return original.call(instance, cbis, filtered, countModifier, newTypeLimit);
	}
	/** create 的 resolveIngredientAmounts 按数量逐 1 递减，物品数量巨大时 O(总量) 卡顿；改为按共享引用数一次分摊 */
	@Inject(method = "resolveIngredientAmounts", at = @At("HEAD"), cancellable = true)
	private void fastResolve(List<List<BigItemStack>> validIngredients, CallbackInfoReturnable<List<List<BigItemStack>>> cir) {
		Map<BigItemStack, Integer> shareCount = new IdentityHashMap<>();
		for (var list : validIngredients)
			for (BigItemStack entry : list)
				shareCount.merge(entry, 1, Integer::sum);
		List<List<BigItemStack>> resolved = new ArrayList<>(validIngredients.size());
		for (var list : validIngredients) {
			List<BigItemStack> resolvedList = new ArrayList<>(list.size());
			for (var entry : list)
				resolvedList.add(new BigItemStack(entry.stack, entry.count / shareCount.get(entry)));
			resolved.add(resolvedList);
		}
		cir.setReturnValue(resolved);
	}
}
