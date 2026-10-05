package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.schematics.table.*;
import com.simibubi.create.foundation.gui.*;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.schematic.*;
import io.github.forgestove.create_cyber_goggles.core.schematic.SchematicRenderSettings.Orientation;
import io.github.forgestove.create_cyber_goggles.core.util.SchematicFolderUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.*;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.nio.file.Paths;
import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
/**
 * 蓝图桌界面的本模组增强：递归扫描文件夹选择器（原有）、旁挂 3D 预览面板
 * （来自 Create: Schematic Preview，titlo10，MIT）、导出渲染图按钮与长文件名截断
 * （来自 Create: Blueprinted，MIT）。
 */
@Mixin(SchematicTableScreen.class)
public abstract class SchematicTableScreenMixin extends AbstractSimiContainerScreen<SchematicTableMenu> implements SchematicPreviewAccess {
	@Unique private static final int CCG$PANEL_GAP = 4;
	@Unique private static final int CCG$SCREEN_MARGIN = 6;
	@Unique private static final int CCG$MIN_PANEL_SIZE = 60;
	@Shadow protected AllGuiTextures background;
	@Shadow private ScrollInput schematicsArea;
	@Shadow private IconButton folderButton;
	@Shadow private IconButton refreshButton;
	@Shadow private Label schematicsLabel;
	@Shadow private List<Rect2i> extraAreas;
	@Shadow @Final private Component availableSchematicsTitle;
	@Unique private SelectionScrollInput ccg$folderArea;
	@Unique private Label ccg$folderLabel;
	@Unique private IconButton ccg$folderPickerButton;
	@Unique private List<String> ccg$folders = List.of();
	@Unique private SchematicPreviewPanel ccg$panel;
	@Unique private Rect2i ccg$previewArea;
	@Unique private SchematicExportButton ccg$exportButton;
	@Unique private boolean ccg$shiftWasDownOnInit, ccg$ctrlWasDownOnInit;
	protected SchematicTableScreenMixin(SchematicTableMenu container, Inventory inv, Component title) {
		super(container, inv, title);
	}
	@Inject(method = "init", at = @At("HEAD"))
	private void captureModifiersOnInit(CallbackInfo ci) {
		ccg$shiftWasDownOnInit = hasShiftDown();
		ccg$ctrlWasDownOnInit = hasControlDown();
	}
	@Inject(method = "init", at = @At("RETURN"))
	private void initExtras(CallbackInfo ci) {
		ccg$initFolderSelector();
		ccg$initPreviewPanel();
		ccg$initExportButton();
	}
	// region 文件夹选择器
	@Unique
	private void ccg$initFolderSelector() {
		if (!CCG.config.misc.recursiveSchematicScan) return;
		var x = leftPos;
		var y = topPos + 2;
		ccg$folders = SchematicFolderUtil.listSelectableFolders();
		var folderOptions = ccg$folders.stream()
			.map(folder -> folder.isEmpty()
				? (Component) Component.translatable("create_cyber_goggles.gui.schematicTable.folderRoot")
				: Component.literal(folder))
			.toList();
		ccg$folderLabel = new Label(x + 51, y + 26, CommonComponents.EMPTY).withShadow();
		var selectedIndex = Math.max(0, ccg$folders.indexOf(SchematicFolderUtil.getSelectedFolder()));
		ccg$folderArea = (SelectionScrollInput) new SelectionScrollInput(x + 45, y + 21, 139, 18).forOptions(folderOptions)
			.titled(Component.translatable("create_cyber_goggles.gui.schematicTable.folderSelector"))
			.writingTo(ccg$folderLabel)
			.calling(state -> {
				if (state < 0 || state >= ccg$folders.size()) return;
				SchematicFolderUtil.setSelectedFolder(ccg$folders.get(state));
				ccg$rebuildSchematicList();
			});
		ccg$folderArea.setState(selectedIndex);
		ccg$folderArea.onChanged();
		ccg$setFolderPickerVisible(false);
		addRenderableWidget(ccg$folderArea);
		addRenderableWidget(ccg$folderLabel);
		ccg$folderPickerButton = new IconButton(folderButton.getX() - 19, folderButton.getY(), AllIcons.I_VIEW_SCHEDULE);
		ccg$folderPickerButton.withCallback(() -> ccg$setFolderPickerVisible(!ccg$folderArea.visible));
		ccg$folderPickerButton.setToolTip(Component.translatable("create_cyber_goggles.gui.schematicTable.selectFolder"));
		addRenderableWidget(ccg$folderPickerButton);
		refreshButton.withCallback(this::ccg$refreshFoldersAndFiles);
	}
	// endregion
	// region 3D 预览面板
	@Unique
	private void ccg$initPreviewPanel() {
		ccg$panel = new SchematicPreviewPanel();
		ccg$previewArea = ccg$calculatePreviewArea();
	}
	// endregion
	// region 导出渲染图
	@Unique
	private void ccg$initExportButton() {
		ccg$exportButton = new SchematicExportButton(leftPos + 206, topPos + 1, AllIcons.I_CONFIG_SAVE);
		ccg$exportButton.withCallback(this::ccg$exportSchematicImage);
		// 窗口尺寸变化会重建按钮，此时新按钮的 tooltip 为空，须立即补上
		ccg$updateExportTooltip();
		addRenderableWidget(ccg$exportButton);
	}
	@Unique
	private void ccg$rebuildSchematicList() {
		if (!CCG.config.misc.recursiveSchematicScan) return;
		var schematicSender = CreateClient.SCHEMATIC_SENDER;
		schematicSender.refresh();
		var availableSchematics = schematicSender.getAvailableSchematics();
		var displaySchematics = availableSchematics.stream().map(component -> {
			var value = component.getString().replace('\\', '/');
			return (Component) Component.literal(Paths.get(value).getFileName().toString());
		}).toList();
		if (schematicsArea != null) removeWidget(schematicsArea);
		if (!availableSchematics.isEmpty()) {
			schematicsArea = new SelectionScrollInput(leftPos + 45, topPos + 21, 139, 18).forOptions(displaySchematics)
				.titled(availableSchematicsTitle.plainCopy())
				.writingTo(schematicsLabel);
			schematicsArea.onChanged();
			addRenderableWidget(schematicsArea);
		} else {
			schematicsArea = null;
			schematicsLabel.text = CommonComponents.EMPTY;
		}
		if (ccg$folderArea != null && ccg$folderArea.visible) ccg$setFolderPickerVisible(true);
	}
	@Unique
	private void ccg$setFolderPickerVisible(boolean visible) {
		if (ccg$folderArea != null) {
			ccg$folderArea.visible = visible;
			ccg$folderArea.active = visible;
		}
		if (ccg$folderLabel != null) ccg$folderLabel.visible = visible;
		if (ccg$folderPickerButton != null) ccg$folderPickerButton.green = visible;
		if (schematicsArea != null) {
			schematicsArea.visible = !visible;
			schematicsArea.active = !visible;
		}
		if (schematicsLabel != null) schematicsLabel.visible = !visible;
	}
	@Unique
	private void ccg$refreshFoldersAndFiles() {
		if (!CCG.config.misc.recursiveSchematicScan) return;
		ccg$folders = SchematicFolderUtil.listSelectableFolders();
		var selectedFolder = SchematicFolderUtil.getSelectedFolder();
		if (!selectedFolder.isEmpty() && !ccg$folders.contains(selectedFolder)) {
			SchematicFolderUtil.setSelectedFolder("");
			selectedFolder = "";
		}
		if (ccg$folderArea != null) {
			var folderOptions = ccg$folders.stream()
				.map(folder -> folder.isEmpty()
					? (Component) Component.translatable("create_cyber_goggles.gui.schematicTable.folderRoot")
					: Component.literal(folder))
				.toList();
			ccg$folderArea.forOptions(folderOptions);
			ccg$folderArea.setState(Math.max(0, ccg$folders.indexOf(selectedFolder)));
			ccg$folderArea.onChanged();
		}
		ccg$rebuildSchematicList();
	}
	/** 优先贴左侧与界面的空隙，其次贴上方的空隙；两处均放不下则不显示 */
	@Unique
	private Rect2i ccg$calculatePreviewArea() {
		if (!CCG.config.schematic.preview.previewEnabled) return null;
		var self = (SchematicTableScreen) (Object) this;
		var window = mc.getWindow();
		var screenW = window.getGuiScaledWidth();
		var screenH = window.getGuiScaledHeight();
		var availableW = screenW - CCG$SCREEN_MARGIN * 2;
		var availableH = screenH - CCG$SCREEN_MARGIN * 2;
		var panelW = Math.clamp(availableW, 1, CCG.config.schematic.preview.sidePanelWidth);
		var panelH = Math.clamp(availableH, 1, CCG.config.schematic.preview.maxHeight);
		var minPanelH = Math.min(CCG$MIN_PANEL_SIZE, panelH);
		var leftPos = self.getGuiLeft();
		var topPos = self.getGuiTop();
		var occupiedLeft = leftPos;
		var occupiedTop = topPos;
		var occupiedRight = leftPos + background.getWidth();
		var occupiedBottom = topPos + background.getHeight() + 4 + AllGuiTextures.PLAYER_INVENTORY.getHeight();
		for (var area : extraAreas) {
			occupiedLeft = Math.min(occupiedLeft, area.getX());
			occupiedTop = Math.min(occupiedTop, area.getY());
			occupiedRight = Math.max(occupiedRight, area.getX() + area.getWidth());
			occupiedBottom = Math.max(occupiedBottom, area.getY() + area.getHeight());
		}
		var leftRoom = Math.max(0, occupiedLeft - CCG$PANEL_GAP - CCG$SCREEN_MARGIN);
		var aboveRoom = Math.max(0, occupiedTop - CCG$PANEL_GAP - CCG$SCREEN_MARGIN);
		int px, py;
		// 左侧放得下完整面板，或上方连最小高度都没有 → 贴左（宽度不足时按空隙收窄）
		if (leftRoom >= panelW || aboveRoom < minPanelH) {
			panelW = Math.min(panelW, leftRoom);
			px = occupiedLeft - CCG$PANEL_GAP - panelW;
			py = ccg$clamp(topPos, screenH - CCG$SCREEN_MARGIN - panelH);
		} else {
			// 界面上方的宽扁条（高度不足时按空隙压低）
			panelW = 204;
			occupiedLeft -= 54;
			panelH = Math.min(panelH, aboveRoom);
			px = ccg$clamp((occupiedLeft + occupiedRight - panelW) / 2, screenW - CCG$SCREEN_MARGIN - panelW);
			py = occupiedTop - CCG$PANEL_GAP - panelH;
		}
		return new Rect2i(px, py, panelW, panelH);
	}
	@Unique
	private void ccg$exportSchematicImage() {
		if (schematicsArea == null) return;
		SchematicImageUtil.getSchematicNameFromIndex(schematicsArea.getState()).ifPresent(fileName -> {
			Player player = Minecraft.getInstance().player;
			if (player == null) return;
			var settings = SchematicRenderSettings.builder()
				.imageWidth(ccg$shiftToggled() ? CCG.config.schematic.image.alternateWidth : CCG.config.schematic.image.defaultWidth)
				.orientation(ccg$orientation());
			mc.setScreen(null);
			new SchematicImageHandler(fileName, player.createCommandSourceStack(), settings).export();
		});
	}
	@Unique
	private int ccg$clamp(int value, int max) {
		return max < CCG$SCREEN_MARGIN ? CCG$SCREEN_MARGIN : Mth.clamp(value, CCG$SCREEN_MARGIN, max);
	}
	/** 仅在界面打开后按下的修饰键计为切换，避免打开时已按住的键被误判 */
	@Unique
	private boolean ccg$shiftToggled() {
		return hasShiftDown() && !ccg$shiftWasDownOnInit;
	}
	/** 预览面板已载入且启用「沿用预览朝向」时取面板角度，否则用等轴视角 */
	@Unique
	private Orientation ccg$orientation() {
		if (CCG.config.schematic.image.usePreviewRotation && ccg$panel != null) return new Orientation(ccg$panel.yaw(), ccg$panel.pitch());
		return ccg$ctrlToggled() ? Orientation.ISOMETRIC_LEFT : Orientation.ISOMETRIC_RIGHT;
	}
	@Unique
	private boolean ccg$ctrlToggled() {
		return hasControlDown() && !ccg$ctrlWasDownOnInit;
	}
	@Override
	public SchematicPreviewPanel ccg$getPreviewPanel() {
		return ccg$panel;
	}
	@Inject(method = "renderBg", at = @At("TAIL"))
	private void renderPreviewPanel(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY, CallbackInfo ci) {
		ccg$previewArea = ccg$calculatePreviewArea();
		if (ccg$previewArea == null || ccg$panel == null) return;
		if (schematicsArea != null)
			SchematicImageUtil.getSchematicNameFromIndex(schematicsArea.getState()).ifPresent(ccg$panel::setSelected);
		var window = mc.getWindow().getWindow();
		var leftDown = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
		ccg$panel.updateMouse(mouseX, mouseY, leftDown);
		ccg$panel.render(graphics, ccg$previewArea.getX(), ccg$previewArea.getY(), ccg$previewArea.getWidth(),
			ccg$previewArea.getHeight());
	}
	@Inject(method = "getExtraAreas", at = @At("RETURN"), cancellable = true)
	private void includePreviewAreas(CallbackInfoReturnable<List<Rect2i>> cir) {
		var areas = new ArrayList<>(cir.getReturnValue());
		if (ccg$previewArea != null) areas.add(ccg$previewArea);
		if (ccg$exportButton != null) areas.add(new Rect2i(
			ccg$exportButton.getX(),
			ccg$exportButton.getY(),
			ccg$exportButton.getWidth(),
			ccg$exportButton.getHeight()
		));
		cir.setReturnValue(List.copyOf(areas));
	}
	// endregion
	@Inject(method = "containerTick", at = @At("TAIL"))
	private void containerTickTail(CallbackInfo ci) {
		if (ccg$folderArea != null && ccg$folderArea.visible) {
			if (schematicsArea != null) schematicsArea.visible = false;
			if (schematicsLabel != null) schematicsLabel.visible = false;
		}
		ccg$updateExportTooltip();
	}
	/** 文件名过长会溢出滚动框，此处截断并补省略号。*/
	@Inject(method = "renderBg", at = @At("HEAD"))
	private void truncateSchematicName(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY, CallbackInfo ci) {
		if (!CCG.config.schematic.truncateSchematicName) return;
		if (schematicsArea == null || schematicsLabel == null || schematicsLabel.text == null) return;
		var originalText = schematicsLabel.text.getString();
		if (originalText.isEmpty()) return;
		var truncated = SchematicLang.truncate(mc.font, originalText, schematicsArea.getWidth() - 5);
		if (!truncated.equals(originalText)) schematicsLabel.text = Component.literal(truncated);
	}
	/** 提示行跟随 Shift / Ctrl 实时变化，故每次刷新都整份重建 */
	@Unique
	private void ccg$updateExportTooltip() {
		if (ccg$exportButton == null) return;
		var image = CCG.config.schematic.image;
		var shift = ccg$shiftToggled();
		var ctrl = ccg$ctrlToggled();
		var lines = new ArrayList<Component>();
		lines.add(SchematicLang.translatable("gui.schematicTable.exportButton.title").withColor(SchematicLang.DARK_BLUE));
		var resolution = SchematicLang.translatable("gui.schematicTable.exportButton.resolution")
			.withStyle(ChatFormatting.GRAY)
			.append(Component.literal(String.valueOf(shift ? image.alternateWidth : image.defaultWidth))
				.withColor(SchematicLang.LIGHT_BLUE));
		if (!shift) resolution.append(SchematicLang.translatable(
			"gui.schematicTable.exportButton.resolutionHint",
			Component.literal("Shift").withStyle(ChatFormatting.GRAY),
			image.alternateWidth
		).withStyle(ChatFormatting.DARK_GRAY));
		lines.add(resolution);
		// 沿用预览朝向时角度直接取自预览面板，等轴方向无从选择，故不显示这一行
		if (!image.usePreviewRotation || !CCG.config.schematic.preview.previewEnabled) {
			var direction = SchematicLang.translatable("gui.schematicTable.exportButton.direction")
				.withStyle(ChatFormatting.GRAY)
				.append(SchematicLang.translatable(ctrl
					? "gui.schematicTable.exportButton.direction.left"
					: "gui.schematicTable.exportButton.direction.right").withColor(SchematicLang.LIGHT_BLUE));
			if (!ctrl) direction.append(SchematicLang.translatable(
					"gui.schematicTable.exportButton.directionHint",
					Component.literal("Ctrl").withStyle(ChatFormatting.GRAY)
				)
				.withStyle(ChatFormatting.DARK_GRAY));
			lines.add(direction);
		}
		lines.add(Component.literal(" "));
		lines.add(SchematicLang.translatable("gui.schematicTable.exportButton.saveHint").withStyle(ChatFormatting.GRAY));
		ccg$exportButton.setToolTipLines(lines);
	}
}
