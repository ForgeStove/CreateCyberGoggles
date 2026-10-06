package io.github.forgestove.create_cyber_goggles.compat.emi;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterMenu;
import dev.emi.emi.EmiPort;
import dev.emi.emi.api.recipe.*;
import dev.emi.emi.api.recipe.handler.*;
import dev.emi.emi.api.stack.EmiStack;
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
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.compat.RedstoneRequesterFill.*;
public final class RedstoneRequesterEmiHandler implements EmiRecipeHandler<RedstoneRequesterMenu> {
	public static final RedstoneRequesterEmiHandler INSTANCE = new RedstoneRequesterEmiHandler();
	@Override
	public EmiPlayerInventory getInventory(AbstractContainerScreen<RedstoneRequesterMenu> screen) {
		List<EmiStack> list = new ArrayList<>();
		for (Slot slot : screen.getMenu().slots)
			if (slot.container instanceof Inventory) {
				ItemStack item = slot.getItem();
				EmiStack emiStack = EmiStack.of(item);
				list.add(emiStack);
			}
		return new EmiPlayerInventory(list);
	}
	@Override
	public boolean supportsRecipe(EmiRecipe recipe) {
		return !recipe.getInputs().isEmpty();
	}
	@Override
	public boolean craft(EmiRecipe recipe, EmiCraftContext<RedstoneRequesterMenu> context) {
		var reps = toRepresentatives(recipe);
		var maxTransfer = context.getAmount() > 1;
		var groups = Screen.hasAltDown() ? vanillaStyleGroups(reps, maxTransfer) : mechanicalStyleGroups(reps, maxTransfer);
		var container = context.getScreenHandler();
		if (groups.size() > slotCount(container)) return false;
		applyGroups(container, groups);
		return true;
	}
	/** 空位为 {@link ItemStack#EMPTY}，有原料但无候选项为 {@code null} */
	private static List<ItemStack> toRepresentatives(EmiRecipe recipe) {
		List<ItemStack> reps = new ArrayList<>();
		for (var input : recipe.getInputs()) {
			var stacks = input.getEmiStacks();
			if (stacks.isEmpty()) {
				reps.add(ItemStack.EMPTY);
				continue;
			}
			var stack = stacks.getFirst().getItemStack();
			reps.add(stack.isEmpty() ? null : stack);
		}
		return reps;
	}
	@Override
	public List<ClientTooltipComponent> getTooltip(EmiRecipe recipe, EmiCraftContext<RedstoneRequesterMenu> context) {
		if (!canCraft(recipe, context)) return List.of(ClientTooltipComponent.create(EmiPort.ordered(Component.translatable(
			"create_cyber_goggles.gui.redstoneRequester.tooManyIngredients").withStyle(ChatFormatting.RED))));
		return List.of(new AltHintTooltip(0), new AltHintTooltip(1));
	}
	@Override
	public boolean canCraft(EmiRecipe recipe, EmiCraftContext<RedstoneRequesterMenu> context) {
		return vanillaStyleGroups(toRepresentatives(recipe), false).size() <= slotCount(context.getScreenHandler());
	}
	/**
	 * EMI 在构造 {@code RecipeFillButtonWidget} 时就把 tooltip 缓存了下来，普通 Component 会定格在那一刻的按键状态；
	 * 这里让内容在渲染时求值，才能随 Alt 的按下/松开设色。
	 */
	private record AltHintTooltip(int line) implements ClientTooltipComponent {
		@Override
		public int getHeight() {
			return 10;
		}
		@Override
		public int getWidth(@NotNull Font font) {
			return font.width(hints().get(line));
		}
		@Override
		public void renderText(@NotNull Font font, int x, int y, @NotNull Matrix4f matrix, @NotNull BufferSource bufferSource) {
			font.drawInBatch(hints().get(line).getVisualOrderText(), x, y, -1, true, matrix, bufferSource, DisplayMode.NORMAL, 0,
				0xF000F0);
		}
	}
}
