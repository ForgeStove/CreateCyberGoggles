package io.github.forgestove.create_cyber_goggles;
import io.github.forgestove.create_cyber_goggles.content.chainConveyor.ChainConveyorFlowHandler;
import io.github.forgestove.create_cyber_goggles.content.drafting.*;
import io.github.forgestove.create_cyber_goggles.content.forceOverlay.*;
import io.github.forgestove.create_cyber_goggles.content.goggles.*;
import io.github.forgestove.create_cyber_goggles.content.goggles.tooltip.*;
import io.github.forgestove.create_cyber_goggles.content.goggles.tooltip.component.*;
import io.github.forgestove.create_cyber_goggles.content.kinetic.*;
import io.github.forgestove.create_cyber_goggles.content.replenish.ReplenishScreen;
import io.github.forgestove.create_cyber_goggles.content.schematic.*;
import io.github.forgestove.create_cyber_goggles.foundation.events.*;
import io.github.forgestove.create_cyber_goggles.foundation.util.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.*;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

import static io.github.forgestove.create_cyber_goggles.CCG.ID;
@Mod(value = ID, dist = Dist.CLIENT)
public class CCGClient {
	public CCGClient(ModContainer container) {
		CCGGoggleEntries.register();
		CatnipConfigLocalization.registerSource();
		IEventBus mod = container.getEventBus();
		assert mod != null;
		mod.addListener(CCGKey::register);
		mod.addListener(ConfigResourcePackEvents::register);
		mod.addListener(TooltipOverlay::register);
		mod.addListener(TipOverlay::register);
		mod.addListener(ClientItemEntryTooltipComponent::register);
		mod.addListener(ClientItemListTooltipComponent::register);
		mod.addListener(ClientFluidEntryTooltipComponent::register);
		mod.addListener(ClientFluidListTooltipComponent::register);
		mod.addListener(ClientSchematicPreviewTooltipComponent::register);
		mod.addListener(DraftingShaders::register);
		CCGMods.simulated.executeIfInstalled(() -> mod.addListener(ForceTooltipOverlay::register));
		IEventBus game = NeoForge.EVENT_BUS;
		game.addListener(KeyInputEvents::key);
		game.addListener(KeyInputEvents::mouseScroll);
		game.addListener(PlayerInteractionEvents::leftClick);
		game.addListener(PlayerInteractionEvents::rightClick);
		game.addListener(PlayerInteractionEvents::tick);
		game.addListener(ItemTooltip::itemTooltip);
		game.addListener(ItemTooltip::gatherComponents);
		game.addListener(ItemTooltip::renderTooltipPre);
		game.addListener(KineticParticle::tick);
		game.addListener(KineticDebugger::tick);
		game.addListener(Outliner::tick);
		game.addListener(ChainConveyorFlowHandler::render);
		game.addListener(TipOverlay::tick);
		game.addListener(DraftingViewHandler::render);
		game.addListener(EnderChestTooltipUtil::clear);
		game.addListener(ImageActionProgress::onClientTick);
		game.addListener(EventPriority.LOWEST, ContraptionRotationEvents::onClientTick);
		game.addListener(SchematicPreviewInput::onMouseScrolled);
		game.addListener(EventPriority.HIGHEST, ReplenishScreen::onCharTyped);
		game.addListener(SchematicCommands::register);
		CCGMods.simulated.executeIfInstalled(() -> {
			game.addListener(ForceOverlay::tick);
			game.addListener(ForceOverlayRenderer::render);
		});
	}
}
