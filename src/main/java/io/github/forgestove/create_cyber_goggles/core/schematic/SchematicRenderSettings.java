package io.github.forgestove.create_cyber_goggles.core.schematic;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.createmod.catnip.theme.Color;
/**
 * 一张蓝图渲染图的设置：输出宽度、视角、抗锯齿与背景色。
 * <p>
 * 移植自 Create: Blueprinted（MIT），默认值改读本模组配置。
 */
public class SchematicRenderSettings {
	public static final Color DEFAULT_BG_COLOR = Color.TRANSPARENT_BLACK;
	public static final int MAX_ANTIALIASING = 4;
	public static final int MIN_WIDTH = 64;
	public static final int MAX_WIDTH = 8192;
	private final int imageWidth;
	private final Orientation orientation;
	private final int antialiasingFactor;
	private final Color backgroundColor;
	private SchematicRenderSettings(int imageWidth, Orientation orientation, int antialiasingFactor, Color backgroundColor) {
		this.imageWidth = imageWidth;
		this.orientation = orientation;
		this.antialiasingFactor = antialiasingFactor;
		this.backgroundColor = backgroundColor;
		if (imageWidth < MIN_WIDTH || imageWidth > MAX_WIDTH)
			throw new IllegalArgumentException("Width must be between " + MIN_WIDTH + " and " + MAX_WIDTH);
		if (antialiasingFactor < 1 || antialiasingFactor > MAX_ANTIALIASING)
			throw new IllegalArgumentException("Antialiasing must be between 1 and " + MAX_ANTIALIASING);
	}
	public static Builder builder() {
		return new Builder();
	}
	/** 实际渲染像素宽 = 期望宽 × 抗锯齿倍率（超采样后再降采样） */
	public int imageWidth() {
		return imageWidth * antialiasingFactor;
	}
	public Orientation orientation() {
		return orientation;
	}
	public int antialiasingFactor() {
		return antialiasingFactor;
	}
	public Color backgroundColor() {
		return backgroundColor;
	}
	public record Orientation(float yaw, float pitch, float roll) {
		public static final Orientation ISOMETRIC_RIGHT = new Orientation(-45F, 35.264F, 0F);
		public static final Orientation ISOMETRIC_LEFT = new Orientation(45F, 35.264F, 0F);
		public Orientation(float yaw, float pitch) {
			this(yaw, pitch, 0);
		}
	}
	public static class Builder {
		private final Color backgroundColor;
		private int imageWidth;
		private Orientation orientation;
		private int antialiasingFactor;
		public Builder() {
			imageWidth = CCG.config.schematic.image.defaultWidth;
			orientation = Orientation.ISOMETRIC_RIGHT;
			antialiasingFactor = CCG.config.schematic.image.defaultAntialiasing;
			backgroundColor = DEFAULT_BG_COLOR;
		}
		public Builder imageWidth(int width) {
			imageWidth = width;
			return this;
		}
		public Builder orientation(Orientation orientation) {
			this.orientation = orientation;
			return this;
		}
		public Builder antialiasingFactor(int factor) {
			antialiasingFactor = factor;
			return this;
		}
		public SchematicRenderSettings build() {
			return new SchematicRenderSettings(imageWidth, orientation, antialiasingFactor, backgroundColor);
		}
	}
}
