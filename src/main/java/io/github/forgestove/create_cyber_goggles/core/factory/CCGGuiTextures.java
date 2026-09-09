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
	AUTO_REPLENISH_ADDRESS("auto_replenish", 0, 144, 256, 18),
	AUTO_REPLENISH_BLUEPRINT("auto_replenish", 16, 176, 24, 24),
	AUTO_REPLENISH_BOX_UP("auto_replenish", 48, 176, 162, 21),
	AUTO_REPLENISH_BOX_MIDDLE("auto_replenish", 48, 197, 162, 17),
	AUTO_REPLENISH_BOX_DOWN("auto_replenish", 48, 214, 162, 11),
	;
	public final ResourceLocation location;
	private final int width;
	private final int height;
	private final int startX;
	private final int startY;
	@SuppressWarnings("unused")
	CCGGuiTextures(String location, int width, int height) {
		this(location, 0, 0, width, height);
	}
	CCGGuiTextures(String location, int startX, int startY, int width, int height) {
		this(CCG.ID, location, startX, startY, width, height);
	}
	CCGGuiTextures(String namespace, String location, int startX, int startY, int width, int height) {
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
	public void render(GuiGraphics gui, int x, int y) {
		gui.blit(location, x, y, startX, startY, width, height);
	}
	@OnlyIn(Dist.CLIENT)
	public void render(GuiGraphics gui, int x, int y, Color c) {
		bind();
		UIRenderHelper.drawColoredTexture(gui, c, x, y, startX, startY, width, height);
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
