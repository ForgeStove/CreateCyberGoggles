package io.github.forgestove.create_cyber_goggles.core.util;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.equipment.clipboard.*;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.*;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
public final class ClipboardUtil {
	private static final float SCALE = 0.5F;
	public static int getWidth() {
		return Mth.ceil(AllGuiTextures.CLIPBOARD.getWidth() * SCALE);
	}
	public static int getHeight() {
		return Mth.ceil(AllGuiTextures.CLIPBOARD.getHeight() * SCALE);
	}
	public static void renderTooltipOverlay(GuiGraphics graphics, ItemStack stack, int x, int y) {
		var pose = graphics.pose();
		pose.pushPose();
		pose.translate(x - 16, y, 600F);
		pose.scale(SCALE, SCALE, 1F);
		AllGuiTextures.CLIPBOARD.render(graphics, 0, 0);
		renderTooltipOverlayText(graphics, stack);
		pose.popPose();
	}
	private static void renderTooltipOverlayText(GuiGraphics graphics, ItemStack stack) {
		// 读取剪贴板内容
		var content = stack.getOrDefault(AllDataComponents.CLIPBOARD_CONTENT, ClipboardContent.EMPTY);
		var pages = ClipboardEntry.readAll(content);
		if (pages.isEmpty()) return;
		var currentPage = Mth.clamp(content.previouslyOpenedPage(), 0, pages.size() - 1);
		var entries = pages.get(currentPage);
		if (entries.isEmpty()) return;
		var font = mc.font;
		// 渲染剪贴板内容
		int x = 45, y = 50;
		for (var entry : entries) {
			var raw = entry.text.getString();
			var address = raw.startsWith("#") && !raw.substring(1).isBlank();
			var text = address ? raw.substring(1).stripLeading() : raw;
			if (text.isBlank()) continue;
			var color = entry.checked ? address ? 0x668D7F6B : 0x31B25D : 0x311A00;
			if (address) {
				var texture = entry.checked ? AllGuiTextures.CLIPBOARD_ADDRESS_INACTIVE : AllGuiTextures.CLIPBOARD_ADDRESS;
				texture.render(graphics, x - 1, y + 1);
			} else {
				graphics.drawString(font, "□", x, y + 1, entry.checked ? 0x668D7F6B : 0xFF8D7F6B, false);
				if (entry.checked) graphics.drawString(font, "✔", x, y, 0x31B25D, false);
			}
			for (var sequence : font.split(Component.literal(text), 150)) {
				graphics.drawString(font, sequence, x + 13, y, color, false);
				y += 9;
			}
			y += 3;
		}
		// 渲染页数指示器
		var pageIndicator = Component.translatable("book.pageIndicator", currentPage + 1, pages.size());
		var indicator = pageIndicator.getString();
		var slashCenterX = AllGuiTextures.CLIPBOARD.getWidth() / 2F;
		var slashIndex = indicator.indexOf('/');
		int indicatorX;
		if (slashIndex >= 0) {
			var leftPart = indicator.substring(0, slashIndex);
			indicatorX = (int) (slashCenterX - font.width(leftPart) - font.width("/") / 2F);
		} else indicatorX = (int) (slashCenterX - font.width(indicator) / 2F);
		graphics.drawString(font, pageIndicator, indicatorX, 235, 0x311A00, false);
	}
	public static void renderClipboardPage(PoseStack poseStack, MultiBufferSource buffer, int light, ItemStack stack) {
		poseStack.mulPose(Axis.YP.rotationDegrees(180F));
		poseStack.mulPose(Axis.ZP.rotationDegrees(180F));
		var matrix = poseStack.last().pose();
		matrix.translate(-0.25F, -0.25F, 0F);
		matrix.scale(0.004F);
		renderBack(poseStack, buffer, light);
		renderText(poseStack, buffer, light, stack);
	}
	private static void renderBack(PoseStack poseStack, MultiBufferSource buffer, int light) {
		// 渲染剪贴板背景界面
		var vertex = buffer.getBuffer(RenderType.text(AllGuiTextures.CLIPBOARD.getLocation()));
		var matrix = poseStack.last().pose();
		vertex.addVertex(matrix, 0F, 128F, 1F).setColor(-1).setUv(0F, 1F).setLight(light);
		vertex.addVertex(matrix, 128F, 128F, 1F).setColor(-1).setUv(1F, 1F).setLight(light);
		vertex.addVertex(matrix, 128F, 0F, 1F).setColor(-1).setUv(1F, 0F).setLight(light);
		vertex.addVertex(matrix, 0F, 0F, 1F).setColor(-1).setUv(0F, 0F).setLight(light);
	}
	private static void renderText(PoseStack poseStack, MultiBufferSource buffer, int light, ItemStack stack) {
		poseStack.scale(SCALE, SCALE, 1F);
		var font = mc.font;
		var matrix = poseStack.last().pose();
		var mode = DisplayMode.POLYGON_OFFSET;
		// 读取剪贴板内容
		var content = stack.getOrDefault(AllDataComponents.CLIPBOARD_CONTENT, ClipboardContent.EMPTY);
		var pages = ClipboardEntry.readAll(content);
		if (pages.isEmpty()) return;
		// 预设颜色
		var normalC = 0x311A00;
		var greenC = 0x31B25D;
		var lightC = 0x668D7F6B;
		var currentPage = Mth.clamp(content.previouslyOpenedPage(), 0, pages.size() - 1);
		var entries = pages.get(currentPage);
		if (entries.isEmpty()) return;
		// 渲染剪贴板内容
		int x = 45, y = 50;
		for (var entry : entries) {
			var text = entry.text.getString();
			var address = text.startsWith("#") && !text.substring(1).isBlank();
			if (address) text = text.substring(1).stripLeading();
			if (text.isBlank()) continue;
			// 渲染包裹图标
			var checked = entry.checked;
			var checkColor = checked ? lightC : normalC;
			if (address) renderAddressIcon(poseStack, buffer, light, checked, x, y + 1);
			else {
				// 渲染勾选框
				font.drawInBatch("□", x, y, checkColor, false, matrix, buffer, mode, 0, light);
				if (checked) font.drawInBatch("✔", x, y - 1, greenC, false, matrix, buffer, mode, 0, light);
			}
			// 渲染每行文本
			for (var sequence : font.split(Component.literal(text), 150)) {
				var textColor = address ? checkColor : checked ? greenC : normalC;
				font.drawInBatch(sequence, x + 13, y, textColor, false, matrix, buffer, mode, 0, light);
				y += 9;
			}
			y += 3;
		}
		// 渲染页数指示器
		var pageIndicator = Component.translatable("book.pageIndicator", currentPage + 1, pages.size());
		var indicator = pageIndicator.getString();
		var slashCenterX = AllGuiTextures.CLIPBOARD.getWidth() / 2F;
		var slashIndex = indicator.indexOf('/');
		float indicatorX;
		if (slashIndex >= 0) {
			var leftPart = indicator.substring(0, slashIndex);
			indicatorX = slashCenterX - font.width(leftPart) - font.width("/") / 2F;
		} else indicatorX = slashCenterX - font.width(indicator) / 2F;
		font.drawInBatch(indicator, indicatorX, 235, normalC, false, matrix, buffer, mode, 0, light);
	}
	private static void renderAddressIcon(PoseStack poseStack, MultiBufferSource buffer, int light, boolean checked, int x, int y) {
		var texture = checked ? AllGuiTextures.CLIPBOARD_ADDRESS_INACTIVE : AllGuiTextures.CLIPBOARD_ADDRESS;
		var vertex = buffer.getBuffer(RenderType.text(texture.getLocation()));
		var matrix = poseStack.last().pose();
		var startX = texture.getStartX();
		var startY = texture.getStartY();
		var width = texture.getWidth();
		var height = texture.getHeight();
		var v = 256F;
		var minU = startX / v;
		var minV = startY / v;
		var maxU = (startX + width) / v;
		var maxV = (startY + height) / v;
		x -= 1;
		y -= 1;
		var x1 = x + width;
		var y1 = y + height;
		vertex.addVertex(matrix, x, y1, 0F).setColor(-1).setUv(minU, maxV).setLight(light);
		vertex.addVertex(matrix, x1, y1, 0F).setColor(-1).setUv(maxU, maxV).setLight(light);
		vertex.addVertex(matrix, x1, y, 0F).setColor(-1).setUv(maxU, minV).setLight(light);
		vertex.addVertex(matrix, x, y, 0F).setColor(-1).setUv(minU, minV).setLight(light);
	}
}
