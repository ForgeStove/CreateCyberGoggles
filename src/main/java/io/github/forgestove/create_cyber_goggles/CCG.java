package io.github.forgestove.create_cyber_goggles;
import com.mojang.logging.LogUtils;
import io.github.forgestove.create_cyber_goggles.config.Config;
import io.github.forgestove.create_cyber_goggles.core.event.*;
import io.github.forgestove.create_cyber_goggles.core.factory.*;
import io.github.forgestove.create_cyber_goggles.core.overlay.*;
import io.github.forgestove.create_cyber_goggles.core.overlay.drafting.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
@Mod(value = CCG.ID, dist = Dist.CLIENT)
public final class CCG {
	public static final String ID = "create_cyber_goggles";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final CCGConfig config = Config.getConfig(CCGConfig.class, LOGGER);
	public CCG(@NotNull ModContainer container) {
		Config.initConfigScreen(container, ID);
		var mod = container.getEventBus();
		if (mod == null) return;
		mod.addListener(CCGKey::register);
		mod.addListener(TooltipOverlay::register);
		mod.addListener(TipOverlay::register);
		mod.addListener(ClientItemEntryTooltipComponent::register);
		mod.addListener(ClientItemListTooltipComponent::register);
		mod.addListener(ClientFluidEntryTooltipComponent::register);
		mod.addListener(ClientFluidListTooltipComponent::register);
		mod.addListener(ForceTooltipOverlay::register);
		mod.addListener(DraftingShaders::onRegisterShaders);
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
		game.addListener(ForceOverlayClient::tick);
		game.addListener(ForceOverlayRenderer::onRenderStage);
		game.addListener(DraftingViewHandler::applyIfEnabled);
	}
}
