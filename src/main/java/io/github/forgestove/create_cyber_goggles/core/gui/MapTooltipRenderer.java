package io.github.forgestove.create_cyber_goggles.core.gui;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.material.MapColor;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
public final class MapTooltipRenderer implements TooltipOverlayRenderer {
	private static final int PADDING = 4;
	private static final int MAP_SIZE = 128;
	private static final int PREVIEW_SIZE = 64;
	private static final int PANEL_SIZE = PREVIEW_SIZE + PADDING * 2;
	private static final int SAMPLE_STEP = MAP_SIZE / PREVIEW_SIZE;
	private static final int CHECK_INTERVAL_TICKS = 12;
	private static final ResourceLocation MAP_BACKGROUND = getMCRes("textures/map/map_background_checkerboard.png");
	private static final ResourceLocation MAP_PREVIEW_TEXTURE = getCCGRes("dynamic/map_tooltip_preview");
	private static final int[] PACKED_TO_ABGR = new int[256];
	static {
		for (var i = 1; i < PACKED_TO_ABGR.length; i++) PACKED_TO_ABGR[i] = MapColor.getColorFromPackedId(i);
	}
	private int lastMapId = Integer.MIN_VALUE;
	private long lastUploadTick = Long.MIN_VALUE;
	private DynamicTexture previewTexture;
	private static int blendOpaqueAbgr(int c0, int c1, int c2, int c3) {
		int a = 0, r = 0, g = 0, b = 0, count = 0;
		count = accumulate(c0, count);
		a += c0 >>> 24 & 0xFF;
		r += c0 & 0xFF;
		g += c0 >>> 8 & 0xFF;
		b += c0 >>> 16 & 0xFF;
		count = accumulate(c1, count);
		a += c1 >>> 24 & 0xFF;
		r += c1 & 0xFF;
		g += c1 >>> 8 & 0xFF;
		b += c1 >>> 16 & 0xFF;
		count = accumulate(c2, count);
		a += c2 >>> 24 & 0xFF;
		r += c2 & 0xFF;
		g += c2 >>> 8 & 0xFF;
		b += c2 >>> 16 & 0xFF;
		count = accumulate(c3, count);
		a += c3 >>> 24 & 0xFF;
		r += c3 & 0xFF;
		g += c3 >>> 8 & 0xFF;
		b += c3 >>> 16 & 0xFF;
		if (count == 0) return 0;
		return a / count << 24 | b / count << 16 | g / count << 8 | r / count;
	}
	private static int accumulate(int color, int count) {
		return color >>> 24 == 0 ? count : count + 1;
	}
	@Override
	public boolean supports(ItemStack stack) {
		return CCG.config.tooltip.map && stack.is(Items.FILLED_MAP);
	}
	@Override
	public boolean canRender(ItemStack stack) {
		if (mc.level == null) return false;
		return MapItem.getSavedData(stack, mc.level) != null;
	}
	@Override
	public int width(ItemStack stack) {
		return PANEL_SIZE;
	}
	@Override
	public int height(ItemStack stack) {
		return PANEL_SIZE;
	}
	@Override
	public void render(GuiGraphics gui, ItemStack stack, int x, int y) {
		if (mc.level == null) return;
		var mapData = MapItem.getSavedData(stack, mc.level);
		if (mapData == null) return;
		ensureTexture();
		var mapId = stack.get(DataComponents.MAP_ID);
		var mapIdValue = mapId == null ? Integer.MIN_VALUE : mapId.id();
		var switchedMap = mapIdValue != lastMapId;
		if (switchedMap) lastMapId = mapIdValue;
		var tick = mc.level.getGameTime();
		if (switchedMap || tick - lastUploadTick >= CHECK_INTERVAL_TICKS) {
			lastUploadTick = tick;
			uploadPreview(mapData.colors);
		}
		var pose = gui.pose();
		pose.pushPose();
		pose.translate(x, y, 600F);
		gui.blit(MAP_BACKGROUND, 0, 0, 0, 0, PANEL_SIZE, PANEL_SIZE, PANEL_SIZE, PANEL_SIZE);
		gui.blit(MAP_PREVIEW_TEXTURE, PADDING, PADDING, 0, 0, PREVIEW_SIZE, PREVIEW_SIZE, PREVIEW_SIZE, PREVIEW_SIZE);
		pose.popPose();
	}
	private void ensureTexture() {
		if (previewTexture != null) return;
		previewTexture = new DynamicTexture(PREVIEW_SIZE, PREVIEW_SIZE, true);
		mc.getTextureManager().register(MAP_PREVIEW_TEXTURE, previewTexture);
	}
	private void uploadPreview(byte[] colors) {
		if (previewTexture == null) return;
		var image = previewTexture.getPixels();
		if (image == null) return;
		for (var py = 0; py < PREVIEW_SIZE; py++) {
			var mapY = py * SAMPLE_STEP;
			var mapY1 = mapY + 1;
			var row0 = mapY * MAP_SIZE;
			var row1 = mapY1 * MAP_SIZE;
			for (var px = 0; px < PREVIEW_SIZE; px++) {
				var mapX = px * SAMPLE_STEP;
				var mapX1 = mapX + 1;
				var c0 = PACKED_TO_ABGR[Byte.toUnsignedInt(colors[mapX + row0])];
				var c1 = PACKED_TO_ABGR[Byte.toUnsignedInt(colors[mapX1 + row0])];
				var c2 = PACKED_TO_ABGR[Byte.toUnsignedInt(colors[mapX + row1])];
				var c3 = PACKED_TO_ABGR[Byte.toUnsignedInt(colors[mapX1 + row1])];
				image.setPixelRGBA(px, py, blendOpaqueAbgr(c0, c1, c2, c3));
			}
		}
		previewTexture.upload();
	}
}
