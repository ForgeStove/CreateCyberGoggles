package io.github.forgestove.create_cyber_goggles.core.event;
import com.simibubi.create.content.equipment.wrench.WrenchItem;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import org.jetbrains.annotations.NotNull;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
import static net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock.Action.*;
public class PlayerInteract {
	public static void tick(@NotNull LeftClickBlock event) {
		if (!CCG.CONFIG.wrench.leftClickFastDismantle) return;
		var action = event.getAction();
		if (!action.equals(START) && !action.equals(CLIENT_HOLD)) return;
		var itemStack = event.getItemStack();
		if (!(itemStack.getItem() instanceof WrenchItem)) return;
		var player = mc.player;
		if (player == null || mc.gameMode == null) return;
		var result = getBlockHitResult();
		if (result == null) return;
		player.connection.send(new ServerboundPlayerCommandPacket(player, Action.PRESS_SHIFT_KEY));
		mc.gameMode.useItemOn(player, player.getUsedItemHand(), result);
		player.connection.send(new ServerboundPlayerCommandPacket(player, Action.RELEASE_SHIFT_KEY));
	}
}
