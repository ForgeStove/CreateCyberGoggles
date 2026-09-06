package io.github.forgestove.create_cyber_goggles.compat.jei;
import com.simibubi.create.AllMenuTypes;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.redstoneRequester.*;
import com.simibubi.create.foundation.gui.menu.GhostItemSubmitPacket;
import io.github.forgestove.create_cyber_goggles.mixin.accessor.RedstoneRequesterScreenAccessor;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.*;
import mezz.jei.library.transfer.RecipeTransferErrorTooltip;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.*;

import java.util.*;
/**
 * 让红石请求器支持 JEI 配方转移按钮，可用 Alt 切换填入方式：
 * 默认（动力合成器）——按配方格子顺序（先从左到右再从上到下）扫描，空位跳过，
 * 连续同类合并成一组（数量 = 连续格数），批量请求原料供动力合成器铺料；
 * 按住 Alt（原版合成器）——按 3x3 格子位置逐格填入，空位留空，每格数量固定 1，保留配方形状。
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
		// 按住 Alt 以原版合成器方式填入，否则默认以动力合成器方式填入
		var recipe = recipeHolder.value();
		var groups = Screen.hasAltDown() ? vanillaStyleGroups(recipe) : mechanicalStyleGroups(recipe);
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
				amounts.set(i, i < groups.size() && groups.get(i) != null ? groups.get(i).count : 1);
		}
		return null;
	}
	/** 原版合成器方式：按配方格子逐格填入（行优先），空位用 null 占位留空，每格数量固定为 1 */
	private static List<BigItemStack> vanillaStyleGroups(Recipe<?> recipe) {
		List<BigItemStack> groups = new ArrayList<>();
		for (var ingredient : recipe.getIngredients()) {
			if (ingredient.isEmpty()) {
				groups.add(null); // 空位占槽留空
				continue;
			}
			var matches = ingredient.getItems();
			if (matches.length == 0) {
				groups.add(null);
				continue;
			}
			groups.add(new BigItemStack(matches[0].copyWithCount(1), 1));
		}
		return groups;
	}
	/** 动力合成器方式：连续同类合并成一组（数量 = 连续格数），空位只跳过不打断连续 */
	private static List<BigItemStack> mechanicalStyleGroups(Recipe<?> recipe) {
		List<BigItemStack> groups = new ArrayList<>();
		BigItemStack currentGroup = null;
		for (var ingredient : recipe.getIngredients()) {
			if (ingredient.isEmpty()) continue; // 空位跳过，不打断当前连续组
			var matches = ingredient.getItems();
			if (matches.length == 0) {
				currentGroup = null; // 该格原料无配方候选 → 断开连续
				continue;
			}
			var representative = matches[0];
			if (currentGroup != null && ItemStack.isSameItemSameComponents(currentGroup.stack, representative)) currentGroup.count++;
			else {
				currentGroup = new BigItemStack(representative.copyWithCount(1), 1);
				groups.add(currentGroup);
			}
		}
		return groups;
	}
}
