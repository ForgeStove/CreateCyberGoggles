package io.github.forgestove.create_cyber_goggles.core.schematic;
import com.simibubi.create.foundation.gui.widget.IconButton;
import net.createmod.catnip.gui.element.ScreenElement;
import net.minecraft.network.chat.Component;

import java.util.List;
/**
 * 导出按钮：Create 的 {@link IconButton} 只有单行 tooltip（{@code setToolTip(Component)} 只塞一个元素，
 * 而 tooltip 是按列表元素逐行渲染的，组件内部的 {@code \n} 不会被拆行），这里补一个多行版本。
 */
public class SchematicExportButton extends IconButton {
	public SchematicExportButton(int x, int y, ScreenElement icon) {
		super(x, y, icon);
	}
	public void setToolTipLines(List<Component> lines) {
		toolTip.clear();
		toolTip.addAll(lines);
	}
}
