package io.github.forgestove.create_cyber_goggles.core.util;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.FastColor.ABGR32;
import net.minecraft.util.Mth;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.awt.Color;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
public class NativeImageUtil {
	private static final int BASE = 0xC6C6C6;
	private static final float MIN_LUMA = 100F;
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
		int sampled = sampleFromItemIdColor(stack);
		if (sampled == BASE) sampled = sampleFromBakedModelIcon(stack);
		return clampLuma(sampled);
	}
	private static int sampleFromItemIdColor(ItemStack stack) {
		ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
		String path = "_" + id.getPath().toLowerCase(Locale.ROOT).replace('-', '_') + "_";
		for (Entry<String, Integer> entry : ID_COLOR_MAP.entrySet())
			if (path.contains("_" + entry.getKey() + "_")) return entry.getValue();
		return BASE;
	}
	@SuppressWarnings("resource")
	private static int sampleFromBakedModelIcon(ItemStack stack) {
		BakedModel model = mc.getItemRenderer().getModel(stack, mc.level, mc.player, 0);
		TextureAtlasSprite sprite = model.getParticleIcon(ModelData.EMPTY);
		// sprite.contents() 返回的是图集共享的 SpriteContents 对象,绝不能 close()——否则会把该精灵底层的
		// NativeImage 释放掉,等下一个 tick TextureAtlas 上传动画帧时崩在 "Image is not allocated"。
		// 这里只读 name,不需要持有/关闭它。
		ResourceLocation spriteName = sprite.contents().name();
		return sampleTexture(getRes(spriteName.getNamespace(), "textures/" + spriteName.getPath() + ".png"));
	}
	private static int clampLuma(int rgb) {
		int r = rgb >> 16 & 0xFF;
		int g = rgb >> 8 & 0xFF;
		int b = rgb & 0xFF;
		float luma = 0.299F * r + 0.587F * g + 0.114F * b;
		if (luma <= 0F) return BASE;
		if (luma >= MIN_LUMA && luma <= MAX_LUMA) return rgb;
		float target = Mth.clamp(luma < MIN_LUMA ? MIN_LUMA : MAX_LUMA, 0F, 255F);
		float scale = target / luma;
		int outR = Mth.clamp((int) (r * scale), 0, 255);
		int outG = Mth.clamp((int) (g * scale), 0, 255);
		int outB = Mth.clamp((int) (b * scale), 0, 255);
		return outR << 16 | outG << 8 | outB;
	}
	private static int sampleTexture(ResourceLocation texture) {
		Optional<Resource> optionalResource = mc.getResourceManager().getResource(texture);
		if (optionalResource.isEmpty()) return BASE;
		Resource resource = optionalResource.get();
		try (InputStream stream = resource.open(); NativeImage image = NativeImage.read(stream)) {
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
		int stepX = Math.max(1, image.getWidth() / 24);
		int stepY = Math.max(1, image.getHeight() / 24);
		for (var y = 0; y < image.getHeight(); y += stepY)
			for (var x = 0; x < image.getWidth(); x += stepX) {
				int pixel = image.getPixelRGBA(x, y);
				if (ABGR32.alpha(pixel) < 16) continue;
				int r = ABGR32.red(pixel);
				int g = ABGR32.green(pixel);
				int b = ABGR32.blue(pixel);
				// 跳过过暗像素（阴影/轮廓），避免拉低平均色导致整体偏深
				if (0.299F * r + 0.587F * g + 0.114F * b < 50F) continue;
				totalR += r;
				totalG += g;
				totalB += b;
				samples++;
			}
		if (samples == 0) return BASE;
		var r = (int) (totalR / samples);
		var g = (int) (totalG / samples);
		var b = (int) (totalB / samples);
		return r << 16 | g << 8 | b;
	}
}
