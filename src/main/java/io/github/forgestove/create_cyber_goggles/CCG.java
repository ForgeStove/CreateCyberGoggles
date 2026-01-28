package io.github.forgestove.create_cyber_goggles;
import com.mojang.logging.LogUtils;
import io.github.forgestove.create_cyber_goggles.core.event.*;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import org.slf4j.Logger;
public class CCG implements ClientModInitializer {
	public static final String ID = "create_cyber_goggles";
	public static final CCGConfig CONFIG = AutoConfig.register(CCGConfig.class, Toml4jConfigSerializer::new).getConfig();
	public static final Logger LOGGER = LogUtils.getLogger();
	@SuppressWarnings("deprecation")
	@Override
	public void onInitializeClient() {
		CCGKey.register();
		ClientTickEvents.START_CLIENT_TICK.register(KeyInput::register);
		ClientTickEvents.START_CLIENT_TICK.register(KineticParticle::tick);
		ClientTickEvents.START_CLIENT_TICK.register(Outliner::tick);
		ClientTickEvents.START_CLIENT_TICK.register(PlayerInteract::leftClick);
		ClientTickEvents.START_CLIENT_TICK.register(KineticDebugger::tick);
		ClientTickEvents.END_CLIENT_TICK.register(TipOverlay::tick);
		HudRenderCallback.EVENT.register(TooltipOverlay::renderOverlay);
		HudRenderCallback.EVENT.register(TipOverlay::renderOverlay);
		UseBlockCallback.EVENT.register(PlayerInteract::rightClick);
		ItemTooltipCallback.EVENT.register(ItemTooltip::itemTooltip);
	}
}
