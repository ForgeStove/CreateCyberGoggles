package io.github.forgestove.create_cyber_goggles.core.factory;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.createmod.catnip.gui.*;
import net.createmod.catnip.gui.element.ScreenElement;
import net.createmod.catnip.theme.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.*;
import org.jetbrains.annotations.NotNull;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.getRes;
public enum CCGGuiTextures implements ScreenElement, TextureSheetSegment {
	AUTO_REPLENISH_HEADER("auto_replenish", 0, 0, 256, 36),
	AUTO_REPLENISH_BODY("auto_replenish", 0, 48, 256, 20),
	AUTO_REPLENISH_FOOTER("auto_replenish", 0, 80, 256, 48),
	AUTO_REPLENISH_ADDRESS("auto_replenish", 16, 144, 127, 18),
	AUTO_REPLENISH_BLUEPRINT_00("auto_replenish", 160, 144, 8, 8),
	AUTO_REPLENISH_BLUEPRINT_10("auto_replenish", 168, 144, 8, 8),
	AUTO_REPLENISH_BLUEPRINT_20("auto_replenish", 176, 144, 8, 8),
	AUTO_REPLENISH_BLUEPRINT_01("auto_replenish", 160, 152, 8, 8),
	AUTO_REPLENISH_BLUEPRINT_11("auto_replenish", 168, 152, 8, 8),
	AUTO_REPLENISH_BLUEPRINT_21("auto_replenish", 176, 152, 8, 8),
	AUTO_REPLENISH_BLUEPRINT_02("auto_replenish", 160, 160, 8, 8),
	AUTO_REPLENISH_BLUEPRINT_12("auto_replenish", 168, 160, 8, 8),
	AUTO_REPLENISH_BLUEPRINT_22("auto_replenish", 176, 160, 8, 8),
	;
	public final ResourceLocation location;
	private final int width;
	private final int height;
	private final int startX;
	private final int startY;
	CCGGuiTextures(String location, int width, int height) {
		this(location, 0, 0, width, height);
	}
	CCGGuiTextures(String location, int startX, int startY, int width, int height) {
		this(CCG.ID, location, startX, startY, width, height);
	}
	CCGGuiTextures(String namespace, String location, int startX, int startY, int width, int height) {
		// 整套 UI 图集位于 textures/gui/sprites/<file> 下（如 sprites/auto_replenish/auto_replenish.png）
		this.location = getRes(namespace, "textures/gui/sprites/" + location + ".png");
		this.width = width;
		this.height = height;
		this.startX = startX;
		this.startY = startY;
	}
	@Override
	public @NotNull ResourceLocation getLocation() {
		return location;
	}
	@Override
	@OnlyIn(Dist.CLIENT)
	public void render(GuiGraphics graphics, int x, int y) {
		graphics.blit(location, x, y, startX, startY, width, height);
	}
	@OnlyIn(Dist.CLIENT)
	public void render(GuiGraphics graphics, int x, int y, Color c) {
		bind();
		UIRenderHelper.drawColoredTexture(graphics, c, x, y, startX, startY, width, height);
	}
	@Override
	public int getStartX() {
		return startX;
	}
	@Override
	public int getStartY() {
		return startY;
	}
	@Override
	public int getWidth() {
		return width;
	}
	@Override
	public int getHeight() {
		return height;
	}
}
