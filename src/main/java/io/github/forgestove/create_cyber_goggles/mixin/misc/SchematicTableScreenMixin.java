package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.schematics.table.*;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.SchematicFolderUtil;
import net.minecraft.network.chat.*;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Paths;
import java.util.List;
@Mixin(value = SchematicTableScreen.class, remap = false)
public abstract class SchematicTableScreenMixin extends AbstractSimiContainerScreen<SchematicTableMenu> {
	@Shadow private ScrollInput schematicsArea;
	@Shadow private IconButton folderButton;
	@Shadow private IconButton refreshButton;
	@Shadow private Label schematicsLabel;
	@Shadow @Final private Component availableSchematicsTitle;
	@Unique private SelectionScrollInput ccg$folderArea;
	@Unique private Label ccg$folderLabel;
	@Unique private List<String> ccg$folders = List.of();
	protected SchematicTableScreenMixin(SchematicTableMenu container, Inventory inv, Component title) {
		super(container, inv, title);
	}
	@Inject(method = "init", at = @At("RETURN"))
	private void initFolderSelector(CallbackInfo ci) {
		if (!CCG.config.misc.recursiveSchematicScan) return;
		var x = leftPos;
		var y = topPos + 2;
		ccg$folders = SchematicFolderUtil.listSelectableFolders();
		List<? extends Component> folderOptions = ccg$folders.stream()
			.map(folder -> folder.isEmpty()
				? Component.translatable("create_cyber_goggles.screen.schematicTable.folderRoot")
				: Component.literal(folder))
			.toList();
		ccg$folderLabel = new Label(x + 51, y + 26, CommonComponents.EMPTY).withShadow();
		var selectedIndex = Math.max(0, ccg$folders.indexOf(SchematicFolderUtil.getSelectedFolder()));
		ccg$folderArea = (SelectionScrollInput) new SelectionScrollInput(x + 45, y + 21, 139, 18).forOptions(folderOptions)
			.titled(Component.translatable("create_cyber_goggles.screen.schematicTable.folderSelector"))
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
		var folderPickerButton = new IconButton(folderButton.getX() - 19, folderButton.getY(), AllIcons.I_VIEW_SCHEDULE);
		folderPickerButton.withCallback(() -> ccg$setFolderPickerVisible(!ccg$folderArea.visible));
		folderPickerButton.setToolTip(Component.translatable("create_cyber_goggles.screen.schematicTable.selectFolder"));
		addRenderableWidget(folderPickerButton);
		refreshButton.withCallback(this::ccg$refreshFoldersAndFiles);
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
			List<? extends Component> folderOptions = ccg$folders.stream()
				.map(folder -> folder.isEmpty()
					? Component.translatable("create_cyber_goggles.screen.schematicTable.folderRoot")
					: Component.literal(folder))
				.toList();
			ccg$folderArea.forOptions(folderOptions);
			ccg$folderArea.setState(Math.max(0, ccg$folders.indexOf(selectedFolder)));
			ccg$folderArea.onChanged();
		}
		ccg$rebuildSchematicList();
	}
	@Unique
	private void ccg$setFolderPickerVisible(boolean visible) {
		if (ccg$folderArea != null) {
			ccg$folderArea.visible = visible;
			ccg$folderArea.active = visible;
		}
		if (ccg$folderLabel != null) ccg$folderLabel.visible = visible;
		if (schematicsArea != null) {
			schematicsArea.visible = !visible;
			schematicsArea.active = !visible;
		}
		if (schematicsLabel != null) schematicsLabel.visible = !visible;
	}
	@Inject(method = "containerTick", at = @At("TAIL"))
	private void keepFileListHiddenWhenFolderPickerOpen(CallbackInfo ci) {
		if (!CCG.config.misc.recursiveSchematicScan) return;
		if (ccg$folderArea != null && ccg$folderArea.visible) {
			if (schematicsArea != null) schematicsArea.visible = false;
			if (schematicsLabel != null) schematicsLabel.visible = false;
		}
	}
	@Unique
	private void ccg$rebuildSchematicList() {
		if (!CCG.config.misc.recursiveSchematicScan) return;
		var schematicSender = CreateClient.SCHEMATIC_SENDER;
		schematicSender.refresh();
		var availableSchematics = schematicSender.getAvailableSchematics();
		List<? extends Component> displaySchematics = availableSchematics.stream().map(component -> {
			var value = component.getString().replace('\\', '/');
			var fileName = Paths.get(value).getFileName().toString();
			return Component.literal(fileName);
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
}
