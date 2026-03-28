package io.github.forgestove.create_cyber_goggles;
import com.mojang.logging.LogUtils;
import io.github.forgestove.create_cyber_goggles.config.Config;
import io.github.forgestove.create_cyber_goggles.core.event.*;
import io.github.forgestove.create_cyber_goggles.core.factory.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.fabricmc.fabric.api.event.player.*;
import org.slf4j.Logger;
public final class CCG implements ClientModInitializer {
	public static final String ID = "create_cyber_goggles";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static CCGConfig config = Config.getConfig(CCGConfig.class, ID, LOGGER);
	@Override
	public void onInitializeClient() {
		CCGKey.register();
		ClientTickEvents.END_CLIENT_TICK.register(KeyInput::key);
		ClientTickEvents.END_CLIENT_TICK.register(TipOverlay::tick);
		ClientTickEvents.END_CLIENT_TICK.register(PlayerInteract::tick);
		ClientTickEvents.END_CLIENT_TICK.register(KineticParticle::tick);
		ClientTickEvents.END_CLIENT_TICK.register(Outliner::tick);
		WorldRenderEvents.AFTER_TRANSLUCENT.register(KineticDebugger::tick);
		HudRenderCallback.EVENT.register(TooltipOverlay::renderOverlay);
		HudRenderCallback.EVENT.register(TipOverlay::renderOverlay);
		ItemTooltipCallback.EVENT.register((stack, flag, lines) -> ItemTooltip.itemTooltip(stack, lines));
		UseBlockCallback.EVENT.register(PlayerInteract::rightClick);
		AttackBlockCallback.EVENT.register(PlayerInteract::leftClick);
		ClientItemEntryTooltipComponent.register();
		ClientItemListTooltipComponent.register();
		ClientFluidEntryTooltipComponent.register();
		ClientFluidListTooltipComponent.register();
	}
}
