package io.github.forgestove.create_cyber_goggles.core.factory;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.forgestove.create_cyber_goggles.core.util.CCGLang;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import org.jetbrains.annotations.NotNull;
public record ClientFluidEntryTooltipComponent(FluidStack fluid, int indent, long capacityMb, int sharedBarWidth)
	implements ClientTooltipComponent {
	private static final int SLOT_WIDTH = 18;
	private static final int MIN_WIDTH = SLOT_WIDTH * 4;
	private static final int SLOT_HEIGHT = 14;
	private static final int H_PADDING = 4;
	private static final int BORDER_COLOR = 0xFF777777;
	public static void register() {
		TooltipComponentCallback.EVENT.register(data -> {
			if (data instanceof FluidEntryTooltipComponent fluidEntry)
				return new ClientFluidEntryTooltipComponent(fluidEntry.fluid(), fluidEntry.indent(), fluidEntry.capacityMb(), 0);
			return null;
		});
	}
	public static void renderFluidBar(
		@NotNull GuiGraphics guiGraphics,
		FluidStack stack,
		long capacityMb,
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
	@SuppressWarnings("UnstableApiUsage")
	public static void renderFluid(@NotNull GuiGraphics guiGraphics, FluidStack stack, int x, int y, int width, int height) {
		if (stack.isEmpty() || width <= 0 || height <= 0) return;
		var sprite = FluidVariantRendering.getSprite(stack.getType());
		if (sprite == null) return;
		var tint = FluidVariantRendering.getColor(stack.getType());
		var alpha = (tint >>> 24 & 0xFF) / 255.0F;
		if (alpha <= 0.0F) alpha = 1.0F;
		var red = (tint >>> 16 & 0xFF) / 255.0F;
		var green = (tint >>> 8 & 0xFF) / 255.0F;
		var blue = (tint & 0xFF) / 255.0F;
		RenderSystem.enableBlend();
		RenderSystem.setShaderColor(red, green, blue, alpha);
		var tileSize = 16;
		for (var offsetY = 0; offsetY < height; offsetY += tileSize) {
			var drawHeight = Math.min(tileSize, height - offsetY);
			for (var offsetX = 0; offsetX < width; offsetX += tileSize) {
				var drawWidth = Math.min(tileSize, width - offsetX);
				var pose = guiGraphics.pose().last().pose();
				var u0 = sprite.getU0();
				var v0 = sprite.getV0();
				var u1 = sprite.getU(drawWidth);
				var v1 = sprite.getV(drawHeight);
				RenderSystem.setShader(GameRenderer::getPositionTexShader);
				RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
				var buffer = Tesselator.getInstance().getBuilder();
				buffer.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
				buffer.vertex(pose, x + offsetX, y + offsetY + drawHeight, 0.0F).uv(u0, v1).endVertex();
				buffer.vertex(pose, x + offsetX + drawWidth, y + offsetY + drawHeight, 0.0F).uv(u1, v1).endVertex();
				buffer.vertex(pose, x + offsetX + drawWidth, y + offsetY, 0.0F).uv(u1, v0).endVertex();
				buffer.vertex(pose, x + offsetX, y + offsetY, 0.0F).uv(u0, v0).endVertex();
				BufferUploader.drawWithShader(buffer.end());
			}
		}
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	}
	public static @NotNull String formatFluidAmount(long amountMb) {
		if (amountMb < 1000) return amountMb + "mB";
		if (amountMb % 1000 == 0) return amountMb / 1000 + "B";
		var value = amountMb / 1000F;
		return CCGLang.number(value).string() + "B";
	}
	public static int preferredBarWidth(@NotNull Font font, FluidStack fluid, long capacityMb) {
		var label = buildLabel(fluid, capacityMb, Screen.hasShiftDown());
		return Math.max(MIN_WIDTH, font.width(label) + H_PADDING * 2);
	}
	private static @NotNull Component buildLabel(FluidStack fluid, long capacityMb, boolean showCapacity) {
		if (fluid.isEmpty()) return CCGLang.translate("tooltip.empty")
			.component()
			.copy()
			.append(" ")
			.append(Component.literal(formatFluidAmount(capacityMb)));
		var label = fluid.getDisplayName().copy().append(" ").append(Component.literal(formatFluidAmount(fluid.getAmount())));
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
	public record FluidEntryTooltipComponent(FluidStack fluid, int indent, long capacityMb) implements TooltipComponent {}
}
