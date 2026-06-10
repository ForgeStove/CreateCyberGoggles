package io.github.forgestove.create_cyber_goggles.core.util;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.*;

import java.awt.Color;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
public class NativeImageUtil {
	private static final int BASE = 0xC6C6C6;
	private static final float MIN_LUMA = 78F;
	private static final float MAX_LUMA = 208F;
	private static final Map<String, Integer> ID_COLOR_MAP = new LinkedHashMap<>();
	private static final Map<Item, Integer> ITEM_COLOR_CACHE = new ConcurrentHashMap<>();
	static {
		ID_COLOR_MAP.put("light_blue", 0x3AB3DA);
		ID_COLOR_MAP.put("light_gray", 0x9D9D97);
		ID_COLOR_MAP.put("magenta", 0xC74EBD);
		ID_COLOR_MAP.put("yellow", 0xFED83D);
		ID_COLOR_MAP.put("orange", 0xF9801D);
		ID_COLOR_MAP.put("purple", 0x8932B8);
		ID_COLOR_MAP.put("white", 0xF9FFFE);
		ID_COLOR_MAP.put("black", 0x1D1D21);
		ID_COLOR_MAP.put("brown", 0x835432);
		ID_COLOR_MAP.put("green", 0x5E7C16);
		ID_COLOR_MAP.put("gray", 0x474F52);
		ID_COLOR_MAP.put("cyan", 0x169C9C);
		ID_COLOR_MAP.put("blue", 0x3C44AA);
		ID_COLOR_MAP.put("lime", 0x80C71F);
		ID_COLOR_MAP.put("pink", 0xF38BAA);
		ID_COLOR_MAP.put("red", 0xB02E26);
	}
	public static Color getColor(ItemStack stack) {
		return new Color(ITEM_COLOR_CACHE.computeIfAbsent(stack.getItem(), item -> sampleItemTextureColor(stack)));
	}
	public static int sampleItemTextureColor(ItemStack stack) {
		var sampled = sampleFromItemIdColor(stack);
		if (sampled == BASE) sampled = sampleFromItemTexture(stack);
		return clampLuma(sampled);
	}
	private static int clampLuma(int rgb) {
		var r = rgb >> 16 & 0xFF;
		var g = rgb >> 8 & 0xFF;
		var b = rgb & 0xFF;
		var luma = 0.299F * r + 0.587F * g + 0.114F * b;
		if (luma <= 0F) return BASE;
		if (luma >= MIN_LUMA && luma <= MAX_LUMA) return rgb;
		var target = Mth.clamp(luma < MIN_LUMA ? MIN_LUMA : MAX_LUMA, 0F, 255F);
		var scale = target / luma;
		var outR = Mth.clamp((int) (r * scale), 0, 255);
		var outG = Mth.clamp((int) (g * scale), 0, 255);
		var outB = Mth.clamp((int) (b * scale), 0, 255);
		return outR << 16 | outG << 8 | outB;
	}
	private static int sampleTexture(Identifier texture) {
		var optionalResource = mc.getResourceManager().getResource(texture);
		if (optionalResource.isEmpty()) return BASE;
		var resource = optionalResource.get();
		try (var stream = resource.open(); var image = NativeImage.read(stream)) {
			return dominantColor(image);
		} catch (IOException ignored) {
			return BASE;
		}
	}
	private static int dominantColor(NativeImage image) {
		if (image.getWidth() <= 0 || image.getHeight() <= 0) return BASE;
		long totalR = 0;
		long totalG = 0;
		long totalB = 0;
		long samples = 0;
		var stepX = Math.max(1, image.getWidth() / 24);
		var stepY = Math.max(1, image.getHeight() / 24);
		for (var y = 0; y < image.getHeight(); y += stepY)
			for (var x = 0; x < image.getWidth(); x += stepX) {
				var pixel = image.getPixel(x, y);
				var alpha = (pixel >> 24) & 0xFF;
				if (alpha < 16) continue;
				// getPixel returns ARGB format
				totalR += (pixel >> 16) & 0xFF;
				totalG += (pixel >> 8) & 0xFF;
				totalB += pixel & 0xFF;
				samples++;
			}
		if (samples == 0) return BASE;
		var r = (int) (totalR / samples);
		var g = (int) (totalG / samples);
		var b = (int) (totalB / samples);
		return r << 16 | g << 8 | b;
	}
	private static int sampleFromItemTexture(ItemStack stack) {
		var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
		var texturePath = Identifier.fromNamespaceAndPath(id.getNamespace(), "textures/item/" + id.getPath() + ".png");
		return sampleTexture(texturePath);
	}
	private static int sampleFromItemIdColor(ItemStack stack) {
		var id = BuiltInRegistries.ITEM.getKey(stack.getItem());
		var path = "_" + id.getPath().toLowerCase(Locale.ROOT).replace('-', '_') + "_";
		for (var entry : ID_COLOR_MAP.entrySet())
			if (path.contains("_" + entry.getKey() + "_")) return entry.getValue();
		return BASE;
	}
}
