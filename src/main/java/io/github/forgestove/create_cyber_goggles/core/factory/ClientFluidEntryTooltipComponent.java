package io.github.forgestove.create_cyber_goggles.core.factory;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
public record ClientFluidEntryTooltipComponent(FluidStack fluid, int indent, int capacityMb, int sharedBarWidth)
	implements ClientTooltipComponent {
	private static final int H_PADDING = 4;
	private static final int BORDER_COLOR = 0xFF777777;
	public static void register(@NotNull RegisterClientTooltipComponentFactoriesEvent event) {
		event.register(
			FluidEntryTooltipComponent.class,
			data -> new ClientFluidEntryTooltipComponent(data.fluid(), data.indent(), data.capacityMb(), data.sharedBarWidth())
		);
	}
	@Override
	public int getHeight() {
		return SlotUtil.SIZE_SLIM + 2;
	}
	@Override
	public int getWidth(@NotNull Font font) {
		return indentPixels(font) + barWidth(font);
	}
	private int indentPixels(@NotNull Font font) {
		return indent * font.width(" ");
	}
	private int barWidth(@NotNull Font font) {
		var preferred = preferredBarWidth(font, fluid, capacityMb);
		return Math.max(preferred, sharedBarWidth);
	}
	public static int preferredBarWidth(@NotNull Font font, @NotNull FluidStack fluid, int capacityMb) {
		var label = buildLabel(fluid, capacityMb, Screen.hasShiftDown());
		return Math.max(SlotUtil.SIZE * 4, font.width(label) + H_PADDING * 2);
	}
	private static @NotNull Component buildLabel(@NotNull FluidStack fluid, int capacityMb, boolean showCapacity) {
		if (fluid.isEmpty()) return CCGLang.add(Component.translatable("create_cyber_goggles.tooltip.empty"))
			.space()
			.text(AmountUtil.formatFluidAmount(capacityMb))
			.component();
		var label = CCGLang.add(fluid.getHoverName()).space().text(AmountUtil.formatFluidAmount(fluid.getAmount()));
		if (showCapacity)
			return label.text(" / ", ChatFormatting.GRAY).text(AmountUtil.formatFluidAmount(capacityMb), ChatFormatting.GRAY).component();
		return label.component();
	}
	@Override
	public void renderImage(@NotNull Font font, int x, int y, @NotNull GuiGraphics gui) {
		var label = buildLabel();
		var barX = x + indentPixels(font);
		var barWidth = barWidth(font);
		renderFluidBar(gui, fluid, capacityMb, barX, y, barWidth, SlotUtil.SIZE_SLIM);
		var textX = barX + H_PADDING;
		var textY = y + Mth.floor((SlotUtil.SIZE_SLIM - font.lineHeight) / 2F) + 1;
		gui.drawString(font, label, textX, textY, 0xFFFFFFFF, true);
	}
	private @NotNull Component buildLabel() {
		return buildLabel(fluid, capacityMb, Screen.hasShiftDown());
	}
	public static void renderFluidBar(
		@NotNull GuiGraphics gui,
		@NotNull FluidStack stack,
		int capacityMb,
		int x,
		int y,
		int width,
		int height
	) {
		gui.fill(x, y, x + width, y + 1, BORDER_COLOR);
		gui.fill(x, y + height - 1, x + width, y + height, BORDER_COLOR);
		gui.fill(x, y, x + 1, y + height, BORDER_COLOR);
		gui.fill(x + width - 1, y, x + width, y + height, BORDER_COLOR);
		if (stack.isEmpty()) return;
		var innerX = x + 1;
		var innerY = y + 1;
		var innerW = Math.max(0, width - 2);
		var innerH = Math.max(0, height - 2);
		var fillRatio = Mth.clamp(stack.getAmount() / (float) Math.max(1, capacityMb), 0F, 1F);
		var fillWidth = Mth.clamp(Mth.floor(innerW * fillRatio), 1, innerW);
		renderFluid(gui, stack, innerX, innerY, fillWidth, innerH);
	}
	private static void renderFluid(@NotNull GuiGraphics gui, @NotNull FluidStack stack, int x, int y, int width, int height) {
		if (stack.isEmpty()) return;
		var ext = IClientFluidTypeExtensions.of(stack.getFluid());
		var still = ext.getStillTexture(stack);
		var sprite = mc.getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(still);
		var tint = ext.getTintColor(stack);
		var r = ARGB32.red(tint) / 255F;
		var g = ARGB32.green(tint) / 255F;
		var b = ARGB32.blue(tint) / 255F;
		var a = ARGB32.alpha(tint) / 255F;
		gui.enableScissor(x, y, x + width, y + height);
		for (var dx = 0; dx < width; dx += 16)
			for (var dy = 0; dy < height; dy += 16)
				gui.blit(x + dx, y + dy, 0, 16, 16, sprite, r, g, b, a);
		gui.disableScissor();
	}
	public record FluidEntryTooltipComponent(FluidStack fluid, int indent, int capacityMb, int sharedBarWidth) implements TooltipComponent {
		/** 兼容旧调用：共享条宽在 GatherComponents 阶段按同 tooltip 最大条宽填充 */
		public FluidEntryTooltipComponent(FluidStack fluid, int indent, int capacityMb) {
			this(fluid, indent, capacityMb, 0);
		}
	}
}
