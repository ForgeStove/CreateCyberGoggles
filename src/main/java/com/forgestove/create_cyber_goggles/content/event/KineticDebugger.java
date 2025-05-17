package com.forgestove.create_cyber_goggles.content.event;
import com.forgestove.create_cyber_goggles.content.config.CCGConfig;
import com.forgestove.create_cyber_goggles.content.util.Common;
import com.simibubi.create.content.kinetics.base.*;
import net.createmod.catnip.math.VecHelper;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.phys.Vec3;

import java.awt.Color;
public class KineticDebugger {
	public static void tick() {
		if (!CCGConfig.get().other.rainbowDebug) return;
		if (Minecraft.getInstance().isPaused()) return;
		var kbe = Common.getSelectedKBE();
		if (kbe == null) return;
		renderOutline(kbe, 0);
		renderAxisLine(kbe);
	}
	public static void renderOutline(KineticBlockEntity kbe, int depth) {
		var toOutline = kbe.hasSource() ? kbe.source : kbe.getBlockPos();
		if (toOutline == null) return;
		var level = Minecraft.getInstance().level;
		if (level == null) return;
		var shape = level.getBlockState(toOutline).getBlockSupportShape(level, toOutline);
		if (kbe.getTheoreticalSpeed() == 0 || shape.isEmpty()) return;
		var time = System.currentTimeMillis();
		var hue = 1.0f - (depth * 0.05f - (time % 6000L) / 3000f) % 1.0f;
		var rgb = Color.HSBtoRGB(hue, 0.8f, 1.0f);
		Outliner.getInstance().chaseAABB("kineticSource" + toOutline, shape.bounds().move(toOutline)).lineWidth(1 / 16f).colored(rgb);
		renderKineticLine(kbe, rgb);
		if (kbe.source == null) return;
		if (!(level.getBlockEntity(kbe.source) instanceof KineticBlockEntity kbeSource)) return;
		renderOutline(kbeSource, depth + 1);
	}
	public static void renderKineticLine(KineticBlockEntity kbe, int rgb) {
		if (kbe.source == null) return;
		var from = VecHelper.getCenterOf(kbe.getBlockPos());
		var to = VecHelper.getCenterOf(kbe.source);
		Outliner.getInstance().showLine("kineticLine" + kbe.getBlockPos() + "->" + kbe.source, from, to).lineWidth(1 / 8f).colored(rgb);
	}
	public static void renderAxisLine(KineticBlockEntity kbe) {
		var state = kbe.getBlockState();
		if (!(state.getBlock() instanceof IRotate iRotate)) return;
		var axis = iRotate.getRotationAxis(state);
		var vec = Vec3.atLowerCornerOf(Direction.get(AxisDirection.POSITIVE, axis).getNormal());
		var center = VecHelper.getCenterOf(kbe.getBlockPos());
		Outliner.getInstance().showLine("rotationAxis", center.add(vec), center.subtract(vec)).lineWidth(1 / 16f);
	}
}
