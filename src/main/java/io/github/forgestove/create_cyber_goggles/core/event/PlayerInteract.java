package io.github.forgestove.create_cyber_goggles.core.event;
import com.simibubi.create.content.equipment.wrench.WrenchItem;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import org.jetbrains.annotations.NotNull;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
public class PlayerInteract {
	public static void tick(@NotNull LeftClickBlock event) {
		if (!CCG.CONFIG.wrench.leftClickFastDismantle) return;
		var action = event.getAction();
		if (!action.equals(LeftClickBlock.Action.START) && !action.equals(LeftClickBlock.Action.CLIENT_HOLD)) return;
		var itemStack = event.getItemStack();
		if (!(itemStack.getItem() instanceof WrenchItem)) return;
		if (mc.player == null || mc.player.isCreative() || mc.gameMode == null) return;
		var result = getBlockHitResult();
		if (result == null) return;
		sendAction(Action.PRESS_SHIFT_KEY);
		mc.gameMode.useItemOn(mc.player, mc.player.getUsedItemHand(), result);
		sendAction(Action.RELEASE_SHIFT_KEY);
	}
}
