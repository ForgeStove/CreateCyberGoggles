package io.github.forgestove.create_cyber_goggles.core.util;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.equipment.clipboard.*;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.renderer.*;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
public class ClipboardUtil {
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
		vertex.addVertex(matrix, -7F, 135F, 1F).setColor(-1).setUv(0F, 1F).setLight(light);
		vertex.addVertex(matrix, 135F, 135F, 1F).setColor(-1).setUv(1F, 1F).setLight(light);
		vertex.addVertex(matrix, 135F, -7F, 1F).setColor(-1).setUv(1F, 0F).setLight(light);
		vertex.addVertex(matrix, -7F, -7F, 1F).setColor(-1).setUv(0F, 0F).setLight(light);
	}
	private static void renderText(PoseStack poseStack, MultiBufferSource buffer, int light, ItemStack stack) {
		poseStack.scale(0.55F, 0.55F, 1F);
		poseStack.translate(16F, 16F, 0F);
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
		// 渲染页数指示器
		var currentPage = Mth.clamp(content.previouslyOpenedPage(), 0, pages.size() - 1);
		var entries = pages.get(currentPage);
		if (entries.isEmpty()) return;
		var indicatorComponent = Component.translatable("book.pageIndicator", currentPage + 1, pages.size());
		var indicator = indicatorComponent.getString();
		// 与缩放剪贴板标题区域的视觉中心相匹配
		var slashCenterX = 98F;
		var slashIndex = indicator.indexOf('/');
		float indicatorX;
		if (slashIndex >= 0) {
			var leftPart = indicator.substring(0, slashIndex);
			indicatorX = slashCenterX - font.width(leftPart) - font.width("/") / 2F;
		} else indicatorX = slashCenterX - font.width(indicator) / 2F;
		font.drawInBatch(indicator, indicatorX, 205, normalC, false, matrix, buffer, mode, 0, light);
		// 渲染剪贴板内容
		int x = 15, y = 15;
		for (var entry : entries) {
			var text = entry.text.getString();
			var address = text.startsWith("#") && !text.substring(1).isBlank();
			if (address) text = text.substring(1).stripLeading();
			if (text.isBlank()) continue;
			// 渲染包裹图标
			var checked = entry.checked;
			var checkColor = checked ? lightC : normalC;
			if (address) renderAddressIcon(poseStack, buffer, light, checked, x, y + 1);
				// 渲染勾选框
			else {
				font.drawInBatch("□", x, y, checkColor, false, matrix, buffer, mode, 0, light);
				if (checked) font.drawInBatch("✔", x, y - 1, greenC, false, matrix, buffer, mode, 0, light);
			}
			// 渲染每行文本
			for (var sequence : font.split(Component.literal(text), 150)) {
				var textColor = address ? checkColor : checked ? greenC : normalC;
				font.drawInBatch(sequence, x + 10, y, textColor, false, matrix, buffer, mode, 0, light);
				y += 10;
			}
			y += 3;
		}
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
