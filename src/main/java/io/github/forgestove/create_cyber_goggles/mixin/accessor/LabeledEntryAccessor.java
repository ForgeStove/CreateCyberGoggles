package io.github.forgestove.create_cyber_goggles.mixin.accessor;
import net.createmod.catnip.config.ui.ConfigScreenList.LabeledEntry;
import net.createmod.catnip.gui.element.TextStencilElement;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
@Mixin(LabeledEntry.class)
public interface LabeledEntryAccessor {
	@Accessor
	TextStencilElement getLabel();
	@Accessor
	List<Component> getLabelTooltip();
	@Accessor
	void setUnit(String unit);
}
