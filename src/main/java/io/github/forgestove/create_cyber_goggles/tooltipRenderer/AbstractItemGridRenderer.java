package io.github.forgestove.create_cyber_goggles.tooltipRenderer;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.forgestove.create_cyber_goggles.api.TooltipRenderer;
import io.github.forgestove.create_cyber_goggles.compat.fluidlogistics.PackageTankHelper;
import io.github.forgestove.create_cyber_goggles.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.*;

import java.awt.Color;
import java.io.*;
import java.math.*;
import java.util.*;

import static io.github.forgestove.create_cyber_goggles.util.CCGUtil.*;
public abstract class AbstractItemGridRenderer implements TooltipRenderer {
	public static final int PAD = 4;
	private static final ResourceLocation CONTAINER_BACKGROUND = getMCRes("textures/gui/container/generic_54.png");
	private static final int PANEL_TEX = 256;
	private static final int PANEL_SRC_W = 176;
	private static final int PANEL_SRC_H = 222;
	private static PanelRect cachedRect;
	private static String cachedPackId;
	public static void renderItemGrid(
		GuiGraphics gui,
		@NotNull List<ItemStack> items,
		int columns,
		int x,
		int y,
		float r,
		float g,
		float b,
		Set<Integer> zeroCountSlots
	) {
		int rows = Math.max(1, Mth.ceil((float) items.size() / columns));
		int panelWidth = columns * SlotUtil.SIZE + PAD * 2;
		int panelHeight = rows * SlotUtil.SIZE + PAD * 2;
		PoseStack pose = gui.pose();
		pose.pushPose();
		pose.translate(x, y, 600F);
		// 材质包自定义了该 UI 时保留其原样，不染色
		if (shouldTintPanel()) {
			RenderSystem.setShaderColor(r, g, b, 1);
			renderPanel(gui, panelWidth, panelHeight);
			RenderSystem.setShaderColor(1, 1, 1, 1);
		} else renderPanel(gui, panelWidth, panelHeight);
		for (var row = 0; row < rows; row++)
			for (var col = 0; col < columns; col++) {
				int slotX = PAD + col * SlotUtil.SIZE;
				int slotY = PAD + row * SlotUtil.SIZE;
				renderTintedSlot(gui, slotX, slotY, r, g, b);
				int i = row * columns + col;
				if (i >= items.size()) continue;
				ItemStack item = items.get(i);
				gui.renderItem(item, slotX + 1, slotY + 1);
				if (zeroCountSlots.contains(i)) gui.renderItemDecorations(mc.font, item, slotX + 1, slotY + 1, "0");
				else if (PackageTankHelper.isCFLCompressedTank(item) && item.getCount() > 1)
					gui.renderItemDecorations(mc.font, item, slotX + 1, slotY + 1, formatFluidAmount(item.getCount()));
				else gui.renderItemDecorations(mc.font, item, slotX + 1, slotY + 1);
			}
		pose.popPose();
	}
	public static void renderPanel(GuiGraphics gui, int width, int height) {
		PanelRect rect = getPanelRect();
		int e = PAD;
		int mw = Math.max(0, width - e * 2);
		int mh = Math.max(0, height - e * 2);
		int x0 = rect.x0();
		int y0 = rect.y0();
		int x1 = rect.x1();
		int y1 = rect.y1();
		int sw = Math.max(0, x1 - x0 - e * 2);
		// 四角（保留圆角）
		blitPanelPiece(gui, rect, 0, 0, e, e, x0, y0, e, e);
		blitPanelPiece(gui, rect, width - e, 0, e, e, x1 - e, y0, e, e);
		blitPanelPiece(gui, rect, 0, height - e, e, e, x0, y1 - e, e, e);
		blitPanelPiece(gui, rect, width - e, height - e, e, e, x1 - e, y1 - e, e, e);
		// 四边（拉伸）；左右竖条取左下/右下角上方的边带段，避免材质包在此区域的非均匀图案被拉长变形
		blitPanelPiece(gui, rect, e, 0, mw, e, x0 + e, y0, sw, e);
		blitPanelPiece(gui, rect, e, height - e, mw, e, x0 + e, y1 - e, sw, e);
		blitPanelPiece(gui, rect, 0, e, e, mh, x0, y1 - e * 2, e, e);
		blitPanelPiece(gui, rect, width - e, e, e, mh, x1 - e, y1 - e * 2, e, e);
	}
	public static void renderTintedSlot(GuiGraphics gui, int x, int y, float r, float g, float b) {
		RenderSystem.setShaderColor(r, g, b, 1F);
		gui.blitSprite(SlotUtil.SLOT, x, y, 0, SlotUtil.SIZE, SlotUtil.SIZE);
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
	}
	public static @NotNull String formatFluidAmount(int amountMb) {
		if (amountMb % 1000 == 0) return amountMb / 1000 + "B";
		return BigDecimal.valueOf(amountMb).divide(BigDecimal.valueOf(1000), 1, RoundingMode.DOWN).stripTrailingZeros().toPlainString()
			+ "B";
	}
	/** 是否给面板染色：仅当 generic_54 仍为原版纹理（未被材质包覆盖）时为 true。 */
	private static boolean shouldTintPanel() {
		return "vanilla".equals(getPanelRect().packId());
	}
	/** 返回当前 generic_54 面板范围，按来源包 id 缓存；资源不可读时回退原版值。 */
	private static PanelRect getPanelRect() {
		String packId = mc.getResourceManager().getResource(CONTAINER_BACKGROUND).map(Resource::sourcePackId).orElse("");
		if (cachedRect == null || !packId.equals(cachedPackId)) {
			PanelRect rect = analyzePanelRect(packId);
			cachedRect = rect != null ? rect : new PanelRect(0, 0, PANEL_SRC_W, PANEL_SRC_H, PANEL_TEX, PANEL_TEX, packId);
			cachedPackId = packId;
		}
		return cachedRect;
	}
	/** 分析材质包纹理的面板范围（非透明 bbox）与纹理尺寸，适配自定义布局。 */
	private static @Nullable PanelRect analyzePanelRect(String packId) {
		Resource res = mc.getResourceManager().getResource(CONTAINER_BACKGROUND).orElse(null);
		if (res == null) return null;
		try (InputStream in = res.open()) {
			NativeImage img = NativeImage.read(in);
			try (img) {
				int texW = img.getWidth();
				int texH = img.getHeight();
				int minX = texW;
				int minY = texH;
				int maxX = -1;
				int maxY = -1;
				for (var y = 0; y < texH; y++)
					for (var x = 0; x < texW; x++)
						if ((img.getPixelRGBA(x, y) >>> 24 & 0xFF) > 0) {
							if (x < minX) minX = x;
							if (y < minY) minY = y;
							if (x > maxX) maxX = x;
							if (y > maxY) maxY = y;
						}
				return maxX < 0 ? null : new PanelRect(minX, minY, maxX + 1, maxY + 1, texW, texH, packId);
			}
		} catch (IOException e) {
			return null;
		}
	}
	private static void blitPanelPiece(
		GuiGraphics gui,
		PanelRect rect,
		int x,
		int y,
		int dstW,
		int dstH,
		int srcU,
		int srcV,
		int srcW,
		int srcH
	) {
		gui.blit(CONTAINER_BACKGROUND, x, y, dstW, dstH, srcU, srcV, srcW, srcH, rect.texW(), rect.texH());
	}
	public static int resolveColumns(OverlayData data) {
		return Mth.clamp(data.columns(), 1, Math.max(1, data.items().size()));
	}
	@Override
	public int width(ItemStack stack) {
		OverlayData data = getData(stack);
		if (data == null) return 0;
		return resolveColumns(data) * SlotUtil.SIZE + PAD * 2;
	}
	@Override
	public int height(ItemStack stack) {
		OverlayData data = getData(stack);
		if (data == null) return 0;
		int columns = resolveColumns(data);
		int rows = Math.max(1, Mth.ceil((float) data.items().size() / columns));
		return rows * SlotUtil.SIZE + PAD * 2;
	}
	@Override
	public void render(GuiGraphics gui, ItemStack stack, int x, int y) {
		OverlayData data = getData(stack);
		if (data == null) return;
		Color color = NativeImageUtil.getColor(stack);
		float r = color.getRed() / 255F;
		float g = color.getGreen() / 255F;
		float b = color.getBlue() / 255F;
		int cols = resolveColumns(data);
		renderItemGrid(gui, data.items, cols, x, y, r, g, b, data.zeroCountSlots);
	}
	private @Nullable OverlayData getData(ItemStack stack) {
		OverlayData data = buildItemGrid(stack);
		if (data == null || data.items().isEmpty()) return null;
		return data;
	}
	public abstract @Nullable OverlayData buildItemGrid(ItemStack stack);
	/** generic_54 纹理面板范围（含纹理尺寸，用于 blit uv 换算） */
	private record PanelRect(int x0, int y0, int x1, int y1, int texW, int texH, String packId) {}
	public record OverlayData(List<ItemStack> items, int columns, Set<Integer> zeroCountSlots) {
		public OverlayData(List<ItemStack> items, int columns) {
			this(items, columns, Set.of());
		}
	}
}
