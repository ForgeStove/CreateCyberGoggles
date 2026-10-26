package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.simibubi.create.content.logistics.factoryBoard.*;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.CCGCreateLang;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
@Mixin(FactoryPanelSetItemScreen.class)
public abstract class FactoryPanelSetItemScreenMixin extends AbstractSimiContainerScreen<FactoryPanelSetItemMenu> {
	public FactoryPanelSetItemScreenMixin(FactoryPanelSetItemMenu container, Inventory inv, Component title) {
		super(container, inv, title);
	}
	@Inject(method = "init", at = @At("TAIL"))
	public void init(CallbackInfo ci) {
		if (!CCG.config.goggles.betterFactoryGauge) return;
		int x = getGuiLeft();
		int y = getGuiTop();
		FactoryPanelBehaviour behaviour = getMenu().contentHolder;
		var newInputButton = new IconButton(x + 4, y + 63, AllIcons.I_ADD);
		newInputButton.withCallback(() -> {
			FactoryPanelConnectionHandler.startConnection(behaviour);
			mc.setScreen(null);
		});
		newInputButton.setToolTip(CCGCreateLang.translate("gui.factory_panel.connect_input").component());
		var relocateButton = new IconButton(x + 26, y + 63, AllIcons.I_MOVE_GAUGE);
		relocateButton.withCallback(() -> {
			FactoryPanelConnectionHandler.startRelocating(behaviour);
			mc.setScreen(null);
		});
		relocateButton.setToolTip(CCGCreateLang.translate("gui.factory_panel.relocate").component());
		addRenderableWidget(newInputButton);
		addRenderableWidget(relocateButton);
	}
}
