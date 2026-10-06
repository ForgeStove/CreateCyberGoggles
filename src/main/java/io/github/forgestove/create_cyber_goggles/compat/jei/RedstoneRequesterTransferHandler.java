package io.github.forgestove.create_cyber_goggles.compat.jei;
import com.simibubi.create.AllMenuTypes;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterMenu;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.transfer.*;
import mezz.jei.library.transfer.RecipeTransferErrorTooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.*;

import java.util.Optional;

import static io.github.forgestove.create_cyber_goggles.compat.RedstoneRequesterFill.*;
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
		var reps = representatives(recipeHolder.value());
		var groups = Screen.hasAltDown() ? vanillaStyleGroups(reps, maxTransfer) : mechanicalStyleGroups(reps, maxTransfer);
		if (groups.size() > slotCount(container))
			return new RecipeTransferErrorTooltip(Component.translatable("create_cyber_goggles.gui.redstoneRequester.tooManyIngredients"));
		// 不实际转移时返回 COSMETIC 错误
		if (!doTransfer) return AltHintError.INSTANCE;
		applyGroups(container, groups);
		return null;
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
			hints().forEach(tooltip::add);
		}
	}
}
