package io.github.forgestove.create_cyber_goggles;
import com.mojang.logging.LogUtils;
import io.github.forgestove.create_cyber_goggles.config.Config;
import io.github.forgestove.create_cyber_goggles.core.event.*;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
@Mod(CCG.ID)
public final class CCG {
	public static final String ID = "create_cyber_goggles";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static CCGConfig config = Config.getConfig(CCGConfig.class, ID, LOGGER);
	public CCG(FMLJavaModLoadingContext context) {
		if (FMLEnvironment.dist != Dist.CLIENT) return;
		var container = context.getContainer();
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
		var game = MinecraftForge.EVENT_BUS;
		game.addListener(KeyInput::key);
		game.addListener(KeyInput::mouseScroll);
		game.addListener(PlayerInteract::leftClick);
		game.addListener(PlayerInteract::rightClick);
		game.addListener(PlayerInteract::tick);
		game.addListener(ItemTooltip::itemTooltip);
		game.addListener(ItemTooltip::gatherComponents);
		game.addListener(KineticParticle::tick);
		game.addListener(KineticDebugger::tick);
		game.addListener(Outliner::tick);
		game.addListener(TipOverlay::tick);
	}
}
