package io.github.forgestove.create_cyber_goggles.compat.emi;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.redstoneRequester.*;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.*;
import dev.emi.emi.api.recipe.handler.*;
import dev.emi.emi.api.stack.*;
import dev.emi.emi.api.widget.RecipeFillButtonWidget;
import dev.emi.emi.registry.EmiRecipeFiller;
import io.github.forgestove.create_cyber_goggles.compat.RedstoneRequesterFill;
import io.github.forgestove.create_cyber_goggles.foundation.lang.CCGLang;
import io.github.forgestove.create_cyber_goggles.mixin.accessor.RecipeButtonWidgetAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.*;
import org.joml.Matrix4f;

import java.util.*;
public class RedstoneRequesterEmiHandler implements EmiRecipeHandler<RedstoneRequesterMenu> {
	public static final RedstoneRequesterEmiHandler INSTANCE = new RedstoneRequesterEmiHandler();
	private static List<ItemStack> toRepresentatives(EmiRecipe recipe) {
		List<ItemStack> reps = new ArrayList<>();
		for (EmiIngredient input : recipe.getInputs()) {
			if (input.isEmpty()) {
				reps.add(ItemStack.EMPTY);
				continue;
			}
			ItemStack stack = input.getEmiStacks().getFirst().getItemStack();
			reps.add(stack.isEmpty() ? null : stack);
		}
		return reps;
	}
	public static boolean fits(EmiRecipe recipe, RedstoneRequesterMenu container, boolean vanillaStyle) {
		List<ItemStack> reps = toRepresentatives(recipe);
		List<BigItemStack> groups = vanillaStyle
			? RedstoneRequesterFill.vanillaStyleGroups(reps, false)
			: RedstoneRequesterFill.mechanicalStyleGroups(reps, false);
		return groups.size() <= RedstoneRequesterFill.slotCount(container);
	}
	@Override
	public boolean supportsRecipe(EmiRecipe recipe) {
		return !recipe.getInputs().isEmpty();
	}
	/** @return {@code null} 表示按钮不属于红石请求器 */
	public static @Nullable Boolean fillState(Object button) {
		if (!(EmiApi.getHandledScreen() instanceof RedstoneRequesterScreen screen)) return null;
		EmiRecipe recipe = ((RecipeButtonWidgetAccessor) button).getRecipe();
		if (EmiRecipeFiller.getFirstValidHandler(recipe, screen) != INSTANCE) return null;
		return fits(recipe, screen.getMenu(), Screen.hasAltDown());
	}
	@Override
	public EmiPlayerInventory getInventory(AbstractContainerScreen<RedstoneRequesterMenu> screen) {
		List<EmiStack> list = new ArrayList<>();
		for (Slot slot : screen.getMenu().slots)
			if (slot.container instanceof Inventory) list.add(EmiStack.of(slot.getItem()));
		return new EmiPlayerInventory(list);
	}
	@Override
	public boolean canCraft(EmiRecipe recipe, EmiCraftContext<RedstoneRequesterMenu> context) {
		return fits(recipe, context.getScreenHandler(), Screen.hasAltDown());
	}
	@Override
	public boolean craft(EmiRecipe recipe, EmiCraftContext<RedstoneRequesterMenu> context) {
		List<ItemStack> reps = toRepresentatives(recipe);
		boolean maxTransfer = context.getAmount() > 1;
		List<BigItemStack> groups = Screen.hasAltDown()
			? RedstoneRequesterFill.vanillaStyleGroups(reps, maxTransfer)
			: RedstoneRequesterFill.mechanicalStyleGroups(reps, maxTransfer);
		RedstoneRequesterMenu container = context.getScreenHandler();
		if (groups.size() > RedstoneRequesterFill.slotCount(container)) return false;
		RedstoneRequesterFill.applyGroups(container, groups);
		return true;
	}
	@Override
	public List<ClientTooltipComponent> getTooltip(EmiRecipe recipe, EmiCraftContext<RedstoneRequesterMenu> context) {
		return List.of(new FillHintTooltip(recipe, context.getScreenHandler()));
	}
	/**
	 * EMI 在构造 {@link RecipeFillButtonWidget} 时缓存了 {@code Tooltip}，导致按键状态不会更新
	 * <p>
	 * 这里让内容在渲染时更新按键状态。
	 */
	private record FillHintTooltip(EmiRecipe recipe, RedstoneRequesterMenu container) implements ClientTooltipComponent {
		private List<Component> lines() {
			if (!fits(recipe, container, Screen.hasAltDown()))
				return List.of(CCGLang.translateDirect("gui.redstoneRequester.tooManyIngredients", ChatFormatting.RED));
			return RedstoneRequesterFill.hints();
		}
		@Override
		public int getHeight() {
			return 10 * lines().size();
		}
		@Override
		public int getWidth(@NotNull Font font) {
			return lines().stream().mapToInt(font::width).max().orElse(0);
		}
		@Override
		public void renderText(@NotNull Font font, int x, int y, @NotNull Matrix4f matrix, @NotNull BufferSource bufferSource) {
			for (Component line : lines()) {
				font.drawInBatch(line.getVisualOrderText(), x, y, -1, true, matrix, bufferSource, DisplayMode.NORMAL, 0, 0xF000F0);
				y += 10;
			}
		}
	}
}
