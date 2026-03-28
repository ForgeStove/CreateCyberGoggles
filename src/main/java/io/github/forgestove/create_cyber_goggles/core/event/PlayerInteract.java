package io.github.forgestove.create_cyber_goggles.core.event;
import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.content.contraptions.chassis.*;
import com.simibubi.create.content.contraptions.wrench.RadialWrenchMenuSubmitPacket;
import com.simibubi.create.content.equipment.wrench.*;
import com.simibubi.create.content.fluids.pipes.EncasedPipeBlock;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogwheelBlock;
import com.simibubi.create.content.logistics.tableCloth.TableClothBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.*;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.phys.BlockHitResult;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
public class PlayerInteract {
	private static long lastDismantleTime, dismantleDelay = 10;
	private static long lastTick;
	public static void tick(Minecraft ignoredMc) {
		wrench();
		encasedCogWheel();
		enacesdPipe();
		chassis();
		tableCloth();
	}
	@SuppressWarnings("unused")
	public static InteractionResult leftClick(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction) {
		if (world.isClientSide) wrench(player, world, pos);
		return InteractionResult.PASS;
	}
	public static InteractionResult rightClick(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
		if (!world.isClientSide) return InteractionResult.PASS;
		if (enacesdPipe(player, world, hand, hitResult)) return InteractionResult.SUCCESS;
		if (encasedCogWheel(player, world, hand, hitResult)) return InteractionResult.SUCCESS;
		if (chassis(player, world, hand, hitResult)) return InteractionResult.SUCCESS;
		return InteractionResult.PASS;
	}
	private static void wrench() {
		if (dismantleDelay < 10) dismantleDelay++;
	}
	@SuppressWarnings("unused")
	private static void wrench(Player player, Level world, BlockPos pos) {
		if (!CCG.config.wrench.leftClickFastDismantle) return;
		if (dismantleDelay > 0) dismantleDelay--;
		var canDismantle = System.currentTimeMillis() - lastDismantleTime > dismantleDelay * 20;
		if (!canDismantle) return;
		if (player == null || player.isCreative() || mc.gameMode == null) return;
		if (!(player instanceof LocalPlayer localPlayer)) return;
		var handWithWrench = player.getMainHandItem().getItem() instanceof WrenchItem
			? InteractionHand.MAIN_HAND
			: player.getOffhandItem().getItem() instanceof WrenchItem ? InteractionHand.OFF_HAND : null;
		if (handWithWrench == null) return;
		var state = world.getBlockState(pos);
		var block = state.getBlock();
		if (!(block instanceof IWrenchable || AllBlockTags.WRENCH_PICKUP.matches(state))) return;
		var result = getBlockHitResult();
		if (result == null || !result.getBlockPos().equals(pos)) return;
		sendAction(Action.PRESS_SHIFT_KEY);
		mc.gameMode.useItemOn(localPlayer, handWithWrench, result);
		sendAction(Action.RELEASE_SHIFT_KEY);
		lastDismantleTime = System.currentTimeMillis();
	}
	private static void encasedCogWheel() {
		if (!CCG.config.wrench.betterEncasedCogwheel) return;
		var ecb = getBlock(EncasedCogwheelBlock.class);
		if (ecb == null) return;
		var bhr = getBlockHitResult();
		if (mc.level == null || bhr == null) return;
		if (ecb.getRotationAxis(mc.level.getBlockState(bhr.getBlockPos())) != bhr.getDirection().getAxis()) return;
		showCommonTip("message.openState");
	}
	private static boolean encasedCogWheel(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
		if (!CCG.config.wrench.betterEncasedCogwheel) return false;
		var pos = hitResult.getBlockPos();
		var state = world.getBlockState(pos);
		if (!(state.getBlock() instanceof EncasedCogwheelBlock) || hand != InteractionHand.MAIN_HAND || player == null || hasItemInHand())
			return false;
		var clickedFace = hitResult.getDirection();
		if (CCGKey.interactOpposite.isDown()) clickedFace = clickedFace.getOpposite();
		var axis = state.getValue(RotatedPillarBlock.AXIS);
		if (clickedFace.getAxis() != axis) return false;
		var booleanProperty = clickedFace.getAxisDirection() == AxisDirection.POSITIVE
			? EncasedCogwheelBlock.TOP_SHAFT
			: EncasedCogwheelBlock.BOTTOM_SHAFT;
		sendToServer(new RadialWrenchMenuSubmitPacket(pos, state.cycle(booleanProperty)));
		player.swing(hand);
		return true;
	}
	private static void enacesdPipe() {
		if (!CCG.config.wrench.betterEncasedPipe) return;
		if (getBlock(EncasedPipeBlock.class) == null) return;
		showCommonTip("message.openState");
	}
	private static boolean enacesdPipe(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
		if (!CCG.config.wrench.betterEncasedPipe) return false;
		var pos = hitResult.getBlockPos();
		var state = world.getBlockState(pos);
		if (!(state.getBlock() instanceof EncasedPipeBlock) || hand != InteractionHand.MAIN_HAND || player == null || hasItemInHand())
			return false;
		var clickedFace = hitResult.getDirection();
		if (CCGKey.interactOpposite.isDown()) clickedFace = clickedFace.getOpposite();
		var booleanProperty = EncasedPipeBlock.FACING_TO_PROPERTY_MAP.get(clickedFace);
		if (booleanProperty == null) return false;
		sendToServer(new RadialWrenchMenuSubmitPacket(pos, state.cycle(booleanProperty)));
		player.swing(hand);
		return true;
	}
	private static void chassis() {
		if (!CCG.config.wrench.betterChassis) return;
		if (mc.level == null) return;
		var acb = getBlock(AbstractChassisBlock.class);
		if (acb == null) return;
		var bhr = getBlockHitResult();
		if (bhr == null || acb.getGlueableSide(mc.level.getBlockState(bhr.getBlockPos()), bhr.getDirection()) == null) return;
		showCommonTip("message.glueState");
	}
	private static boolean chassis(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
		if (!CCG.config.wrench.betterChassis) return false;
		if (hasActivedValueBox()) return false;
		if (hand != InteractionHand.MAIN_HAND) return false;
		if (player == null) return false;
		if (player.isShiftKeyDown()) return false;
		if (hasItemInHand()) return false;
		var pos = hitResult.getBlockPos();
		var state = level.getBlockState(pos);
		if (!(state.getBlock() instanceof AbstractChassisBlock acb)) return false;
		var targetPos = pos;
		var targetState = state;
		var targetAcb = acb;
		var clickedFace = hitResult.getDirection();
		if (CCGKey.interactOpposite.isDown()) {
			clickedFace = clickedFace.getOpposite();
			var axisProp = RotatedPillarBlock.AXIS;
			var baseAxis = targetState.hasProperty(axisProp) ? targetState.getValue(axisProp) : null;
			var probePos = targetPos.relative(clickedFace);
			var probeState = level.getBlockState(probePos);
			if (LinearChassisBlock.isChassis(targetState) && LinearChassisBlock.sameKind(targetState, probeState))
				while (probeState.getBlock() instanceof LinearChassisBlock lcb) {
					var probeAxis = probeState.hasProperty(axisProp) ? probeState.getValue(axisProp) : null;
					if (probeAxis == null || probeAxis != baseAxis) break;
					targetPos = probePos;
					targetState = probeState;
					targetAcb = lcb;
					probePos = probePos.relative(clickedFace);
					probeState = level.getBlockState(probePos);
				}
		}
		var booleanProperty = targetAcb.getGlueableSide(targetState, clickedFace);
		if (booleanProperty == null) return false;
		sendToServer(new RadialWrenchMenuSubmitPacket(targetPos, targetState.cycle(booleanProperty)));
		player.swing(hand);
		return true;
	}
	public static void tableCloth() {
		if (!CCG.config.goggles.betterStoreInfo) return;
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
