package io.github.forgestove.create_cyber_goggles.core.event.forceOverlay;
import dev.ryanhcode.sable.api.physics.force.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.event.forceOverlay.ForceClusterer.Cluster;
import io.github.forgestove.create_cyber_goggles.core.util.CCGLang;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.*;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.jetbrains.annotations.*;

import java.awt.Point;
import java.util.*;
import java.util.Map.Entry;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
/**
 * HUD 提示框，显示当前目标子层级的总质量和每个力组的净力大小。
 */
public final class ForceTooltipOverlay {
	// 缓存：仅在簇数据引用或质量变化时重建行，避免每帧分配
	private static @Nullable Map<ResourceLocation, List<Cluster>> linesSource;
	private static double linesMass = Double.NaN;
	private static List<Component> linesCache = List.of();
	public static void register(@NotNull RegisterGuiLayersEvent event) {
		event.registerAbove(VanillaGuiLayers.HOTBAR, getCCGRes("force_tooltip"), ForceTooltipOverlay::render);
	}
	private static void render(GuiGraphics gui, DeltaTracker deltaTracker) {
		if (!CCG.config.aeronautics.forceOverlay.hudPanelEnabled) return;
		if (shouldSuppressInfo()) return;
		if (mc.options.hideGui) return;
		if (!ForceOverlay.hasData()) return;
		List<Component> lines = buildLines();
		if (lines.isEmpty()) return;
		drawHud(gui, mc.font, lines);
	}
	private static List<Component> buildLines() {
		Map<ResourceLocation, List<Cluster>> clusters = ForceOverlay.smoothedClusters();
		double mass = ForceOverlay.currentMass();
		if (clusters == linesSource && mass == linesMass) return linesCache;
		List<Component> lines = new ArrayList<>();
		lines.add(labeledLine(
			CCGLang.translateDirect("hud.forceOverlay.mass"),
			0xCCCCCC,
			CCGLang.translateDirect("hud.forceOverlay.massValue", formatScalar(mass))
		));
		if (clusters != null) for (Entry<ResourceLocation, List<Cluster>> entry : clusters.entrySet()) {
			ForceGroup group = ForceGroups.REGISTRY.get(entry.getKey());
			if (group == null) continue;
			double netMagnitude = 0;
			for (Cluster c : entry.getValue()) netMagnitude += c.force().length();
			if (netMagnitude <= 0) continue;
			lines.add(labeledLine(
				group.name(),
				0xFF000000 | group.color(),
				CCGLang.translateDirect("hud.forceOverlay.forceValue", formatScalar(netMagnitude))
			));
		}
		linesSource = clusters;
		linesMass = mass;
		linesCache = lines;
		return lines;
	}
	private static void drawHud(GuiGraphics gui, Font font, List<Component> lines) {
		Point pos = CCG.config.aeronautics.forceOverlay.forceOverlayPos;
		int offsetX = pos.x;
		int offsetY = pos.y;
		int lineHeight = 9 + 1;
		var maxLine = 0;
		for (Component line : lines) maxLine = Math.max(maxLine, font.width(line));
		int textHeight = lines.size() * lineHeight - 1;
		int textX = gui.guiWidth() - 4 - 3 - 1 - maxLine + offsetX;
		int innerX1 = textX - 3;
		int innerX2 = textX + maxLine + 3;
		int innerY2 = 8 + textHeight + 3 + offsetY;
		// 背景
		var bgColor = 0xE0202020;
		gui.fill(innerX1, 4 + offsetY, innerX2, 5 + offsetY, bgColor);
		gui.fill(innerX1, innerY2, innerX2, innerY2 + 1, bgColor);
		gui.fill(innerX1, 5 + offsetY, innerX2, innerY2, bgColor);
		gui.fill(innerX1 - 1, 5 + offsetY, innerX1, innerY2, bgColor);
		gui.fill(innerX2, 5 + offsetY, innerX2 + 1, innerY2, bgColor);
		// 边框
		var borderColor = 0xE05A5A5A;
		gui.fill(innerX1, 5 + offsetY, innerX2, 6 + offsetY, borderColor);
		gui.fill(innerX1, innerY2 - 1, innerX2, innerY2, borderColor);
		gui.fill(innerX1, 6 + offsetY, innerX1 + 1, innerY2 - 1, borderColor);
		gui.fill(innerX2 - 1, 6 + offsetY, innerX2, innerY2 - 1, borderColor);
		int y = 8 + offsetY;
		for (Component line : lines) {
			gui.drawString(font, line, textX, y, 0xFFFFFF, false);
			y += lineHeight;
		}
	}
	private static Component labeledLine(Component label, int labelColor, Component value) {
		MutableComponent out = Component.empty();
		out.append(label.copy().withStyle(Style.EMPTY.withColor(TextColor.fromRgb(labelColor & 0xFFFFFF))));
		out.append(Component.literal(": ").withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xB0B0B0))));
		out.append(value.copy().withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFFF)).withBold(false)));
		return out.withStyle(ChatFormatting.RESET);
	}
	static String formatScalar(double value) {
		return !Double.isFinite(value) ? "—" : String.format(Locale.ROOT, "%,.2f", value);
	}
}
