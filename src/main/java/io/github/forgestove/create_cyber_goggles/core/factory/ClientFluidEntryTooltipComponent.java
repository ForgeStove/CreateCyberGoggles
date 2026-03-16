package io.github.forgestove.create_cyber_goggles.core.factory;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.forgestove.create_cyber_goggles.core.util.CCGLang;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
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
public record ClientFluidEntryTooltipComponent(FluidStack fluid, int indent, int capacityMb, int sharedBarWidth)
	implements ClientTooltipComponent {
	private static final int SLOT_WIDTH = 18;
	private static final int MIN_WIDTH = SLOT_WIDTH * 4;
	private static final int SLOT_HEIGHT = 14;
	private static final int H_PADDING = 4;
	private static final int BORDER_COLOR = 0xFF777777;
	public static void register(@NotNull RegisterClientTooltipComponentFactoriesEvent event) {
		event.register(
			FluidEntryTooltipComponent.class,
			data -> new ClientFluidEntryTooltipComponent(data.fluid(), data.indent(), data.capacityMb(), 0)
		);
	}
	public static void renderFluidBar(
		@NotNull GuiGraphics guiGraphics,
		@NotNull FluidStack stack,
		int capacityMb,
		int x,
		int y,
		int width,
		int height
	) {
		guiGraphics.fill(x, y, x + width, y + 1, BORDER_COLOR);
		guiGraphics.fill(x, y + height - 1, x + width, y + height, BORDER_COLOR);
		guiGraphics.fill(x, y, x + 1, y + height, BORDER_COLOR);
		guiGraphics.fill(x + width - 1, y, x + width, y + height, BORDER_COLOR);
		if (stack.isEmpty()) return;
		var innerX = x + 1;
		var innerY = y + 1;
		var innerW = Math.max(0, width - 2);
		var innerH = Math.max(0, height - 2);
		var fillRatio = Mth.clamp(stack.getAmount() / (float) Math.max(1, capacityMb), 0F, 1F);
		var fillWidth = Mth.clamp(Mth.floor(innerW * fillRatio), 1, innerW);
		renderFluid(guiGraphics, stack, innerX, innerY, fillWidth, innerH);
	}
	private static void renderFluid(@NotNull GuiGraphics guiGraphics, @NotNull FluidStack stack, int x, int y, int width, int height) {
		if (stack.isEmpty()) return;
		var ext = IClientFluidTypeExtensions.of(stack.getFluid());
		var still = ext.getStillTexture(stack);
		var sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(still);
		var tint = ext.getTintColor(stack);
		var r = ARGB32.red(tint) / 255F;
		var g = ARGB32.green(tint) / 255F;
		var b = ARGB32.blue(tint) / 255F;
		var a = ARGB32.alpha(tint) / 255F;
		RenderSystem.enableBlend();
		RenderSystem.setShaderColor(r, g, b, a);
		guiGraphics.enableScissor(x, y, x + width, y + height);
		for (var dx = 0; dx < width; dx += 16)
			for (var dy = 0; dy < height; dy += 16)
				guiGraphics.blit(x + dx, y + dy, 0, 16, 16, sprite);
		guiGraphics.disableScissor();
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
	}
	public static @NotNull String formatFluidAmount(int amountMb) {
		if (amountMb < 1000) return amountMb + "mB";
		if (amountMb % 1000 == 0) return amountMb / 1000 + "B";
		var value = amountMb / 1000F;
		return CCGLang.number(value).string() + "B";
	}
	public static int preferredBarWidth(@NotNull Font font, @NotNull FluidStack fluid, int capacityMb) {
		var label = buildLabel(fluid, capacityMb, Screen.hasShiftDown());
		return Math.max(MIN_WIDTH, font.width(label) + H_PADDING * 2);
	}
	private static @NotNull Component buildLabel(@NotNull FluidStack fluid, int capacityMb, boolean showCapacity) {
		if (fluid.isEmpty()) return CCGLang.translate("tooltip.empty")
			.component()
			.copy()
			.append(" ")
			.append(Component.literal(formatFluidAmount(capacityMb)));
		var label = fluid.getHoverName().copy().append(" ").append(Component.literal(formatFluidAmount(fluid.getAmount())));
		if (showCapacity) return label.append(Component.literal(" / ").withStyle(ChatFormatting.DARK_GRAY))
			.append(Component.literal(formatFluidAmount(capacityMb)).withStyle(ChatFormatting.DARK_GRAY));
		return label;
	}
	private @NotNull Component buildLabel() {
		return buildLabel(fluid, capacityMb, Screen.hasShiftDown());
	}
	@Override
	public int getHeight() {
		return SLOT_HEIGHT + 2;
	}
	@Override
	public int getWidth(@NotNull Font font) {
		return indentPixels(font) + barWidth(font);
	}
	@Override
	public void renderImage(@NotNull Font font, int x, int y, @NotNull GuiGraphics guiGraphics) {
		var label = buildLabel();
		var barX = x + indentPixels(font);
		var barWidth = barWidth(font);
		renderFluidBar(guiGraphics, fluid, capacityMb, barX, y, barWidth, SLOT_HEIGHT);
		var textX = barX + H_PADDING;
		var textY = y + Mth.floor((SLOT_HEIGHT - font.lineHeight) / 2F) + 1;
		guiGraphics.drawString(font, label, textX, textY, 0xFFFFFFFF, true);
	}
	private int barWidth(@NotNull Font font) {
		var preferred = preferredBarWidth(font, fluid, capacityMb);
		return Math.max(preferred, sharedBarWidth);
	}
	private int indentPixels(@NotNull Font font) {
		return indent * font.width(" ");
	}
	public record FluidEntryTooltipComponent(FluidStack fluid, int indent, int capacityMb) implements TooltipComponent {}
}
