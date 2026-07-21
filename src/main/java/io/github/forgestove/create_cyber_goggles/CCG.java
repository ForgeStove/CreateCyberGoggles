package io.github.forgestove.create_cyber_goggles;
import com.mojang.logging.LogUtils;
import io.github.forgestove.config.ConfigRegistry;
import io.github.forgestove.config.client.ConfigScreenFactory;
import io.github.forgestove.create_cyber_goggles.core.event.*;
import io.github.forgestove.create_cyber_goggles.core.event.drafting.*;
import io.github.forgestove.create_cyber_goggles.core.event.forceOverlay.*;
import io.github.forgestove.create_cyber_goggles.core.factory.*;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
@Mod(value = CCG.ID, dist = Dist.CLIENT)
public final class CCG {
	public static final String ID = "create_cyber_goggles";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final CCGConfig config = ConfigRegistry.init(CCGConfig.class);
	public CCG(ModContainer container) {
		ConfigScreenFactory.initConfigScreen(container, ID);
		var mod = container.getEventBus();
		assert mod != null;
		mod.addListener(CCGKey::register);
		mod.addListener(TooltipOverlay::register);
		mod.addListener(TipOverlay::register);
		mod.addListener(ClientItemEntryTooltipComponent::register);
		mod.addListener(ClientItemListTooltipComponent::register);
		mod.addListener(ClientFluidEntryTooltipComponent::register);
		mod.addListener(ClientFluidListTooltipComponent::register);
		mod.addListener(DraftingShaders::register);
		CCGMods.SIMULATED.executeIfInstalled(() -> mod.addListener(ForceTooltipOverlay::register));
		var game = NeoForge.EVENT_BUS;
		game.addListener(KeyInput::key);
		game.addListener(KeyInput::mouseScroll);
		game.addListener(PlayerInteract::leftClick);
		game.addListener(PlayerInteract::rightClick);
		game.addListener(PlayerInteract::tick);
		game.addListener(ItemTooltip::itemTooltip);
		game.addListener(ItemTooltip::gatherComponents);
		game.addListener(ItemTooltip::renderTooltipPre);
		game.addListener(KineticParticle::tick);
		game.addListener(KineticDebugger::tick);
		game.addListener(Outliner::tick);
		game.addListener(TipOverlay::tick);
		game.addListener(DraftingViewHandler::applyIfEnabled);
		game.addListener(EnderChestTooltipUtil::clear);
		CCGMods.SIMULATED.executeIfInstalled(() -> {
			game.addListener(ForceOverlay::tick);
			game.addListener(ForceOverlayRenderer::onRenderStage);
		});
	}
}
