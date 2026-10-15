package io.github.forgestove.create_cyber_goggles.core.factory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
public enum CCGGuiTextures {
	HEADER("replenish/header"),
	BODY("replenish/body"),
	FOOTER("replenish/footer"),
	ADDRESS("replenish/address"),
	BLUEPRINT("replenish/blueprint"),
	BOX("replenish/box");
	private final ResourceLocation sprite;
	CCGGuiTextures(String name) {
		sprite = getCCGRes(name);
	}
	public void render(@NotNull GuiGraphics gui, int x, int y) {
		gui.blitSprite(sprite, x, y, width(), height());
	}
	public int width() {
		return getContents().width();
	}
	public int height() {
		return getContents().height();
	}
	private @NonNull SpriteContents getContents() {
		return mc.getGuiSprites().getSprite(sprite).contents();
	}
	public void render(@NotNull GuiGraphics gui, int x, int y, int width, int height) {
		if (width <= 0 || height <= 0) return;
		gui.blitSprite(sprite, x, y, width, height);
	}
}
