package io.github.forgestove.create_cyber_goggles.schematic;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.*;
import io.github.forgestove.create_cyber_goggles.util.*;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.*;

import java.util.*;
import java.util.function.Consumer;
/**
 * 蓝图桌的递归文件夹选择器，与 Create 自带的蓝图滚动框互斥显示。
 */
public class SchematicFolderSelector {
	private final Runnable rebuildSchematicList;
	private final Consumer<Boolean> setSchematicInputVisible;
	private final SelectionScrollInput area;
	private final Label label;
	private final IconButton pickerButton;
	private List<String> folders;
	public SchematicFolderSelector(
		int x,
		int y,
		int folderButtonX,
		int folderButtonY,
		Consumer<AbstractWidget> addWidget,
		Runnable rebuildSchematicList,
		Consumer<Boolean> setSchematicInputVisible
	) {
		this.rebuildSchematicList = rebuildSchematicList;
		this.setSchematicInputVisible = setSchematicInputVisible;
		folders = SchematicFolderUtil.listSelectableFolders();
		label = new Label(x + 51, y + 26, CommonComponents.EMPTY).withShadow();
		area = (SelectionScrollInput) new SelectionScrollInput(x + 45, y + 21, 139, 18).forOptions(options())
			.titled(CCGLang.translateDirect("gui.schematicTable.folderSelector"))
			.writingTo(label)
			.calling(state -> {
				if (state < 0 || state >= folders.size()) return;
				SchematicFolderUtil.setSelectedFolder(folders.get(state));
				rebuildSchematicList.run();
			});
		pickerButton = new IconButton(folderButtonX - 19, folderButtonY, AllIcons.I_VIEW_SCHEDULE);
		pickerButton.withCallback(() -> setVisible(!area.visible));
		pickerButton.setToolTip(CCGLang.translateDirect("gui.schematicTable.selectFolder"));
		area.setState(Math.max(0, folders.indexOf(SchematicFolderUtil.getSelectedFolder())));
		area.onChanged();
		setVisible(false);
		addWidget.accept(area);
		addWidget.accept(label);
		addWidget.accept(pickerButton);
	}
	public void refresh() {
		folders = SchematicFolderUtil.listSelectableFolders();
		String selectedFolder = SchematicFolderUtil.getSelectedFolder();
		if (!selectedFolder.isEmpty() && !folders.contains(selectedFolder)) {
			SchematicFolderUtil.setSelectedFolder("");
			selectedFolder = "";
		}
		area.forOptions(options());
		area.setState(Math.max(0, folders.indexOf(selectedFolder)));
		area.onChanged();
		rebuildSchematicList.run();
	}
	/** 选择器显示时重新隐藏刚重建的蓝图滚动框 */
	public void reassertVisibility() {
		if (area.visible) setVisible(true);
	}
	private void setVisible(boolean visible) {
		area.visible = visible;
		area.active = visible;
		label.visible = visible;
		pickerButton.green = visible;
		setSchematicInputVisible.accept(!visible);
	}
	private List<Component> options() {
		List<Component> list = new ArrayList<>();
		for (String folder : folders) {
			Component component = folder.isEmpty() ? CCGLang.translateDirect("gui.schematicTable.folderRoot") : Component.literal(folder);
			list.add(component);
		}
		return list;
	}
}
