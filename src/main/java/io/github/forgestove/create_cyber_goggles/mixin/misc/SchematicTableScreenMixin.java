package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.simibubi.create.content.schematics.table.*;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.schematic.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.*;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
/**
 * 蓝图桌界面的本模组增强：递归扫描文件夹选择器（原有）、旁挂 3D 预览面板
 * （来自 Create: Schematic Preview，titlo10，MIT）、导出渲染图按钮与长文件名截断
 * （来自 Create: Blueprinted，MIT）。
 */
@Mixin(SchematicTableScreen.class)
public abstract class SchematicTableScreenMixin extends AbstractSimiContainerScreen<SchematicTableMenu> implements SchematicPreviewAccess {
	@Shadow protected AllGuiTextures background;
	@Shadow private ScrollInput schematicsArea;
	@Shadow private IconButton folderButton;
	@Shadow private IconButton refreshButton;
	@Shadow private Label schematicsLabel;
	@Shadow private List<Rect2i> extraAreas;
	@Shadow @Final private Component availableSchematicsTitle;
	@Unique private SchematicFolderSelector ccg$folderSelector;
	@Unique private SchematicTablePreviewController ccg$previewController;
	@Unique private SchematicExportController ccg$exportController;
	protected SchematicTableScreenMixin(SchematicTableMenu container, Inventory inv, Component title) {
		super(container, inv, title);
	}
	@Inject(method = "init", at = @At("RETURN"))
	private void initExtras(CallbackInfo ci) {
		if (CCG.config.schematic.recursiveSchematicScan) {
			ccg$folderSelector = new SchematicFolderSelector(
				leftPos,
				topPos + 2,
				folderButton.getX(),
				folderButton.getY(),
				this::addRenderableWidget,
				this::ccg$rebuildSchematicList,
				this::ccg$setSchematicInputVisible
			);
			refreshButton.withCallback(ccg$folderSelector::refresh);
		}
		ccg$previewController = new SchematicTablePreviewController(this::ccg$selectedSchematicIndex);
		ccg$previewController.updateArea(leftPos, topPos, background.getWidth(), extraAreas);
		ccg$exportController = new SchematicExportController(
			leftPos + 206,
			topPos + 1,
			this::ccg$selectedSchematicIndex,
			ccg$previewController.panel()
		);
		addRenderableWidget(ccg$exportController.button());
	}
	@Unique
	private void ccg$rebuildSchematicList() {
		if (!CCG.config.schematic.recursiveSchematicScan) return;
		var displaySchematics = SchematicImageUtil.refreshSchematicNames();
		if (schematicsArea != null) removeWidget(schematicsArea);
		if (!displaySchematics.isEmpty()) {
			schematicsArea = new SelectionScrollInput(leftPos + 45, topPos + 21, 139, 18).forOptions(displaySchematics)
				.titled(availableSchematicsTitle.plainCopy())
				.writingTo(schematicsLabel);
			schematicsArea.onChanged();
			addRenderableWidget(schematicsArea);
		} else {
			schematicsArea = null;
			schematicsLabel.text = CommonComponents.EMPTY;
		}
		if (ccg$folderSelector != null) ccg$folderSelector.reassertVisibility();
	}
	@Unique
	private void ccg$setSchematicInputVisible(boolean visible) {
		if (schematicsArea != null) {
			schematicsArea.visible = visible;
			schematicsArea.active = visible;
		}
		if (schematicsLabel != null) schematicsLabel.visible = visible;
	}
	@Override
	public SchematicPreviewPanel ccg$getPreviewPanel() {
		return ccg$previewController == null ? null : ccg$previewController.panel();
	}
	@Unique
	private int ccg$selectedSchematicIndex() {
		return schematicsArea == null ? -1 : schematicsArea.getState();
	}
	@Inject(method = "renderBg", at = @At("TAIL"))
	private void renderPreviewPanel(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY, CallbackInfo ci) {
		if (ccg$previewController == null) return;
		ccg$previewController.updateArea(leftPos, topPos, background.getWidth(), extraAreas);
		ccg$previewController.render(graphics, mouseX, mouseY);
	}
	@Inject(method = "getExtraAreas", at = @At("RETURN"), cancellable = true)
	private void includePreviewAreas(CallbackInfoReturnable<List<Rect2i>> cir) {
		var areas = new ArrayList<>(cir.getReturnValue());
		if (ccg$previewController != null) {
			var area = ccg$previewController.area();
			if (area != null) areas.add(area);
		}
		if (ccg$exportController != null) {
			var button = ccg$exportController.button();
			areas.add(new Rect2i(button.getX(), button.getY(), button.getWidth(), button.getHeight()));
		}
		cir.setReturnValue(List.copyOf(areas));
	}
	// endregion
	@Inject(method = "containerTick", at = @At("TAIL"))
	private void containerTickTail(CallbackInfo ci) {
		if (ccg$folderSelector != null) ccg$folderSelector.reassertVisibility();
		if (ccg$exportController != null) ccg$exportController.refreshTooltip();
	}
	/** 截断过长的文件名。 */
	@Inject(method = "renderBg", at = @At("HEAD"))
	private void truncateSchematicName(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY, CallbackInfo ci) {
		if (!CCG.config.schematic.truncateSchematicName) return;
		if (schematicsArea == null || schematicsLabel == null || schematicsLabel.text == null) return;
		var originalText = schematicsLabel.text.getString();
		if (originalText.isEmpty()) return;
		var truncated = SchematicLang.truncate(mc.font, originalText, schematicsArea.getWidth() - 5);
		if (!truncated.equals(originalText)) schematicsLabel.text = Component.literal(truncated);
	}
}
