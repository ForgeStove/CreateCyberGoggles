package io.github.forgestove.create_cyber_goggles.core.tooltipRenderer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.equipment.clipboard.*;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.api.*;
import io.github.forgestove.create_cyber_goggles.core.factory.CCGRenderTypes;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.client.ClientHooks;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
@AutoTooltipRenderer
public final class ClipboardRenderer implements TooltipRenderer {
	private static final float SCALE = 0.5F;
	// 手持世界渲染中物品图标：NONE context 模型 1 方块 = 16px
	private static final float ICON_SCALE = 10F;
	// renderClipboardPage 对剪贴板坐标系的整体缩放
	private static final float HAND_SCALE = 0.01F * 0.4F * SCALE;
	public static void renderClipboardPage(PoseStack pose, MultiBufferSource buffer, int light, ItemStack stack) {
		pose.pushPose();
		var matrix = pose.last().pose();
		matrix.rotate(Axis.YP.rotationDegrees(180F)).rotate(Axis.ZP.rotationDegrees(180F)).translate(-0.25F, -0.3F, 0F).scale(HAND_SCALE);
		var renderType = CCGRenderTypes.textWithDepth(AllGuiTextures.CLIPBOARD.getLocation(), false);
		renderGuiTexure(AllGuiTextures.CLIPBOARD, pose, buffer, light, 0, 0, renderType);
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
				renderGuiTexure(texture, pose, buffer, light, x - 1, y, CCGRenderTypes.textWithDepth(texture.getLocation(), true));
			} else {
				font.drawInBatch("□", x, y, checked ? 0x668D7F6B : 0xFF8D7F6B, false, matrix, buffer, mode, 0, light);
				if (checked) font.drawInBatch("✔", x, y - 1, 0x31B25D, false, matrix, buffer, mode, 0, light);
			}
			// 渲染条目携带的物品图标（背景/文字不写深度后，3D 模型可正常通过深度测试）
			var iconOffset = entry.icon.isEmpty() ? 0 : 16;
			if (!entry.icon.isEmpty()) {
				pose.pushPose();
				pose.translate(x + 9 + 8F, y + 8F, 0F);
				// 外部矩阵为绕 X 180°，抵消后让图标正面朝玩家
				pose.mulPose(Axis.XP.rotationDegrees(180F));
				pose.mulPose(Axis.YP.rotationDegrees(180F));
				pose.scale(ICON_SCALE, ICON_SCALE, ICON_SCALE);
				// 手动渲染模型：NO_DEPTH_TEST 无视背景深度，始终显示在剪贴板上
				// 与 renderStatic 内部一致：translate(0.5) → 相机变换 → translate(-0.5)，模型绕中心旋转
				var itemRenderer = mc.getItemRenderer();
				var model = itemRenderer.getModel(entry.icon, mc.level, null, 0);
				var handled = ClientHooks.handleCameraTransforms(pose, model, ItemDisplayContext.NONE, false);
				pose.translate(-0.5F, -0.5F, -0.5F);
				itemRenderer.renderModelLists(
					handled,
					entry.icon,
					light,
					OverlayTexture.NO_OVERLAY,
					pose,
					buffer.getBuffer(CCGRenderTypes.ICON_NO_DEPTH)
				);
				pose.popPose();
			}
			for (var sequence : font.split(Component.literal(text), 150 - iconOffset)) {
				var textColor = checked ? address ? 0x668D7F6B : 0x31B25D : 0x311A00;
				font.drawInBatch(sequence, x + 13 + iconOffset, y, textColor, false, matrix, buffer, mode, 0, light);
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
			// 渲染条目携带的物品图标
			var iconOffset = entry.icon.isEmpty() ? 0 : 16;
			if (!entry.icon.isEmpty()) gui.renderItem(entry.icon, x1 + 9, y1);
			var color = checked ? address ? 0x668D7F6B : 0x31B25D : 0x311A00;
			for (var sequence : font.split(Component.literal(text), 150 - iconOffset)) {
				gui.drawString(font, sequence, x1 + 13 + iconOffset, y1, color, false);
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
