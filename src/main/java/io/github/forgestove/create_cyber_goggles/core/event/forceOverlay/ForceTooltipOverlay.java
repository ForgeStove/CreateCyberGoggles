package io.github.forgestove.create_cyber_goggles.core.event.forceOverlay;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.CCGLang;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.*;
import net.minecraft.network.chat.*;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
/**
 * HUD 提示框，显示当前目标子层级的总质量和每个力组的净力大小。
 */
public final class ForceTooltipOverlay {
	public static void register(@NotNull RegisterGuiLayersEvent event) {
		event.registerAbove(VanillaGuiLayers.HOTBAR, getCCGRes("force_tooltip"), ForceTooltipOverlay::render);
	}
	private static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
		if (!CCG.config.aeronautics.forceOverlay.hudPanelEnabled) return;
		if (mc.options.hideGui) return;
		if (!ForceOverlay.hasData()) return;
		var lines = buildLines();
		if (lines.isEmpty()) return;
		drawHud(graphics, mc.font, lines);
	}
	private static List<Component> buildLines() {
		List<Component> lines = new ArrayList<>();
		var mass = ForceOverlay.currentMass();
		lines.add(labeledLine(
			CCGLang.translate("hud.force_overlay.mass").component(),
			0xCCCCCC,
			CCGLang.translate("hud.force_overlay.mass_value", formatScalar(mass)).component()
		));
		var clusters = ForceOverlay.smoothedClusters();
		if (clusters == null) return lines;
		for (var entry : clusters.entrySet()) {
			var group = ForceGroups.REGISTRY.get(entry.getKey());
			if (group == null) continue;
			double netMagnitude = 0;
			for (var c : entry.getValue()) netMagnitude += c.force().length();
			if (netMagnitude <= 0) continue;
			lines.add(labeledLine(
				group.name(),
				0xFF000000 | group.color(),
				CCGLang.translate("hud.force_overlay.force_value", formatScalar(netMagnitude)).component()
			));
		}
		return lines;
	}
	private static void drawHud(GuiGraphics graphics, Font font, List<Component> lines) {
		var pos = CCG.config.aeronautics.forceOverlay.forceOverlayPos;
		var offsetX = pos.x;
		var offsetY = pos.y;
		var lineHeight = 9 + 1;
		var maxLine = 0;
		for (var line : lines) maxLine = Math.max(maxLine, font.width(line));
		var textHeight = lines.size() * lineHeight - 1;
		var textX = graphics.guiWidth() - 4 - 3 - 1 - maxLine + offsetX;
		var innerX1 = textX - 3;
		var innerX2 = textX + maxLine + 3;
		var innerY2 = 8 + textHeight + 3 + offsetY;
		// 背景
		var bgColor = 0xE0202020;
		graphics.fill(innerX1, 4 + offsetY, innerX2, 5 + offsetY, bgColor);
		graphics.fill(innerX1, innerY2, innerX2, innerY2 + 1, bgColor);
		graphics.fill(innerX1, 5 + offsetY, innerX2, innerY2, bgColor);
		graphics.fill(innerX1 - 1, 5 + offsetY, innerX1, innerY2, bgColor);
		graphics.fill(innerX2, 5 + offsetY, innerX2 + 1, innerY2, bgColor);
		// 边框
		var borderColor = 0xE05A5A5A;
		graphics.fill(innerX1, 5 + offsetY, innerX2, 6 + offsetY, borderColor);
		graphics.fill(innerX1, innerY2 - 1, innerX2, innerY2, borderColor);
		graphics.fill(innerX1, 6 + offsetY, innerX1 + 1, innerY2 - 1, borderColor);
		graphics.fill(innerX2 - 1, 6 + offsetY, innerX2, innerY2 - 1, borderColor);
		var y = 8 + offsetY;
		for (var line : lines) {
			graphics.drawString(font, line, textX, y, 0xFFFFFF, false);
			y += lineHeight;
		}
	}
	private static Component labeledLine(Component label, int labelColor, Component value) {
		var out = Component.empty();
		out.append(label.copy().withStyle(Style.EMPTY.withColor(TextColor.fromRgb(labelColor & 0xFFFFFF))));
		out.append(Component.literal(": ").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xB0B0B0))));
		out.append(value.copy().withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFFF)).withBold(false)));
		return out.withStyle(ChatFormatting.RESET);
	}
	static String formatScalar(double value) {
		return !Double.isFinite(value) ? "—" : String.format(Locale.ROOT, "%,.2f", value);
	}
}
