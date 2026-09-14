package io.github.forgestove.create_cyber_goggles.compat.jei;
import com.simibubi.create.AllMenuTypes;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.redstoneRequester.*;
import com.simibubi.create.foundation.gui.menu.GhostItemSubmitPacket;
import io.github.forgestove.create_cyber_goggles.mixin.accessor.RedstoneRequesterScreenAccessor;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.*;
import mezz.jei.library.transfer.RecipeTransferErrorTooltip;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.*;

import java.util.*;
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
		var groups = Screen.hasAltDown() ? vanillaStyleGroups(recipe, maxTransfer) : mechanicalStyleGroups(recipe);
		var slots = container.ghostInventory.getSlots();
		if (groups.size() > slots)
			return new RecipeTransferErrorTooltip(Component.translatable("create_cyber_goggles.gui.redstoneRequester.tooManyIngredients"));
		// 不实际转移时返回 COSMETIC 错误
		if (!doTransfer) return AltHintError.INSTANCE;
		// 填入请求槽并同步服务端
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
	private static List<BigItemStack> vanillaStyleGroups(Recipe<?> recipe, boolean maxTransfer) {
		List<BigItemStack> groups = new ArrayList<>();
		var count = maxTransfer ? 64 : 1;
		for (var ingredient : recipe.getIngredients()) {
			if (ingredient.isEmpty()) {
				groups.add(null);
				continue;
			}
			var matches = ingredient.getItems();
			if (matches.length == 0) {
				groups.add(null);
				continue;
			}
			groups.add(new BigItemStack(matches[0].copyWithCount(1), count));
		}
		return groups;
	}
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
			if (currentGroup == null || !ItemStack.isSameItemSameComponents(currentGroup.stack, representative)) {
				currentGroup = new BigItemStack(representative.copyWithCount(1), 1);
				groups.add(currentGroup);
			} else currentGroup.count++;
		}
		return groups;
	}
	private static final class AltHintError implements IRecipeTransferError {
		private static final AltHintError INSTANCE = new AltHintError();
		@Override
		public @NotNull Type getType() {
			return Type.COSMETIC;
		}
		@Override
		public int getButtonHighlightColor() {
			return 0; // 禁用默认的高亮
		}
		@Override
		public void getTooltip(ITooltipBuilder tooltip) {
			tooltip.add(Component.translatable("jei.tooltip.transfer"));
			tooltip.add(Component.translatable("create_cyber_goggles.gui.redstoneRequester.jeiHint").withStyle(ChatFormatting.DARK_GRAY));
		}
	}
}
