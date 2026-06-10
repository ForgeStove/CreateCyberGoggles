package io.github.forgestove.create_cyber_goggles;
import com.mojang.logging.LogUtils;
import io.github.forgestove.create_cyber_goggles.config.Config;
import io.github.forgestove.create_cyber_goggles.core.event.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.KeyMapping.Category;
import org.slf4j.Logger;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.getCCGRes;
public class CCG implements ClientModInitializer {
	public static final String ID = "create_cyber_goggles";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final Category CATEGORY = Category.register(getCCGRes("main"));
	public static final CCGConfig config = Config.getConfig(CCGConfig.class, LOGGER);
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
		// Custom tooltip components are handled via ClientTooltipComponentMixin
	}
}
