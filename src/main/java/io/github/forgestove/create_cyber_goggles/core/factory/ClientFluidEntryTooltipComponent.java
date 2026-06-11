package io.github.forgestove.create_cyber_goggles.core.factory;
import com.zurrtum.create.client.AllFluidConfigs;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.material.FlowingFluid;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
public record ClientFluidEntryTooltipComponent(FluidStack fluid, int indent, int capacityMb, int sharedBarWidth)
	implements ClientTooltipComponent {
	private static final int H_PADDING = 4;
	private static final int BORDER_COLOR = 0xFF777777;
	public static void renderFluidBar(
		@NotNull GuiGraphicsExtractor gui,
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
	private static void renderFluid(@NotNull GuiGraphicsExtractor gui, @NotNull FluidStack stack, int x, int y, int width, int height) {
		if (stack.isEmpty()) return;
		var fluid = stack.getFluid();
		var tintSource = AllFluidConfigs.TINT.get(fluid);
		if (tintSource == null) return;
		var tint = tintSource.get(fluid, stack.getComponentChanges()) | 0xFF000000;
		if (!(fluid instanceof FlowingFluid flowingFluid)) return;
		var model = AllFluidConfigs.MODEL.get(flowingFluid);
		if (model == null) return;
		var spriteId = model.stillMaterial().sprite();
		gui.enableScissor(x, y, x + width, y + height);
		for (var dx = 0; dx < width; dx += 16)
			for (var dy = 0; dy < height; dy += 16)
				gui.blitSprite(RenderPipelines.GUI_TEXTURED, spriteId, x + dx, y + dy, 16, 16, tint);
		gui.disableScissor();
	}
	public static @NotNull String formatFluidAmount(int amountMb) {
		if (amountMb < 1000) return amountMb + "mB";
		if (amountMb % 1000 == 0) return amountMb / 1000 + "B";
		var value = amountMb / 1000F;
		return CCGLang.number(value).string() + "B";
	}
	public static int preferredBarWidth(@NotNull Font font, @NotNull FluidStack fluid, int capacityMb) {
		var label = buildLabel(fluid, capacityMb, hasShiftDown());
		return Math.max(SlotUtil.SIZE * 4, font.width(label) + H_PADDING * 2);
	}
	private static @NotNull Component buildLabel(@NotNull FluidStack fluid, int capacityMb, boolean showCapacity) {
		if (fluid.isEmpty()) return CCGLang.translate("tooltip.empty")
			.component()
			.copy()
			.append(" ")
			.append(Component.literal(formatFluidAmount(capacityMb)));
		var label = fluid.getName().copy().append(" ").append(Component.literal(formatFluidAmount(fluid.getAmount())));
		if (showCapacity) return label.append(Component.literal(" / ").withStyle(ChatFormatting.GRAY))
			.append(Component.literal(formatFluidAmount(capacityMb)).withStyle(ChatFormatting.GRAY));
		return label;
	}
	private static boolean hasShiftDown() {
		var window = Minecraft.getInstance().getWindow();
		return GLFW.glfwGetKey(window.handle(), GLFW.GLFW_KEY_LEFT_SHIFT) == 1
			|| GLFW.glfwGetKey(window.handle(), GLFW.GLFW_KEY_RIGHT_SHIFT) == 1;
	}
	private @NotNull Component buildLabel() {
		return buildLabel(fluid, capacityMb, hasShiftDown());
	}
	@Override
	public int getHeight(@NotNull Font font) {
		return SlotUtil.SIZE_SLIM + 2;
	}
	@Override
	public int getWidth(@NotNull Font font) {
		return indentPixels(font) + barWidth(font);
	}
	@Override
	public void extractImage(@NotNull Font font, int x, int y, int width, int height, @NotNull GuiGraphicsExtractor gui) {
		var label = buildLabel();
		var barX = x + indentPixels(font);
		var barWidth = barWidth(font);
		renderFluidBar(gui, fluid, capacityMb, barX, y, barWidth, SlotUtil.SIZE_SLIM);
		var textX = barX + H_PADDING;
		var textY = y + Mth.floor((SlotUtil.SIZE_SLIM - font.lineHeight) / 2F) + 1;
		gui.text(font, label, textX, textY, 0xFFFFFFFF, true);
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
