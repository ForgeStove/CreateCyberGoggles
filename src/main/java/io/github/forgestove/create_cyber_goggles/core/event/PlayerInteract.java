package io.github.forgestove.create_cyber_goggles.core.event;
import com.zurrtum.create.content.equipment.wrench.*;
import com.zurrtum.create.content.fluids.pipes.EncasedPipeBlock;
import com.zurrtum.create.infrastructure.packet.c2s.RadialWrenchMenuSubmitPacket;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.client.Minecraft;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
public class PlayerInteract {
	private static long lastDismantleTime, dismantleDelay = 10;
	public static void leftClick(Minecraft mc) {
		if (dismantleDelay < 10) dismantleDelay++;
		if (mc == null || mc.player == null || mc.level == null) return;
		if (!CCG.CONFIG.wrench.leftClickFastDismantle) return;
		if (!mc.options.keyAttack.isDown()) return;
		if (mc.gameMode == null || mc.player.isCreative()) return;
		if (!(mc.hitResult instanceof BlockHitResult bhr)) return;
		InteractionHand handWithWrench = null;
		if (mc.player.getMainHandItem().getItem() instanceof WrenchItem) handWithWrench = InteractionHand.MAIN_HAND;
		else if (mc.player.getOffhandItem().getItem() instanceof WrenchItem) handWithWrench = InteractionHand.OFF_HAND;
		if (handWithWrench == null) return;
		var state = mc.level.getBlockState(bhr.getBlockPos());
		if (!(state.getBlock() instanceof IWrenchable)) return;
		if (dismantleDelay > 0) dismantleDelay--;
		var canDismantle = System.currentTimeMillis() - lastDismantleTime > dismantleDelay * 20L;
		if (!canDismantle) return;
		sendShift(true);
		mc.gameMode.useItemOn(mc.player, handWithWrench, bhr);
		sendShift(false);
		lastDismantleTime = System.currentTimeMillis();
	}
	public static @NotNull InteractionResult rightClick(
		Player player,
		@NotNull Level level,
		InteractionHand hand,
		BlockHitResult hitResult
	) {
		if (!level.isClientSide()) return InteractionResult.PASS;
		if (!CCG.CONFIG.wrench.betterEncasedPipe) return InteractionResult.PASS;
		if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
		if (!(hitResult instanceof BlockHitResult bhr)) return InteractionResult.PASS;
		var pos = bhr.getBlockPos();
		var state = level.getBlockState(pos);
		if (!(state.getBlock() instanceof EncasedPipeBlock)) return InteractionResult.PASS;
		var anyMatch = Stream.of(player.getMainHandItem(), player.getOffhandItem())
			.map(ItemStack::getItem)
			.anyMatch(item -> item instanceof BlockItem || item instanceof WrenchItem || item instanceof DebugStickItem);
		if (anyMatch) return InteractionResult.PASS;
		var face = bhr.getDirection();
		if (player.isShiftKeyDown()) face = face.getOpposite();
		var property = EncasedPipeBlock.FACING_TO_PROPERTY_MAP.get(face);
		if (property == null) return InteractionResult.PASS;
		boolean current = state.getValue(property);
		var newState = state.setValue(property, !current);
		sendToServer(new RadialWrenchMenuSubmitPacket(pos, newState));
		player.swing(hand);
		return InteractionResult.SUCCESS;
	}
}
