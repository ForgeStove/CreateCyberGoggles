package io.github.forgestove.create_cyber_goggles.core.gui;
import io.github.forgestove.create_cyber_goggles.core.api.TooltipOverlayRenderer;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.*;

import java.math.*;
import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
public abstract class AbstractItemGridRenderer implements TooltipOverlayRenderer {
	public static final int PAD = 4;
	private static final int BG = 0xFFC6C6C6;
	private static final int LIGHT = 0xFFFFFFFF;
	private static final int DARK = 0xFF555555;
	private static final int DARKER = 0xFF373737;
	private static final Identifier CFL_COMPRESSED_TANK_ID = Identifier.fromNamespaceAndPath("fluidlogistics", "compressed_storage_tank");
	public static int resolveColumns(OverlayData data) {
		return Mth.clamp(data.columns(), 1, Math.max(1, data.items().size()));
	}
	public static void renderItemGrid(
		GuiGraphicsExtractor gui,
		@NotNull List<ItemStack> items,
		int columns,
		int x,
		int y,
		float r,
		float g,
		float b,
		Set<Integer> zeroCountSlots
	) {
		var rows = Math.max(1, Mth.ceil((float) items.size() / columns));
		var panelWidth = columns * SlotUtil.SIZE + PAD * 2;
		var panelHeight = rows * SlotUtil.SIZE + PAD * 2;
		var pose = gui.pose();
		pose.pushMatrix();
		pose.translate(x, y);
		renderPanel(gui, panelWidth, panelHeight, r, g, b);
		for (var i = 0; i < items.size(); i++) {
			var col = i % columns;
			var row = i / columns;
			var slotX = PAD + col * SlotUtil.SIZE;
			var slotY = PAD + row * SlotUtil.SIZE;
			renderTintedSlot(gui, slotX, slotY, r, g, b);
			var item = items.get(i);
			gui.item(item, slotX + 1, slotY + 1);
			if (zeroCountSlots.contains(i)) gui.itemDecorations(mc.font, item, slotX + 1, slotY + 1, "0");
			else if (isCFLCompressedTank(item) && item.getCount() > 1)
				gui.itemDecorations(mc.font, item, slotX + 1, slotY + 1, formatFluidAmount(item.getCount()));
			else gui.itemDecorations(mc.font, item, slotX + 1, slotY + 1);
		}
		pose.popMatrix();
	}
	public static boolean isCFLCompressedTank(ItemStack stack) {
		if (stack.isEmpty()) return false;
		return BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(CFL_COMPRESSED_TANK_ID);
	}
	public static int getCFLTankAmount(ItemStack stack) {
		if (!isCFLCompressedTank(stack)) return 0;
		var container = stack.get(DataComponents.CONTAINER);
		if (container != null) {
			var nonEmpty = container.nonEmptyItems().iterator();
			if (nonEmpty.hasNext()) return nonEmpty.next().count();
		}
		return 0;
	}
	public static @NotNull String formatFluidAmount(int amountMb) {
		if (amountMb % 1000 == 0) return amountMb / 1000 + "B";
		return BigDecimal.valueOf(amountMb).divide(BigDecimal.valueOf(1000), 1, RoundingMode.DOWN).stripTrailingZeros().toPlainString()
			+ "B";
	}
	public static void renderPanel(GuiGraphicsExtractor gui, int width, int height, float r, float g, float b) {
		gui.fill(0, 0, width, height, tintColor(BG, r, g, b));
		gui.fill(0, 0, width, 2, tintColor(LIGHT, r, g, b));
		gui.fill(0, 0, 2, height, tintColor(LIGHT, r, g, b));
		gui.fill(0, height - 2, width, height, tintColor(DARK, r, g, b));
		gui.fill(width - 2, 0, width, height, tintColor(DARK, r, g, b));
		gui.fill(1, height - 1, width - 1, height, tintColor(DARKER, r, g, b));
		gui.fill(width - 1, 1, width, height - 1, tintColor(DARKER, r, g, b));
	}
	private static int tintColor(int argb, float r, float g, float b) {
		var a = argb >>> 24;
		var red = (int) ((argb >> 16 & 0xFF) * r);
		var green = (int) ((argb >> 8 & 0xFF) * g);
		var blue = (int) ((argb & 0xFF) * b);
		return a << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
	}
	public static void renderTintedSlot(GuiGraphicsExtractor gui, int x, int y, float r, float g, float b) {
		var slotColor = ((int) (r * 255) & 0xFF) << 16 | ((int) (g * 255) & 0xFF) << 8 | (int) (b * 255) & 0xFF | 0xFF000000;
		gui.blitSprite(RenderPipelines.GUI_TEXTURED, SlotUtil.SLOT, x, y, SlotUtil.SIZE, SlotUtil.SIZE, slotColor);
	}
	public abstract boolean supports(ItemStack stack);
	public abstract @Nullable OverlayData buildItemGrid(ItemStack stack);
	@Override
	public int width(ItemStack stack) {
		var data = getData(stack);
		if (data == null) return 0;
		return resolveColumns(data) * SlotUtil.SIZE + PAD * 2;
	}
	@Override
	public int height(ItemStack stack) {
		var data = getData(stack);
		if (data == null) return 0;
		var columns = resolveColumns(data);
		var rows = Math.max(1, Mth.ceil((float) data.items().size() / columns));
		return rows * SlotUtil.SIZE + PAD * 2;
	}
	private @Nullable OverlayData getData(ItemStack stack) {
		var data = buildItemGrid(stack);
		if (data == null || data.items().isEmpty()) return null;
		return data;
	}
	@Override
	public void render(GuiGraphicsExtractor gui, ItemStack stack, int x, int y) {
		var data = getData(stack);
		if (data == null) return;
		var color = NativeImageUtil.getColor(stack);
		var r = color.getRed() / 255F;
		var g = color.getGreen() / 255F;
		var b = color.getBlue() / 255F;
		var cols = resolveColumns(data);
		renderItemGrid(gui, data.items, cols, x, y, r, g, b, data.zeroCountSlots);
	}
	public record OverlayData(List<ItemStack> items, int columns, Set<Integer> zeroCountSlots) {
		public OverlayData(List<ItemStack> items, int columns) {
			this(items, columns, Set.of());
		}
	}
}
