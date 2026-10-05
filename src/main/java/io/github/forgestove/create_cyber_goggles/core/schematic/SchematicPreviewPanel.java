package io.github.forgestove.create_cyber_goggles.core.schematic;
import com.mojang.blaze3d.platform.GlStateManager.*;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.content.schematics.client.SchematicRenderer;
import com.simibubi.create.foundation.blockEntity.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.createmod.catnip.gui.UIRenderHelper;
import net.createmod.catnip.levelWrappers.SchematicLevel;
import net.createmod.catnip.render.*;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

import java.util.Locale;
/**
 * 蓝图 3D 预览面板，可拖拽旋转、滚轮缩放。
 * <p>
 * 移植自 Create: Schematic Preview（作者 titlo10，MIT），做了以下改动：配置改读 {@link CCG} 的 flexconfig、
 * 语言键迁到本模组命名空间、日志改用本模组 logger。
 */
public class SchematicPreviewPanel {
	private static final int BORDER = 1;
	private static final int CHECKER_SIZE = 10;
	private static final int CHECKER_LIGHT = 0xFF7E90BD;
	private static final int CHECKER_DARK = 0xFF6874AD;
	private static final int FRAME_COLOR = 0xFFFFFFFF;
	private String currentFile;
	private String pendingFile;
	private long pendingSince;
	private State state = State.NONE;
	private SchematicRenderer renderer;
	private BoundingBox frame = new BoundingBox(BlockPos.ZERO);
	private float yaw = CCG.config.schematic.preview.defaultYaw;
	private float pitch = CCG.config.schematic.preview.defaultPitch;
	private float zoom = CCG.config.schematic.preview.defaultZoom;
	private boolean dragging;
	private boolean wasDown;
	private double lastMouseX, lastMouseY;
	private int lastX, lastY, lastW, lastH;
	public void setSelected(String fileName) {
		if (fileName == null || fileName.isEmpty()) {
			clear();
			return;
		}
		if (fileName.equals(currentFile) || fileName.equals(pendingFile)) return;
		pendingFile = fileName;
		pendingSince = Util.getMillis();
		state = State.LOADING;
		currentFile = null;
		renderer = null;
		resetView();
	}
	public void clear() {
		currentFile = null;
		pendingFile = null;
		renderer = null;
		state = State.NONE;
	}
	/** 视角与缩放复位到配置的默认值 */
	public void resetView() {
		yaw = CCG.config.schematic.preview.defaultYaw;
		pitch = CCG.config.schematic.preview.defaultPitch;
		zoom = CCG.config.schematic.preview.defaultZoom;
	}
	/** 当前水平旋转角（度），供蓝图渲染取用 */
	public float yaw() {
		return yaw;
	}
	/** 当前俯仰角（度），供蓝图渲染取用 */
	public float pitch() {
		return pitch;
	}
	public void updateMouse(double mouseX, double mouseY, boolean leftDown) {
		if (leftDown && !wasDown && isMouseOver(mouseX, mouseY)) dragging = true;
		if (!leftDown) dragging = false;
		if (dragging) {
			yaw += (float) (mouseX - lastMouseX);
			pitch = Mth.clamp(pitch + (float) (mouseY - lastMouseY), -90F, 90F);
		}
		lastMouseX = mouseX;
		lastMouseY = mouseY;
		wasDown = leftDown;
	}
	public boolean isMouseOver(double mouseX, double mouseY) {
		return mouseX >= lastX && mouseX < lastX + lastW && mouseY >= lastY && mouseY < lastY + lastH;
	}
	public void onScroll(double scrollDelta) {
		zoom = Mth.clamp(zoom * (scrollDelta > 0 ? 1.1F : 1F / 1.1F), 0.25F, 5F);
	}
	public void render(GuiGraphics graphics, int x, int y, int w, int h) {
		var cols = Math.max(1, (w - 2 * BORDER) / CHECKER_SIZE);
		var rows = Math.max(1, (h - 2 * BORDER) / CHECKER_SIZE);
		w = cols * CHECKER_SIZE + 2 * BORDER;
		h = rows * CHECKER_SIZE + 2 * BORDER;
		lastX = x;
		lastY = y;
		lastW = w;
		lastH = h;
		tickLoad();
		var innerX = x + BORDER;
		var innerY = y + BORDER;
		var innerW = w - 2 * BORDER;
		var innerH = h - 2 * BORDER;
		drawCheckerboard(graphics, innerX, innerY, innerW, innerH);
		graphics.fill(x, y, x + w, y + BORDER, FRAME_COLOR);
		graphics.fill(x, y + h - BORDER, x + w, y + h, FRAME_COLOR);
		graphics.fill(x, y, x + BORDER, y + h, FRAME_COLOR);
		graphics.fill(x + w - BORDER, y, x + w, y + h, FRAME_COLOR);
		var mc = Minecraft.getInstance();
		if (state == State.OK && renderer != null) renderPreview(graphics, innerX, innerY, innerW, innerH);
		else drawCenteredStatus(graphics, mc, innerX, innerY, innerW, innerH);
	}
	private void tickLoad() {
		var loadDelay = CCG.config.schematic.preview.loadDelayMs;
		if (pendingFile != null && Util.getMillis() - pendingSince >= loadDelay) build(pendingFile);
	}
	/** 棋盘格底：让半透明/镂空的方块结构有可辨识的衬底 */
	private void drawCheckerboard(GuiGraphics graphics, int x, int y, int w, int h) {
		var cols = (w + CHECKER_SIZE - 1) / CHECKER_SIZE;
		var rows = (h + CHECKER_SIZE - 1) / CHECKER_SIZE;
		for (var row = 0; row < rows; row++)
			for (var col = 0; col < cols; col++) {
				var cx = x + col * CHECKER_SIZE;
				var cy = y + row * CHECKER_SIZE;
				var cw = Math.min(CHECKER_SIZE, x + w - cx);
				var ch = Math.min(CHECKER_SIZE, y + h - cy);
				graphics.fill(cx, cy, cx + cw, cy + ch, (row + col & 1) == 0 ? CHECKER_LIGHT : CHECKER_DARK);
			}
	}
	private void renderPreview(GuiGraphics graphics, int x, int y, int w, int h) {
		var centerX = x + w / 2.0F;
		var centerY = y + h / 2.0F;
		var maxDim = Math.max(1, Math.max(frame.getXSpan(), Math.max(frame.getYSpan(), frame.getZSpan())));
		var fit = Math.min(w, h) * 0.6F;
		var scale = fit / maxDim * zoom;
		graphics.enableScissor(x, y, x + w, y + h);
		graphics.flush();
		PoseStack ms = graphics.pose();
		ms.pushPose();
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		RenderSystem.enableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		Lighting.setupFor3DItems();
		ms.translate(centerX, centerY, 350F);
		ms.scale(scale, scale, scale);
		UIRenderHelper.flipForGuiRender(ms);
		ms.mulPose(Axis.XP.rotationDegrees(pitch));
		ms.mulPose(Axis.YP.rotationDegrees(yaw));
		ms.translate(
			-(frame.minX() + frame.maxX() + 1) / 2F,
			-(frame.minY() + frame.maxY() + 1) / 2F,
			-(frame.minZ() + frame.maxZ() + 1) / 2F
		);
		SuperRenderTypeBuffer buffer = DefaultSuperRenderTypeBuffer.getInstance();
		renderer.render(ms, buffer);
		buffer.draw();
		ms.popPose();
		RenderSystem.disableDepthTest();
		RenderSystem.disableBlend();
		RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
		graphics.disableScissor();
	}
	private void drawCenteredStatus(GuiGraphics graphics, Minecraft mc, int x, int y, int w, int h) {
		var text = state.getComponent();
		graphics.drawString(mc.font, text, x + (w - mc.font.width(text)) / 2, y + h / 2 - 4, 0xFFFFFFFF);
	}
	private void build(String fileName) {
		var mc = Minecraft.getInstance();
		Level level = mc.level;
		if (level == null || mc.player == null) {
			state = State.FAILED;
			return;
		}
		currentFile = fileName;
		pendingFile = null;
		try {
			var template = SchematicImageUtil.loadTemplateFromSchematicName(fileName);
			var templateSize = template.getSize();
			if (templateSize.equals(Vec3i.ZERO)) {
				state = State.EMPTY;
				return;
			}
			var volume = (long) templateSize.getX() * templateSize.getY() * templateSize.getZ();
			if (volume > CCG.config.schematic.preview.maxBlockVolume) {
				state = State.TOO_LARGE;
				return;
			}
			var fakeSchematicLevel = new SchematicLevel(level);
			template.placeInWorld(
				fakeSchematicLevel,
				BlockPos.ZERO,
				BlockPos.ZERO,
				new StructurePlaceSettings(),
				fakeSchematicLevel.getRandom(),
				Block.UPDATE_CLIENTS
			);
			for (var be : fakeSchematicLevel.getBlockEntities()) be.setLevel(fakeSchematicLevel);
			fixControllerBlockEntities(fakeSchematicLevel);
			// sable 的子维度不在这份模板的方块表里，得单独展开成渲染层级，否则预览里它们是空的
			SchematicImageUtil.attachSubLevels(template, fakeSchematicLevel, level);
			// 取景用包围盒而不是模板尺寸：子维度是整艘船，可能远超主模板的框
			frame = SchematicImageUtil.frameBounds(fakeSchematicLevel);
			renderer = new SchematicRenderer(fakeSchematicLevel);
			state = State.OK;
		} catch (Exception e) {
			CCG.LOGGER.warn("Failed to build schematic preview for '{}'", fileName, e);
			renderer = null;
			state = State.FAILED;
		}
	}
	/** 蓝图放置后多方块控制器的"最后已知位置"停留在世界坐标，需要按偏移量修正回虚拟层级，否则模型渲染错位 */
	private void fixControllerBlockEntities(SchematicLevel schematicLevel) {
		for (var blockEntity : schematicLevel.getBlockEntities()) {
			if (!(blockEntity instanceof IMultiBlockEntityContainer multiBlock)) continue;
			var lastKnownPos = multiBlock.getLastKnownPos();
			var currentPos = blockEntity.getBlockPos();
			if (lastKnownPos == null || multiBlock.isController() || lastKnownPos.equals(currentPos)) continue;
			var adjustedController = multiBlock.getController().offset(currentPos.subtract(lastKnownPos));
			if (multiBlock instanceof SmartBlockEntity smartBlockEntity) smartBlockEntity.markVirtual();
			multiBlock.setController(adjustedController);
		}
	}
	private enum State {
		NONE,
		LOADING,
		OK,
		EMPTY,
		TOO_LARGE,
		FAILED;
		private final Component component;
		State() {
			component = Component.translatable(CCG.ID + ".gui.schematicPreview." + toString().toLowerCase(Locale.ENGLISH));
		}
		public Component getComponent() {
			return component;
		}
	}
}
