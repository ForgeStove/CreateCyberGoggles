package io.github.forgestove.create_cyber_goggles.core.event;
import com.simibubi.create.content.contraptions.wrench.RadialWrenchMenuSubmitPacket;
import com.simibubi.create.content.equipment.wrench.*;
import com.simibubi.create.content.fluids.pipes.EncasedPipeBlock;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.*;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
import static net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock.Action.*;
public class PlayerInteract {
	private static long lastDismantleTime;
	private static long dismantleDelay = 10;
	public static void tick(Post ignoredEvent) {
		if (dismantleDelay < 10) dismantleDelay++;
	}
	public static void leftClick(@NotNull LeftClickBlock event) {
		if (!isClient()) return;
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
	public static void rightClick(RightClickBlock event) {
		if (!isClient()) return;
		if (!CCG.CONFIG.wrench.betterEncasedPipe) return;
		if (event.getHand() != InteractionHand.MAIN_HAND) return;
		if (mc.player == null) return;
		var anyMatch = Stream.of(mc.player.getMainHandItem(), mc.player.getOffhandItem())
			.map(ItemStack::getItem)
			.anyMatch(item -> item instanceof BlockItem || item instanceof WrenchItem || item instanceof DebugStickItem);
		if (anyMatch) return;
		var pos = event.getPos();
		var state = event.getLevel().getBlockState(pos);
		if (!(state.getBlock() instanceof EncasedPipeBlock)) return;
		var clickedFace = event.getHitVec().getDirection();
		if (mc.player.isShiftKeyDown()) clickedFace = clickedFace.getOpposite();
		var property = EncasedPipeBlock.FACING_TO_PROPERTY_MAP.get(clickedFace);
		boolean currentState = state.getValue(property);
		var newState = state.setValue(property, !currentState);
		CatnipServices.NETWORK.sendToServer(new RadialWrenchMenuSubmitPacket(pos, newState));
		mc.player.swing(mc.player.getUsedItemHand());
	}
}
