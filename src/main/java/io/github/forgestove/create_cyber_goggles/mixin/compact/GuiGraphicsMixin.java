package io.github.forgestove.create_cyber_goggles.mixin.compact;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.forgestove.create_cyber_goggles.api.Self;
import io.github.forgestove.create_cyber_goggles.core.event.ItemTooltip;
import io.github.forgestove.create_cyber_goggles.core.factory.CCGMods;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.inventory.tooltip.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.ClientHooks;
import org.spongepowered.asm.mixin.*;

import java.util.List;
@Pseudo
@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin implements Self<GuiGraphics> {
	@Shadow private ItemStack tooltipStack;
	@WrapMethod(method = "renderTooltipInternal")
	public void render(
		Font font,
		List<ClientTooltipComponent> components,
		int mouseX,
		int mouseY,
		ClientTooltipPositioner tooltipPositioner,
		Operation<Void> original
	) {
		if (!CCGMods.obscure_tooltips.isLoaded()) {
			original.call(font, components, mouseX, mouseY, tooltipPositioner);
			return;
		}
		if (!components.isEmpty()) {
			var preEvent = ClientHooks.onRenderTooltipPre(
				tooltipStack,
				thiz(),
				mouseX,
				mouseY,
				thiz().guiWidth(),
				thiz().guiHeight(),
				components,
				font,
				tooltipPositioner
			);
			ItemTooltip.renderTooltipPre(preEvent);
		}
		original.call(font, components, mouseX, mouseY, tooltipPositioner);
	}
}
