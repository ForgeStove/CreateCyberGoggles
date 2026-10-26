package io.github.forgestove.create_cyber_goggles.schematic;
import com.simibubi.create.foundation.gui.AllIcons;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.CCGConfig.Schematic.Image;
import io.github.forgestove.create_cyber_goggles.schematic.SchematicRenderSettings.*;
import io.github.forgestove.create_cyber_goggles.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.function.IntSupplier;
/**
 * 蓝图桌的导出渲染图按钮：Shift 切换分辨率、Ctrl 切换等轴方向。
 */
public class SchematicExportController {
	private final SchematicExportButton button;
	private final IntSupplier schematicIndex;
	@Nullable private final SchematicPreviewPanel previewPanel;
	private final boolean shiftWasDownOnInit;
	private final boolean ctrlWasDownOnInit;
	public SchematicExportController(int x, int y, IntSupplier schematicIndex, @Nullable SchematicPreviewPanel previewPanel) {
		this.schematicIndex = schematicIndex;
		this.previewPanel = previewPanel;
		shiftWasDownOnInit = Screen.hasShiftDown();
		ctrlWasDownOnInit = Screen.hasControlDown();
		button = new SchematicExportButton(x, y, AllIcons.I_CONFIG_SAVE);
		button.withCallback(this::exportSchematicImage);
		refreshTooltip();
	}
	public SchematicExportButton button() {
		return button;
	}
	/** 提示行跟随 Shift / Ctrl 实时变化，故每次刷新都整份重建 */
	public void refreshTooltip() {
		Image image = CCG.config.schematic.image;
		boolean shift = shiftToggled();
		boolean ctrl = ctrlToggled();
		var lines = new ArrayList<Component>();
		lines.add(CCGLang.translate("gui.schematicTable.exportButton.title").color(SchematicLang.DARK_BLUE).component());
		CCGLangBuilder resolution = CCGLang.translate("gui.schematicTable.exportButton.resolution")
			.style(ChatFormatting.GRAY)
			.add(Component.literal(String.valueOf(shift ? image.alternateWidth : image.defaultWidth)).withColor(SchematicLang.LIGHT_BLUE));
		if (!shift) resolution.add(CCGLang.translate(
			"gui.schematicTable.exportButton.resolutionHint",
			Component.literal("Shift").withStyle(ChatFormatting.GRAY),
			image.alternateWidth
		).style(ChatFormatting.DARK_GRAY));
		lines.add(resolution.component());
		// 沿用预览朝向时角度直接取自预览面板，等轴方向无从选择，故不显示这一行
		if (!image.usePreviewRotation || !CCG.config.schematic.preview.previewEnabled) {
			CCGLangBuilder direction = CCGLang.translate("gui.schematicTable.exportButton.direction").style(ChatFormatting.GRAY);
			direction.add(ctrl
				? CCGLang.translate("gui.schematicTable.exportButton.direction.left").color(SchematicLang.LIGHT_BLUE)
				: CCGLang.translate("gui.schematicTable.exportButton.direction.right").color(SchematicLang.LIGHT_BLUE));
			if (!ctrl) direction.add(CCGLang.translate(
					"gui.schematicTable.exportButton.directionHint",
					Component.literal("Ctrl").withStyle(ChatFormatting.GRAY)
				)
				.style(ChatFormatting.DARK_GRAY));
			lines.add(direction.component());
		}
		lines.add(Component.literal(" "));
		lines.add(CCGLang.translate("gui.schematicTable.exportButton.saveHint").style(ChatFormatting.GRAY).component());
		button.setToolTipLines(lines);
	}
	private void exportSchematicImage() {
		SchematicImageUtil.getSchematicNameFromIndex(schematicIndex.getAsInt()).ifPresent(fileName -> {
			Minecraft mc = Minecraft.getInstance();
			LocalPlayer player = mc.player;
			if (player == null) return;
			Builder settings = SchematicRenderSettings.builder()
				.imageWidth(shiftToggled() ? CCG.config.schematic.image.alternateWidth : CCG.config.schematic.image.defaultWidth)
				.orientation(orientation());
			mc.setScreen(null);
			new SchematicImageHandler(fileName, player.createCommandSourceStack(), settings).export();
		});
	}
	/** 仅在界面打开后按下的修饰键计为切换，避免打开时已按住的键被误判 */
	private boolean shiftToggled() {
		return Screen.hasShiftDown() && !shiftWasDownOnInit;
	}
	private boolean ctrlToggled() {
		return Screen.hasControlDown() && !ctrlWasDownOnInit;
	}
	/** 预览面板已载入且启用「沿用预览朝向」时取面板角度，否则用等轴视角 */
	private Orientation orientation() {
		if (CCG.config.schematic.image.usePreviewRotation && previewPanel != null)
			return new Orientation(previewPanel.yaw(), previewPanel.pitch());
		return ctrlToggled() ? Orientation.ISOMETRIC_LEFT : Orientation.ISOMETRIC_RIGHT;
	}
}
