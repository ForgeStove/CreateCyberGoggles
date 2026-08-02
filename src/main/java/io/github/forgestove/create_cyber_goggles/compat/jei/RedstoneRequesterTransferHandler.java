package io.github.forgestove.create_cyber_goggles.compat.jei;
import com.simibubi.create.AllMenuTypes;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.redstoneRequester.*;
import com.simibubi.create.foundation.gui.menu.GhostItemSubmitPacket;
import io.github.forgestove.create_cyber_goggles.api.*;
import io.github.forgestove.create_cyber_goggles.mixin.accessor.RedstoneRequesterScreenAccessor;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.*;
import mezz.jei.library.transfer.RecipeTransferErrorTooltip;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.*;

import java.util.*;
/**
 * 让红石请求器支持 JEI 配方转移按钮：按配方格子顺序（从上到下、从左到右）扫描，
 * 把连续相同的原料合并成一组，每组填入一个请求槽（数量 = 连续格数），
 * 使动力合成器能按配方顺序正确铺料。
 */
public class RedstoneRequesterTransferHandler implements IUniversalRecipeTransferHandler<RedstoneRequesterMenu> {
	@Override
	public @NotNull Class<? extends RedstoneRequesterMenu> getContainerClass() {
		return RedstoneRequesterMenu.class;
	}
	@Override
	public @NotNull Optional<MenuType<RedstoneRequesterMenu>> getMenuType() {
		return Optional.of(AllMenuTypes.REDSTONE_REQUESTER.get());
	}
	@Override
	public @Nullable IRecipeTransferError transferRecipe(
		@NotNull RedstoneRequesterMenu container,
		@NotNull Object object,
		@NotNull IRecipeSlotsView recipeSlots,
		@NotNull Player player,
		boolean maxTransfer,
		boolean doTransfer
	) {
		if (!(object instanceof RecipeHolder<?> recipeHolder)) return null;
		// 优先用红石请求器的客户端网络库存快照，其次回退精确缓存
		var summary = container.contentHolder instanceof StockSnapshotHolder holder ? holder.ccg$getStockSnapshot() : null;
		if (summary == null || summary.getStacks().isEmpty()) summary = container.contentHolder.getAccurateSummary();
		var hasStock = summary != null && !summary.getStacks().isEmpty();
		// 按配方 grid 顺序扫描（getIngredients 行优先含空槽），网络库存匹配代表物品，连续同类合并
		List<BigItemStack> groups = new ArrayList<>();
		BigItemStack currentGroup = null;
		for (var ingredient : recipeHolder.value().getIngredients()) {
			if (ingredient.isEmpty()) continue;
			ItemStack representative = null;
			// 网络库存里匹配该原料的物品优先（解决 tag 轮播取到不在库存的物品）
			if (hasStock) for (var stock : summary.getStacks())
				if (stock.count > 0 && ingredient.test(stock.stack)) {
					representative = stock.stack;
					break;
				}
			if (representative == null) {
				// 缺货时仍按配方候选填入，保证请求配置完整（是否部分发送由服务端 allowPartial 决定）
				var matches = ingredient.getItems();
				if (matches.length > 0) representative = matches[0];
			}
			if (representative == null) {
				currentGroup = null; // 该格原料既不在网络也不在配方候选 → 断开连续
				continue;
			}
			if (currentGroup != null && ItemStack.isSameItemSameComponents(currentGroup.stack, representative)) currentGroup.count++;
			else {
				currentGroup = new BigItemStack(representative.copyWithCount(1), 1);
				groups.add(currentGroup);
			}
		}
		var slots = container.ghostInventory.getSlots();
		if (groups.size() > slots)
			return new RecipeTransferErrorTooltip(Component.translatable("create_cyber_goggles.gui.redstoneRequester.tooManyIngredients"));
		if (!doTransfer) return null;
		// 填入请求槽并同步服务端（每格物品 count=1，数量由 amounts 决定，避免与 amounts 渲染叠加假数量）
		for (var i = 0; i < slots; i++) {
			var group = i < groups.size() ? groups.get(i) : null;
			var stack = group != null ? group.stack.copyWithCount(1) : ItemStack.EMPTY;
			container.ghostInventory.setStackInSlot(i, stack);
			CatnipServices.NETWORK.sendToServer(new GhostItemSubmitPacket(stack, i));
		}
		// 通过菜单关联的 Screen 同步请求数量显示和最终请求数量
		if (container instanceof ScreenReferenced referenced
			&& referenced.ccg$getScreenReference() instanceof RedstoneRequesterScreen screen) {
			var amounts = ((RedstoneRequesterScreenAccessor) screen).getAmounts();
			for (var i = 0; i < amounts.size(); i++)
				amounts.set(i, i < groups.size() ? groups.get(i).count : 1);
		}
		return null;
	}
}
