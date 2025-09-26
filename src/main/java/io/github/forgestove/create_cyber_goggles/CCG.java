package io.github.forgestove.create_cyber_goggles;
import io.github.forgestove.create_cyber_goggles.core.event.*;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
@Mod(CCG.ID)
public class CCG {
	public static final String ID = "create_cyber_goggles";
	public static final CCGConfig CONFIG = AutoConfig.register(CCGConfig.class, Toml4jConfigSerializer::new).getConfig();
	public CCG(ModContainer container, Dist dist) {
		if (dist != Dist.CLIENT) return;
		CCGConfig.register(container);
		var mod = container.getEventBus();
		if (mod == null) return;
		mod.addListener(CCGKey::register);
		mod.addListener(OverlayRenderer::registerLayer);
		var game = NeoForge.EVENT_BUS;
		game.addListener(KeyInput::tick);
		game.addListener(MouseScroll::onMouseScroll);
		game.addListener(KineticParticle::tick);
		game.addListener(KineticDebugger::tick);
		game.addListener(OutlineRenderer::tick);
		game.addListener(OverlayRenderer::tickColor);
	}
}
