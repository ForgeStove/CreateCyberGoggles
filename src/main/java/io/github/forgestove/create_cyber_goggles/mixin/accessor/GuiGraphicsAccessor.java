package io.github.forgestove.create_cyber_goggles.mixin.accessor;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.inventory.tooltip.*;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
@Mixin(GuiGraphicsExtractor.class)
public interface GuiGraphicsAccessor {
	@Invoker("setTooltipForNextFrameInternal")
	void ccg$setTooltipForNextFrameInternal(
		Font font,
		List<ClientTooltipComponent> components,
		int x,
		int y,
		ClientTooltipPositioner positioner,
		Identifier background,
		boolean force
	);
}
