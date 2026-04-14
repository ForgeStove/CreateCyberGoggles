package io.github.forgestove.create_cyber_goggles.core.gui;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.client.gui.GuiGraphics;
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
	private static final ResourceLocation MAP_BACKGROUND = getMCRes("textures/map/map_background_checkerboard.png");
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
		var pose = gui.pose();
		pose.pushPose();
		pose.translate(x, y, 600F);
		var left = PADDING;
		var top = PADDING;
		gui.blit(MAP_BACKGROUND, 0, 0, 0, 0, PANEL_SIZE, PANEL_SIZE, PANEL_SIZE, PANEL_SIZE);
		var colors = mapData.colors;
		for (var py = 0; py < PREVIEW_SIZE; py++) {
			var mapY = py * SAMPLE_STEP;
			for (var px = 0; px < PREVIEW_SIZE; px++) {
				var mapX = px * SAMPLE_STEP;
				var packedId = Byte.toUnsignedInt(colors[mapX + mapY * MAP_SIZE]);
				var color = MapColor.getColorFromPackedId(packedId);
				if (color >>> 24 == 0) color |= 0xFF000000;
				// ABGR -> ARGB（交换 R/B 通道）
				var a = color >>> 24 & 0xFF;
				var r = color >>> 16 & 0xFF;
				var g = color >>> 8 & 0xFF;
				var b = color & 0xFF;
				var argb = a << 24 | b << 16 | g << 8 | r;
				gui.fill(left + px, top + py, left + px + 1, top + py + 1, argb);
			}
		}
		pose.popPose();
	}
}
