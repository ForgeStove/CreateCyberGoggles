package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.zurrtum.create.client.content.logistics.factoryBoard.*;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.menu.AbstractSimiContainerScreen;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelSetItemMenu;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
@Mixin(FactoryPanelSetItemScreen.class)
public abstract class FactoryPanelSetItemScreenMixin extends AbstractSimiContainerScreen<FactoryPanelSetItemMenu> {
	public FactoryPanelSetItemScreenMixin(FactoryPanelSetItemMenu container, Inventory inv, Component title) {
		super(container, inv, title, 0, 0);
	}
	@Inject(method = "init", at = @At("TAIL"))
	public void init(CallbackInfo ci) {
		if (!CCG.config.goggles.betterFactoryGauge) return;
		var x = getGuiLeft();
		var y = getGuiTop();
		var behaviour = getMenu().contentHolder;
		var newInputButton = new IconButton(x + 4, y + 63, AllIcons.I_ADD);
		newInputButton.withCallback(() -> {
			FactoryPanelConnectionHandler.startConnection(behaviour);
			mc.setScreen(null);
		});
		newInputButton.setToolTip(CreateLang.translate("gui.factory_panel.connect_input").component());
		var relocateButton = new IconButton(x + 26, y + 63, AllIcons.I_MOVE_GAUGE);
		relocateButton.withCallback(() -> {
			FactoryPanelConnectionHandler.startRelocating(behaviour);
			mc.setScreen(null);
		});
		relocateButton.setToolTip(CreateLang.translate("gui.factory_panel.relocate").component());
		addRenderableWidget(newInputButton);
		addRenderableWidget(relocateButton);
	}
}
