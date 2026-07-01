package io.github.forgestove.create_cyber_goggles;
import com.mojang.logging.LogUtils;
import io.github.forgestove.create_cyber_goggles.config.Config;
import io.github.forgestove.create_cyber_goggles.config.client.*;
import io.github.forgestove.create_cyber_goggles.core.event.*;
import io.github.forgestove.create_cyber_goggles.core.event.drafting.*;
import io.github.forgestove.create_cyber_goggles.core.event.forceOverlay.*;
import io.github.forgestove.create_cyber_goggles.core.factory.*;
import io.github.forgestove.create_cyber_goggles.core.util.CCGMods;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
@Mod(CCG.ID)
public final class CCG {
	public static final String ID = "create_cyber_goggles";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final CCGConfig config = Config.getConfig(CCGConfig.class, LOGGER);
	public CCG(@NotNull ModContainer container) {
		if (FMLLoader.getDist().isClient()) clientInit(container);
	}
	public void clientInit(ModContainer container) {
		ConfigScreenFactory.initConfigScreen(container, ID);
		var mod = container.getEventBus();
		assert mod != null;
		// Mod event bus: client-only registrations
		mod.addListener(CCGKey::register);
		mod.addListener(TooltipOverlay::register);
		mod.addListener(TipOverlay::register);
		mod.addListener(ClientItemEntryTooltipComponent::register);
		mod.addListener(ClientItemListTooltipComponent::register);
		mod.addListener(ClientFluidEntryTooltipComponent::register);
		mod.addListener(ClientFluidListTooltipComponent::register);
		mod.addListener(DraftingShaders::register);
		CCGMods.SIMULATED.executeIfInstalled(() -> mod.addListener(ForceTooltipOverlay::register));
		// Game event bus: client-only registrations
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
		CCGMods.SIMULATED.executeIfInstalled(() -> {
			game.addListener(ForceOverlay::tick);
			game.addListener(ForceOverlayRenderer::onRenderStage);
		});
	}
}
