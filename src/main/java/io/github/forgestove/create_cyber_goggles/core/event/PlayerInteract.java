package io.github.forgestove.create_cyber_goggles.core.event;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.contraptions.chassis.*;
import com.simibubi.create.content.contraptions.wrench.RadialWrenchMenuSubmitPacket;
import com.simibubi.create.content.equipment.wrench.*;
import com.simibubi.create.content.fluids.pipes.EncasedPipeBlock;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogwheelBlock;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.CCGLang;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.*;

import java.util.ArrayList;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
import static net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock.Action.*;
public class PlayerInteract {
	private static long lastDismantleTime, dismantleDelay = 10;
	public static void tick(Post ignoredEvent) {
		wrench();
		encasedCogWheel();
		enacesdPipe();
		chassis();
	}
	public static void leftClick(LeftClickBlock event) {
		if (isServer()) return;
		wrench(event);
	}
	public static void rightClick(RightClickBlock event) {
		if (isServer()) return;
		enacesdPipe(event);
		encasedCogWheel(event);
		chassis(event);
	}
	private static void wrench() {
		if (dismantleDelay < 10) dismantleDelay++;
	}
	private static void wrench(LeftClickBlock event) {
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
	private static void encasedCogWheel() {
		if (!CCG.CONFIG.wrench.betterEncasedCogwheel) return;
		if (!(getBlock() instanceof EncasedCogwheelBlock ecb)) return;
		var bhr = getBlockHitResult();
		if (mc.level == null || bhr == null) return;
		if (ecb.getRotationAxis(mc.level.getBlockState(bhr.getBlockPos())) != bhr.getDirection().getAxis()) return;
		showCommonTip("message.openState");
	}
	private static void encasedCogWheel(RightClickBlock event) {
		if (!CCG.CONFIG.wrench.betterEncasedCogwheel) return;
		var pos = event.getPos();
		var state = event.getLevel().getBlockState(pos);
		if (!(state.getBlock() instanceof EncasedCogwheelBlock)
			|| event.getHand() != InteractionHand.MAIN_HAND
			|| mc.player == null
			|| hasItemInHand()) return;
		var clickedFace = event.getHitVec().getDirection();
		if (CCGKey.interactOpposite.isDown()) clickedFace = clickedFace.getOpposite();
		var axis = state.getValue(RotatedPillarBlock.AXIS);
		if (clickedFace.getAxis() != axis) return;
		var booleanProperty = clickedFace.getAxisDirection() == AxisDirection.POSITIVE
			? EncasedCogwheelBlock.TOP_SHAFT
			: EncasedCogwheelBlock.BOTTOM_SHAFT;
		sendToServer(new RadialWrenchMenuSubmitPacket(pos, state.cycle(booleanProperty)));
		mc.player.swing(mc.player.getUsedItemHand());
	}
	private static void enacesdPipe() {
		if (!CCG.CONFIG.wrench.betterEncasedPipe) return;
		if (!(getBlock() instanceof EncasedPipeBlock)) return;
		showCommonTip("message.openState");
	}
	private static void enacesdPipe(RightClickBlock event) {
		if (!CCG.CONFIG.wrench.betterEncasedPipe) return;
		var pos = event.getPos();
		var state = event.getLevel().getBlockState(pos);
		if (!(state.getBlock() instanceof EncasedPipeBlock)
			|| event.getHand() != InteractionHand.MAIN_HAND
			|| mc.player == null
			|| hasItemInHand()) return;
		var clickedFace = event.getHitVec().getDirection();
		if (CCGKey.interactOpposite.isDown()) clickedFace = clickedFace.getOpposite();
		var booleanProperty = EncasedPipeBlock.FACING_TO_PROPERTY_MAP.get(clickedFace);
		sendToServer(new RadialWrenchMenuSubmitPacket(pos, state.cycle(booleanProperty)));
		mc.player.swing(mc.player.getUsedItemHand());
	}
	private static void chassis() {
		if (!CCG.CONFIG.wrench.betterChassis) return;
		if (mc.level == null) return;
		if (!(getBlock() instanceof AbstractChassisBlock acb)) return;
		var bhr = getBlockHitResult();
		if (bhr == null || acb.getGlueableSide(mc.level.getBlockState(bhr.getBlockPos()), bhr.getDirection()) == null) return;
		showCommonTip("message.glueState");
	}
	private static void chassis(RightClickBlock event) {
		if (!CCG.CONFIG.wrench.betterChassis) return;
		if (hasActivedValueBox()) return;
		var pos = event.getPos();
		var state = event.getLevel().getBlockState(pos);
		if (!(state.getBlock() instanceof AbstractChassisBlock acb)) return;
		if (event.getHand() != InteractionHand.MAIN_HAND) return;
		if (mc.player == null) return;
		if (mc.player.isShiftKeyDown()) return;
		if (hasItemInHand()) return;
		var clickedFace = event.getHitVec().getDirection();
		var oppositeMode = CCGKey.interactOpposite.isDown();
		if (oppositeMode) {
			clickedFace = clickedFace.getOpposite();
			var level = event.getLevel();
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
		mc.player.swing(mc.player.getUsedItemHand());
	}
	private static void showCommonTip(String title) {
		if (hasActivedValueBox()) return;
		if (hasItemInHand()) return;
		var tip = new ArrayList<MutableComponent>();
		CCGLang.translate(title).addTo(tip);
		CCGLang.translate("message.useSwitchState", CCGKey.getColoredDisplayName(mc.options.keyUse)).addTo(tip);
		CCGLang.translate("message.pressToInteractOpposite", CCGKey.interactOpposite.getColoredDisplayName()).addTo(tip);
		CreateClient.VALUE_SETTINGS_HANDLER.showHoverTip(tip);
	}
}
