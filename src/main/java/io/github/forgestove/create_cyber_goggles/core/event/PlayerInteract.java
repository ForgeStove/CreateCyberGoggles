package io.github.forgestove.create_cyber_goggles.core.event;
import com.simibubi.create.content.equipment.wrench.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import org.jetbrains.annotations.NotNull;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
import static net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock.Action.*;
public class PlayerInteract {
	private static long lastDismantleTime;
	private static long dismantleDelay = 10;
	public static void tick(ClientTickEvent ignoredEvent) {
		if (dismantleDelay < 10) dismantleDelay++;
	}
	public static void leftClick(@NotNull LeftClickBlock event) {
		if (!CCG.CONFIG.wrench.leftClickFastDismantle) return;
		if (dismantleDelay > 0) dismantleDelay--;
		var canDismantle = System.currentTimeMillis() - lastDismantleTime > dismantleDelay * 20;
		if (!canDismantle) return;
		var action = event.getAction();
		if (!(action == START || action == CLIENT_HOLD)) return;
		var player = mc.player;
		if (player == null || mc.player.isCreative() || mc.gameMode == null) return;
		var handWithWrench = player.getMainHandItem().getItem() instanceof WrenchItem
			? InteractionHand.MAIN_HAND
			: player.getOffhandItem().getItem() instanceof WrenchItem ? InteractionHand.OFF_HAND : null;
		if (handWithWrench == null) return;
		if (!(getBlock() instanceof IWrenchable)) return;
		var result = getBlockHitResult();
		if (result == null) return;
		sendAction(Action.PRESS_SHIFT_KEY);
		mc.gameMode.useItemOn(player, handWithWrench, result);
		sendAction(Action.RELEASE_SHIFT_KEY);
		lastDismantleTime = System.currentTimeMillis();
		event.setCanceled(true);
	}
}
