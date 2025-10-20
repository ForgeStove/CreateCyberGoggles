package io.github.forgestove.create_cyber_goggles;
import com.mojang.logging.LogUtils;
import io.github.forgestove.create_cyber_goggles.core.event.*;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
@Mod(CCG.ID)
public class CCG {
	public static final String ID = "create_cyber_goggles";
	public static final CCGConfig CONFIG = AutoConfig.register(CCGConfig.class, Toml4jConfigSerializer::new).getConfig();
	public static final Logger LOGGER = LogUtils.getLogger();
	public CCG() {
		if (FMLEnvironment.dist != Dist.CLIENT) return;
		CCGConfig.register();
		var mod = FMLJavaModLoadingContext.get().getModEventBus();
		mod.addListener(CCGKey::register);
		mod.addListener(Overlay::register);
		var game = MinecraftForge.EVENT_BUS;
		game.addListener(KeyInput::key);
		game.addListener(KeyInput::mouseScroll);
		game.addListener(ItemTooltip::itemTooltip);
		game.addListener(KineticParticle::tick);
		game.addListener(KineticDebugger::tick);
		game.addListener(Outliner::tick);
		game.addListener(PlayerInteract::tick);
		game.addListener(PlayerInteract::leftClick);
		game.addListener(PlayerInteract::rightClick);
	}
}
