package io.github.forgestove.create_cyber_goggles.core.tooltipRenderer;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.forgestove.create_cyber_goggles.api.TooltipRenderer;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
import org.jetbrains.annotations.*;

import java.io.IOException;
import java.math.*;
import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
public abstract class AbstractItemGridRenderer implements TooltipRenderer {
	public static final int PAD = 4;
	private static final ResourceLocation CFL_COMPRESSED_TANK_ID = getRes("fluidlogistics", "compressed_storage_tank");
	private static final ResourceLocation CONTAINER_BACKGROUND = getMCRes("textures/gui/container/generic_54.png");
	private static final int PANEL_TEX = 256, PANEL_EDGE = 4, PANEL_SRC_W = 176, PANEL_SRC_H = 222;
	private static PanelRect cachedRect;
	private static String cachedPackId;
	public static int getCFLTankAmount(ItemStack stack) {
		if (!isCFLCompressedTank(stack)) return 0;
		var handler = stack.getCapability(FluidHandler.ITEM);
		if (handler == null) return 0;
		var fluid = handler.getFluidInTank(0);
		return fluid.isEmpty() ? 0 : fluid.getAmount();
	}
	public static boolean isCFLCompressedTank(ItemStack stack) {
		if (stack.isEmpty()) return false;
		return BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(CFL_COMPRESSED_TANK_ID);
	}
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
		var rows = Math.max(1, Mth.ceil((float) items.size() / columns));
		var panelWidth = columns * SlotUtil.SIZE + PAD * 2;
		var panelHeight = rows * SlotUtil.SIZE + PAD * 2;
		var pose = gui.pose();
		pose.pushPose();
		pose.translate(x, y, 600F);
		// 材质包自定义了该 UI 时保留其原样，不染色
		if (shouldTintPanel()) {
			RenderSystem.setShaderColor(r, g, b, 1);
			renderPanel(gui, panelWidth, panelHeight);
			RenderSystem.setShaderColor(1, 1, 1, 1);
		} else renderPanel(gui, panelWidth, panelHeight);
		for (var i = 0; i < items.size(); i++) {
			var col = i % columns;
			var row = i / columns;
			var slotX = PAD + col * SlotUtil.SIZE;
			var slotY = PAD + row * SlotUtil.SIZE;
			renderTintedSlot(gui, slotX, slotY, r, g, b);
			var item = items.get(i);
			gui.renderItem(item, slotX + 1, slotY + 1);
			if (zeroCountSlots.contains(i)) gui.renderItemDecorations(mc.font, item, slotX + 1, slotY + 1, "0");
			else if (isCFLCompressedTank(item) && item.getCount() > 1)
				gui.renderItemDecorations(mc.font, item, slotX + 1, slotY + 1, formatFluidAmount(item.getCount()));
			else gui.renderItemDecorations(mc.font, item, slotX + 1, slotY + 1);
		}
		pose.popPose();
	}
	public static void renderPanel(GuiGraphics gui, int width, int height) {
		var rect = getPanelRect();
		var e = PANEL_EDGE;
		var mw = Math.max(0, width - e * 2);
		var mh = Math.max(0, height - e * 2);
		var x0 = rect.x0();
		var y0 = rect.y0();
		var x1 = rect.x1();
		var y1 = rect.y1();
		var sw = Math.max(0, x1 - x0 - e * 2);
		var sh = Math.max(0, y1 - y0 - e * 2);
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
		// 中心
		blitPanelPiece(gui, rect, e, e, mw, mh, x0 + e, y0 + e, sw, sh);
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
		var packId = mc.getResourceManager().getResource(CONTAINER_BACKGROUND).map(Resource::sourcePackId).orElse("");
		if (cachedRect == null || !packId.equals(cachedPackId)) {
			var rect = analyzePanelRect(packId);
			cachedRect = rect != null ? rect : new PanelRect(0, 0, PANEL_SRC_W, PANEL_SRC_H, PANEL_TEX, PANEL_TEX, packId);
			cachedPackId = packId;
		}
		return cachedRect;
	}
	/** 分析材质包纹理的面板范围（非透明 bbox）与纹理尺寸，适配自定义布局。 */
	private static @Nullable PanelRect analyzePanelRect(String packId) {
		var res = mc.getResourceManager().getResource(CONTAINER_BACKGROUND).orElse(null);
		if (res == null) return null;
		try (var in = res.open()) {
			var img = NativeImage.read(in);
			try (img) {
				var texW = img.getWidth();
				var texH = img.getHeight();
				var minX = texW;
				var minY = texH;
				var maxX = -1;
				var maxY = -1;
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
	public abstract boolean supports(ItemStack stack);
	@Override
	public int width(ItemStack stack) {
		var data = getData(stack);
		if (data == null) return 0;
		return resolveColumns(data) * SlotUtil.SIZE + PAD * 2;
	}
	private @Nullable OverlayData getData(ItemStack stack) {
		var data = buildItemGrid(stack);
		if (data == null || data.items().isEmpty()) return null;
		return data;
	}
	public static int resolveColumns(OverlayData data) {
		return Mth.clamp(data.columns(), 1, Math.max(1, data.items().size()));
	}
	public abstract @Nullable OverlayData buildItemGrid(ItemStack stack);
	@Override
	public int height(ItemStack stack) {
		var data = getData(stack);
		if (data == null) return 0;
		var columns = resolveColumns(data);
		var rows = Math.max(1, Mth.ceil((float) data.items().size() / columns));
		return rows * SlotUtil.SIZE + PAD * 2;
	}
	@Override
	public void render(GuiGraphics gui, ItemStack stack, int x, int y) {
		var data = getData(stack);
		if (data == null) return;
		var color = NativeImageUtil.getColor(stack);
		var r = color.getRed() / 255F;
		var g = color.getGreen() / 255F;
		var b = color.getBlue() / 255F;
		var cols = resolveColumns(data);
		renderItemGrid(gui, data.items, cols, x, y, r, g, b, data.zeroCountSlots);
	}
	/** generic_54 纹理面板范围（含纹理尺寸，用于 blit uv 换算） */
	private record PanelRect(int x0, int y0, int x1, int y1, int texW, int texH, String packId) {}
	public record OverlayData(List<ItemStack> items, int columns, Set<Integer> zeroCountSlots) {
		public OverlayData(List<ItemStack> items, int columns) {
			this(items, columns, Set.of());
		}
	}
}
