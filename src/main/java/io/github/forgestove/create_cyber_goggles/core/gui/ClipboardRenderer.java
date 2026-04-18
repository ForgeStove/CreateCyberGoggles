package io.github.forgestove.create_cyber_goggles.core.gui;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.equipment.clipboard.*;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.*;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
public final class ClipboardRenderer implements TooltipOverlayRenderer {
	private static final float SCALE = 0.5F;
	public static void renderClipboardPage(PoseStack pose, MultiBufferSource buffer, int light, ItemStack stack) {
		pose.pushPose();
		var matrix = pose.last().pose();
		matrix.rotate(Axis.YP.rotationDegrees(180F))
			.rotate(Axis.ZP.rotationDegrees(180F))
			.translate(-0.25F, -0.3F, 0F)
			.scale(0.01F * 0.4F * SCALE);
		renderGuiTexure(AllGuiTextures.CLIPBOARD, pose, buffer, light, 0, 0, RenderType.text(AllGuiTextures.CLIPBOARD.getLocation()));
		renderText(pose, buffer, light, stack);
		pose.popPose();
	}
	private static void renderGuiTexure(
		AllGuiTextures texture,
		PoseStack pose,
		MultiBufferSource buffer,
		int light,
		int x,
		int y,
		RenderType renderType
	) {
		pose.pushPose();
		pose.translate(x, y, 0F);
		var matrix = pose.last().pose();
		var startX = texture.getStartX();
		var startY = texture.getStartY();
		var width = texture.getWidth();
		var height = texture.getHeight();
		var vertex = buffer.getBuffer(renderType);
		var minU = startX / 256F;
		var minV = startY / 256F;
		var maxU = (startX + width) / 256F;
		var maxV = (startY + height) / 256F;
		vertex.addVertex(matrix, 0F, height, 0F).setColor(-1).setUv(minU, maxV).setLight(light);
		vertex.addVertex(matrix, width, height, 0F).setColor(-1).setUv(maxU, maxV).setLight(light);
		vertex.addVertex(matrix, width, 0F, 0F).setColor(-1).setUv(maxU, minV).setLight(light);
		vertex.addVertex(matrix, 0F, 0F, 0F).setColor(-1).setUv(minU, minV).setLight(light);
		pose.popPose();
	}
	private static void renderText(PoseStack pose, MultiBufferSource buffer, int light, ItemStack stack) {
		var font = mc.font;
		var mode = DisplayMode.POLYGON_OFFSET;
		// 读取剪贴板内容
		var content = stack.getOrDefault(AllDataComponents.CLIPBOARD_CONTENT, ClipboardContent.EMPTY);
		var pages = ClipboardEntry.readAll(content);
		if (pages.isEmpty()) return;
		var currentPage = Mth.clamp(content.previouslyOpenedPage(), 0, pages.size() - 1);
		var entries = pages.get(currentPage);
		if (entries.isEmpty()) return;
		// 渲染剪贴板内容
		var matrix = pose.last().pose();
		int x = 45, y = 50;
		for (var entry : entries) {
			var text = entry.text.getString();
			var address = text.startsWith("#") && !text.substring(1).isBlank();
			if (address) text = text.substring(1).stripLeading();
			if (text.isBlank()) continue;
			var checked = entry.checked;
			if (address) {
				var texture = checked ? AllGuiTextures.CLIPBOARD_ADDRESS_INACTIVE : AllGuiTextures.CLIPBOARD_ADDRESS;
				renderGuiTexure(texture, pose, buffer, light, x - 1, y, RenderType.textPolygonOffset(texture.getLocation()));
			} else {
				font.drawInBatch("□", x, y, checked ? 0x668D7F6B : 0xFF8D7F6B, false, matrix, buffer, mode, 0, light);
				if (checked) font.drawInBatch("✔", x, y - 1, 0x31B25D, false, matrix, buffer, mode, 0, light);
			}
			for (var sequence : font.split(Component.literal(text), 150)) {
				var textColor = checked ? address ? 0x668D7F6B : 0x31B25D : 0x311A00;
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
		font.drawInBatch(indicator, indicatorX, 235, 0x311A00, false, matrix, buffer, mode, 0, light);
	}
	@Override
	public boolean supports(ItemStack stack) {
		return CCG.config.tooltip.clipboard && stack.getItem() instanceof ClipboardBlockItem;
	}
	@Override
	public boolean canRender(ItemStack stack) {
		var content = stack.getOrDefault(AllDataComponents.CLIPBOARD_CONTENT, ClipboardContent.EMPTY);
		var pages = ClipboardEntry.readAll(content);
		if (pages.isEmpty()) return false;
		var currentPage = Mth.clamp(content.previouslyOpenedPage(), 0, pages.size() - 1);
		return !pages.get(currentPage).isEmpty();
	}
	@Override
	public int width(ItemStack stack) {
		return Mth.ceil(AllGuiTextures.CLIPBOARD.getWidth() * SCALE);
	}
	@Override
	public int height(ItemStack stack) {
		return Mth.ceil(AllGuiTextures.CLIPBOARD.getHeight() * SCALE);
	}
	@Override
	public void render(GuiGraphics gui, ItemStack stack, int x, int y) {
		// 读取剪贴板内容
		var content = stack.getOrDefault(AllDataComponents.CLIPBOARD_CONTENT, ClipboardContent.EMPTY);
		var pages = ClipboardEntry.readAll(content);
		if (pages.isEmpty()) return;
		var currentPage = Mth.clamp(content.previouslyOpenedPage(), 0, pages.size() - 1);
		var entries = pages.get(currentPage);
		if (entries.isEmpty()) return;
		var pose = gui.pose();
		pose.pushPose();
		pose.translate(x - 12, y, 600F);
		pose.scale(SCALE, SCALE, 1F);
		AllGuiTextures.CLIPBOARD.render(gui, 0, 0);
		// 渲染剪贴板内容
		var font = mc.font;
		int x1 = 45, y1 = 50;
		for (var entry : entries) {
			var raw = entry.text.getString();
			var address = raw.startsWith("#") && !raw.substring(1).isBlank();
			var text = address ? raw.substring(1).stripLeading() : raw;
			if (text.isBlank()) continue;
			var checked = entry.checked;
			if (address) {
				RenderSystem.enableBlend();
				var texture = checked ? AllGuiTextures.CLIPBOARD_ADDRESS_INACTIVE : AllGuiTextures.CLIPBOARD_ADDRESS;
				texture.render(gui, x1 - 1, y1 + 1);
			} else {
				gui.drawString(font, "□", x1, y1 + 1, checked ? 0x668D7F6B : 0xFF8D7F6B, false);
				if (checked) gui.drawString(font, "✔", x1, y1, 0x31B25D, false);
			}
			var color = checked ? address ? 0x668D7F6B : 0x31B25D : 0x311A00;
			for (var sequence : font.split(Component.literal(text), 150)) {
				gui.drawString(font, sequence, x1 + 13, y1, color, false);
				y1 += 9;
			}
			y1 += 3;
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
		gui.drawString(font, pageIndicator, indicatorX, 235, 0x311A00, false);
		pose.popPose();
	}
}
