package io.github.forgestove.create_cyber_goggles.core.gui;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.api.TooltipOverlayRenderer;
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
	private static final float PREVIEW_SCALE = (float) PREVIEW_SIZE / MAP_SIZE;
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
		pose.pushPose();
		pose.translate(PADDING, PADDING, 0);
		pose.scale(PREVIEW_SCALE, PREVIEW_SCALE, 1F);
		gui.blit(MAP_PREVIEW_TEXTURE, 0, 0, 0, 0, MAP_SIZE, MAP_SIZE, MAP_SIZE, MAP_SIZE);
		pose.popPose();
		pose.popPose();
	}
	private void ensureTexture() {
		if (previewTexture != null) return;
		previewTexture = new DynamicTexture(MAP_SIZE, MAP_SIZE, true);
		previewTexture.setFilter(false, false);
		mc.getTextureManager().register(MAP_PREVIEW_TEXTURE, previewTexture);
	}
	private void uploadPreview(byte[] colors) {
		if (previewTexture == null) return;
		var image = previewTexture.getPixels();
		if (image == null) return;
		for (var py = 0; py < MAP_SIZE; py++) {
			var row = py * MAP_SIZE;
			for (var px = 0; px < MAP_SIZE; px++) {
				var packed = Byte.toUnsignedInt(colors[px + row]);
				image.setPixelRGBA(px, py, PACKED_TO_ABGR[packed]);
			}
		}
		previewTexture.upload();
	}
}
