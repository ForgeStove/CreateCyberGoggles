package io.github.forgestove.create_cyber_goggles.content.schematic;
import com.simibubi.create.foundation.gui.widget.IconButton;
import net.createmod.catnip.gui.element.ScreenElement;
import net.minecraft.network.chat.Component;

import java.util.List;
public class SchematicExportButton extends IconButton {
	public SchematicExportButton(int x, int y, ScreenElement icon) {
		super(x, y, icon);
	}
	public void setToolTipLines(List<Component> lines) {
		toolTip.clear();
		toolTip.addAll(lines);
	}
}
