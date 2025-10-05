package io.github.forgestove.create_cyber_goggles;
import io.github.forgestove.create_cyber_goggles.core.event.*;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
@Mod(value = CCG.ID, dist = Dist.CLIENT)
public class CCG {
	public static final String ID = "create_cyber_goggles";
	public static final CCGConfig CONFIG = AutoConfig.register(CCGConfig.class, Toml4jConfigSerializer::new).getConfig();
	public CCG(ModContainer container) {
		CCGConfig.register(container);
		var mod = container.getEventBus();
		if (mod == null) return;
		mod.addListener(CCGKey::register);
		mod.addListener(OverlayRenderer::register);
		var game = NeoForge.EVENT_BUS;
		game.addListener(KeyInput::key);
		game.addListener(KeyInput::mouseScroll);
		game.addListener(OverlayRenderer::color);
		game.addListener(ItemTooltip::itemTooltip);
		game.addListener(KineticParticle::tick);
		game.addListener(KineticDebugger::tick);
		game.addListener(OutlineRenderer::tick);
		game.addListener(PlayerInteract::tick);
		game.addListener(PlayerInteract::leftClick);
		game.addListener(PlayerInteract::rightClick);
	}
}
