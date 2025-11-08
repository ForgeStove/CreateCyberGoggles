package io.github.forgestove.create_cyber_goggles.core.event;
import com.zurrtum.create.content.contraptions.chassis.*;
import com.zurrtum.create.content.equipment.wrench.*;
import com.zurrtum.create.content.fluids.pipes.EncasedPipeBlock;
import com.zurrtum.create.content.kinetics.simpleRelays.encased.EncasedCogwheelBlock;
import com.zurrtum.create.content.logistics.tableCloth.TableClothBlockEntity;
import com.zurrtum.create.infrastructure.packet.c2s.RadialWrenchMenuSubmitPacket;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
public class PlayerInteract {
	private static long lastDismantleTime, dismantleDelay = 10;
	private static long lastTick;
	public static void leftClick(Minecraft mc) {
		wrench(mc);
		tick(mc);
	}
	private static void tick(Minecraft mc) {
		if (mc == null || mc.level == null) return;
		encasedCogWheel();
		enacesdPipe();
		chassis();
		tableCloth();
	}
	public static @NotNull InteractionResult rightClick(
		Player player,
		@NotNull Level level,
		InteractionHand hand,
		BlockHitResult hitResult
	) {
		if (!level.isClientSide()) return InteractionResult.PASS;
		enacesdPipe(player, level, hand, hitResult);
		encasedCogWheel(player, level, hand, hitResult);
		chassis(player, level, hand, hitResult);
		tableCloth();
		return InteractionResult.SUCCESS;
	}
	private static void wrench(Minecraft mc) {
		if (dismantleDelay < 10) dismantleDelay++;
		if (!CCG.CONFIG.wrench.leftClickFastDismantle) return;
		if (mc == null || mc.player == null || mc.level == null) return;
		if (!mc.options.keyAttack.isDown()) return;
		if (mc.gameMode == null || mc.player.isCreative()) return;
		if (!(mc.hitResult instanceof BlockHitResult bhr)) return;
		var handWithWrench = mc.player.getMainHandItem().getItem() instanceof WrenchItem
			? InteractionHand.MAIN_HAND
			: mc.player.getOffhandItem().getItem() instanceof WrenchItem ? InteractionHand.OFF_HAND : null;
		if (handWithWrench == null) return;
		if (!(mc.level.getBlockState(bhr.getBlockPos()).getBlock() instanceof IWrenchable)) return;
		if (dismantleDelay > 0) dismantleDelay--;
		var canDismantle = System.currentTimeMillis() - lastDismantleTime > dismantleDelay * 20;
		if (!canDismantle) return;
		sendShift(true);
		mc.gameMode.useItemOn(mc.player, handWithWrench, bhr);
		sendShift(false);
		lastDismantleTime = System.currentTimeMillis();
	}
	private static void encasedCogWheel() {
		if (!CCG.CONFIG.wrench.betterEncasedCogwheel) return;
		var ecb = getBlock(EncasedCogwheelBlock.class);
		if (ecb == null) return;
		var bhr = getBlockHitResult();
		if (mc.level == null || bhr == null) return;
		if (ecb.getRotationAxis(mc.level.getBlockState(bhr.getBlockPos())) != bhr.getDirection().getAxis()) return;
		showCommonTip("message.openState");
	}
	private static void encasedCogWheel(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
		if (!CCG.CONFIG.wrench.betterEncasedCogwheel) return;
		var pos = hitResult.getBlockPos();
		var state = level.getBlockState(pos);
		if (!(state.getBlock() instanceof EncasedCogwheelBlock) || hand != InteractionHand.MAIN_HAND || player == null || hasItemInHand())
			return;
		var clickedFace = hitResult.getDirection();
		if (CCGKey.interactOpposite.isDown()) clickedFace = clickedFace.getOpposite();
		var axis = state.getValue(RotatedPillarBlock.AXIS);
		if (clickedFace.getAxis() != axis) return;
		var booleanProperty = clickedFace.getAxisDirection() == AxisDirection.POSITIVE
			? EncasedCogwheelBlock.TOP_SHAFT
			: EncasedCogwheelBlock.BOTTOM_SHAFT;
		sendToServer(new RadialWrenchMenuSubmitPacket(pos, state.cycle(booleanProperty)));
		player.swing(player.getUsedItemHand());
	}
	private static void enacesdPipe() {
		if (!CCG.CONFIG.wrench.betterEncasedPipe) return;
		if (getBlock(EncasedPipeBlock.class) == null) return;
		showCommonTip("message.openState");
	}
	private static void enacesdPipe(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
		if (!CCG.CONFIG.wrench.betterEncasedPipe) return;
		var pos = hitResult.getBlockPos();
		var state = level.getBlockState(pos);
		if (!(state.getBlock() instanceof EncasedPipeBlock) || hand != InteractionHand.MAIN_HAND || player == null || hasItemInHand())
			return;
		var clickedFace = hitResult.getDirection();
		if (CCGKey.interactOpposite.isDown()) clickedFace = clickedFace.getOpposite();
		var booleanProperty = EncasedPipeBlock.FACING_TO_PROPERTY_MAP.get(clickedFace);
		sendToServer(new RadialWrenchMenuSubmitPacket(pos, state.cycle(booleanProperty)));
		player.swing(player.getUsedItemHand());
	}
	private static void chassis() {
		if (!CCG.CONFIG.wrench.betterChassis) return;
		if (mc.level == null) return;
		var acb = getBlock(AbstractChassisBlock.class);
		if (acb == null) return;
		var bhr = getBlockHitResult();
		if (bhr == null || acb.getGlueableSide(mc.level.getBlockState(bhr.getBlockPos()), bhr.getDirection()) == null) return;
		showCommonTip("message.glueState");
	}
	private static void chassis(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
		if (!CCG.CONFIG.wrench.betterChassis) return;
		if (hasActivedValueBox()) return;
		var pos = hitResult.getBlockPos();
		var state = level.getBlockState(pos);
		if (!(state.getBlock() instanceof AbstractChassisBlock acb)) return;
		if (hand != InteractionHand.MAIN_HAND) return;
		if (player == null) return;
		if (player.isShiftKeyDown()) return;
		if (hasItemInHand()) return;
		var clickedFace = hitResult.getDirection();
		if (CCGKey.interactOpposite.isDown()) {
			clickedFace = clickedFace.getOpposite();
			var axisProp = RotatedPillarBlock.AXIS;
			var baseAxis = state.hasProperty(axisProp) ? state.getValue(axisProp) : null;
			var probePos = pos.relative(clickedFace);
			var probeState = level.getBlockState(probePos);
			var targetAcb = acb;
			if (LinearChassisBlock.isChassis(state) && LinearChassisBlock.sameKind(state, probeState))
				while (probeState.getBlock() instanceof LinearChassisBlock lcb) {
					var probeAxis = probeState.hasProperty(axisProp) ? probeState.getValue(axisProp) : null;
					if (probeAxis == null || probeAxis != baseAxis) break;
					pos = probePos;
					state = probeState;
					targetAcb = lcb;
					probePos = probePos.relative(clickedFace);
					probeState = level.getBlockState(probePos);
				}
			acb = targetAcb;
		}
		var booleanProperty = acb.getGlueableSide(state, clickedFace);
		if (booleanProperty == null) return;
		sendToServer(new RadialWrenchMenuSubmitPacket(pos, state.cycle(booleanProperty)));
		player.swing(player.getUsedItemHand());
	}
	public static void tableCloth() {
		if (!CCG.CONFIG.goggles.betterStoreInfo) return;
		if (mc.player == null) return;
		var currentTick = mc.player.tickCount;
		if (currentTick == lastTick) return;
		lastTick = currentTick;
		var tcbe = getBlockEntity(TableClothBlockEntity.class);
		if (tcbe == null) return;
		if (TableClothUtil.getItems(tcbe).size() <= 1) return;
		var builder = CCGLang.translate("message.toggleItemOverlay", CCGKey.toggleItemOverlay.getFancyName()).style(ChatFormatting.WHITE);
		TipOverlay.show(List.of(builder.component()), 0, 25);
	}
	public static void showCommonTip(String title) {
		if (hasItemInHand()) return;
		var tip = new ArrayList<MutableComponent>();
		CCGLang.translate(title).addTo(tip);
		CCGLang.translate("message.useSwitchState", CCGKey.getFancyName(mc.options.keyUse)).addTo(tip);
		CCGLang.translate("message.pressToInteractOpposite", CCGKey.interactOpposite.getFancyName()).addTo(tip);
		TipOverlay.show(tip);
	}
}
