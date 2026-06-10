package io.github.forgestove.create_cyber_goggles.core.gui;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.content.equipment.clipboard.ClipboardBlockItem;
import com.zurrtum.create.infrastructure.component.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.api.TooltipOverlayRenderer;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.*;
import net.minecraft.util.*;
import net.minecraft.world.item.ItemStack;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
public final class ClipboardRenderer implements TooltipOverlayRenderer {
	private static final float SCALE = 0.5F;
	public static void renderClipboardPage(PoseStack pose, SubmitNodeCollector nodeCollector, int light, ItemStack stack) {
		pose.pushPose();
		pose.mulPose(Axis.YP.rotationDegrees(180F));
		pose.mulPose(Axis.ZP.rotationDegrees(180F));
		pose.translate(-0.25F, -0.3F, 0F);
		var scale = 0.01F * 0.4F * SCALE;
		pose.scale(scale, scale, scale);
		renderGuiTexure(
			AllGuiTextures.CLIPBOARD,
			pose,
			nodeCollector,
			light,
			0,
			0,
			RenderTypes.text(AllGuiTextures.CLIPBOARD.getLocation())
		);
		renderText(pose, nodeCollector, light, stack);
		pose.popPose();
	}
	private static void renderGuiTexure(
		AllGuiTextures texture,
		PoseStack pose,
		SubmitNodeCollector nodeCollector,
		int light,
		int x,
		int y,
		RenderType renderType
	) {
		pose.pushPose();
		pose.translate(x, y, 0F);
		var startX = texture.getStartX();
		var startY = texture.getStartY();
		var width = texture.getWidth();
		var height = texture.getHeight();
		var minU = startX / 256F;
		var minV = startY / 256F;
		var maxU = (startX + width) / 256F;
		var maxV = (startY + height) / 256F;
		nodeCollector.submitCustomGeometry(
			pose, renderType, (poseMatrix, vertexConsumer) -> {
				vertexConsumer.addVertex(poseMatrix, 0F, height, 0F)
					.setColor(-1)
					.setUv(minU, maxV)
					.setOverlay(OverlayTexture.NO_OVERLAY)
					.setLight(light)
					.setNormal(poseMatrix, 0F, 0F, 1F);
				vertexConsumer.addVertex(poseMatrix, width, height, 0F)
					.setColor(-1)
					.setUv(maxU, maxV)
					.setOverlay(OverlayTexture.NO_OVERLAY)
					.setLight(light)
					.setNormal(poseMatrix, 0F, 0F, 1F);
				vertexConsumer.addVertex(poseMatrix, width, 0F, 0F)
					.setColor(-1)
					.setUv(maxU, minV)
					.setOverlay(OverlayTexture.NO_OVERLAY)
					.setLight(light)
					.setNormal(poseMatrix, 0F, 0F, 1F);
				vertexConsumer.addVertex(poseMatrix, 0F, 0F, 0F)
					.setColor(-1)
					.setUv(minU, minV)
					.setOverlay(OverlayTexture.NO_OVERLAY)
					.setLight(light)
					.setNormal(poseMatrix, 0F, 0F, 1F);
			}
		);
		pose.popPose();
	}
	private static void renderText(PoseStack pose, SubmitNodeCollector nodeCollector, int light, ItemStack stack) {
		var font = mc.font;
		var mode = DisplayMode.POLYGON_OFFSET;
		var content = stack.getOrDefault(AllDataComponents.CLIPBOARD_CONTENT, ClipboardContent.EMPTY);
		var pages = ClipboardEntry.readAll(content);
		if (pages.isEmpty()) return;
		var currentPage = Mth.clamp(content.previouslyOpenedPage(), 0, pages.size() - 1);
		var entries = pages.get(currentPage);
		if (entries.isEmpty()) return;
		int x = 45, y = 50;
		for (var entry : entries) {
			var text = entry.text.getString();
			var address = text.startsWith("#") && !text.substring(1).isBlank();
			if (address) text = text.substring(1).stripLeading();
			if (text.isBlank()) continue;
			var checked = entry.checked;
			if (address) {
				var texture = checked ? AllGuiTextures.CLIPBOARD_ADDRESS_INACTIVE : AllGuiTextures.CLIPBOARD_ADDRESS;
				renderGuiTexure(texture, pose, nodeCollector, light, x - 1, y, RenderTypes.text(texture.getLocation()));
			} else {
				var boxColor = checked ? 0x668D7F6B : 0xFF8D7F6B;
				nodeCollector.submitText(pose, x, y, FormattedCharSequence.forward("□", Style.EMPTY), false, mode, light, boxColor, 0, 0);
				if (checked) nodeCollector.submitText(
					pose,
					x,
					y - 1,
					FormattedCharSequence.forward("✔", Style.EMPTY),
					false,
					mode,
					light,
					0x31B25D,
					0,
					0
				);
			}
			for (var sequence : font.split(Component.literal(text), 150)) {
				var textColor = checked ? address ? 0x668D7F6B : 0x31B25D : 0x311A00;
				nodeCollector.submitText(pose, x + 13, y, sequence, false, mode, light, textColor, 0, 0);
				y += 9;
			}
			y += 3;
		}
		var pageIndicator = Component.translatable("book.pageIndicator", currentPage + 1, pages.size());
		var indicator = pageIndicator.getString();
		var slashCenterX = AllGuiTextures.CLIPBOARD.getWidth() / 2F;
		var slashIndex = indicator.indexOf('/');
		float indicatorX;
		if (slashIndex >= 0) {
			var leftPart = indicator.substring(0, slashIndex);
			indicatorX = slashCenterX - font.width(leftPart) - font.width("/") / 2F;
		} else indicatorX = slashCenterX - font.width(indicator) / 2F;
		nodeCollector.submitText(
			pose,
			indicatorX,
			235,
			FormattedCharSequence.forward(indicator, Style.EMPTY),
			false,
			mode,
			light,
			0x311A00,
			0,
			0
		);
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
		var content = stack.getOrDefault(AllDataComponents.CLIPBOARD_CONTENT, ClipboardContent.EMPTY);
		var pages = ClipboardEntry.readAll(content);
		if (pages.isEmpty()) return;
		var currentPage = Mth.clamp(content.previouslyOpenedPage(), 0, pages.size() - 1);
		var entries = pages.get(currentPage);
		if (entries.isEmpty()) return;
		var pose = gui.pose();
		pose.pushMatrix();
		pose.translate(x - 12, y);
		pose.scale(SCALE, SCALE);
		AllGuiTextures.CLIPBOARD.render(gui, 0, 0);
		var font = mc.font;
		int x1 = 45, y1 = 50;
		for (var entry : entries) {
			var raw = entry.text.getString();
			var address = raw.startsWith("#") && !raw.substring(1).isBlank();
			var text = address ? raw.substring(1).stripLeading() : raw;
			if (text.isBlank()) continue;
			var checked = entry.checked;
			if (address) {
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
		pose.popMatrix();
	}
}
