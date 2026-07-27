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
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.network.chat.*;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.neoforged.neoforge.client.event.ClientTickEvent.Pre;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.*;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
import static net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.LeftClickBlock.Action.*;
public final class PlayerInteract {
	private static long lastDismantleTime, dismantleDelay = 10;
	private static long lastTick;
	private static boolean isShiftKeyDown;
	public static void tick(Pre ignoredEvent) {
		wrench();
		encasedCogWheel();
		enacesdPipe();
		chassis();
		tableCloth();
		ItemSwapUtil.tick();
	}
	private static void wrench() {
		if (dismantleDelay < 10) dismantleDelay++;
	}
	private static void encasedCogWheel() {
		if (!CCG.config.misc.wrench.betterEncasedCogwheel) return;
		var ecb = getBlock(EncasedCogwheelBlock.class);
		if (ecb == null) return;
		var bhr = getBlockHitResult();
		if (mc.level == null || bhr == null) return;
		if (ecb.getRotationAxis(mc.level.getBlockState(bhr.getBlockPos())) != bhr.getDirection().getAxis()) return;
		showCommonTip(Component.translatable("create_cyber_goggles.message.openState"));
	}
	private static void enacesdPipe() {
		if (!CCG.config.misc.wrench.betterEncasedPipe) return;
		if (getBlock(EncasedPipeBlock.class) == null) return;
		showCommonTip(Component.translatable("create_cyber_goggles.message.openState"));
	}
	private static void chassis() {
		if (!CCG.config.misc.wrench.betterChassis) return;
		if (mc.level == null) return;
		var acb = getBlock(AbstractChassisBlock.class);
		if (acb == null) return;
		var bhr = getBlockHitResult();
		if (bhr == null || acb.getGlueableSide(mc.level.getBlockState(bhr.getBlockPos()), bhr.getDirection()) == null) return;
		showCommonTip(Component.translatable("create_cyber_goggles.message.glueState"));
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
		var builder = CCGLang.add(Component.translatable(
			"create_cyber_goggles.message.toggleItemOverlay",
			CCGKey.toggleItemOverlay.getFancyName()
		)).style(ChatFormatting.WHITE);
		TipOverlay.show(List.of(builder.component()), 0, 25);
	}
	public static void showCommonTip(Component title) {
		if (hasItemInHand()) return;
		var tip = new ArrayList<MutableComponent>();
		CCGLang.add(title).addTo(tip);
		CCGLang.add(Component.translatable("create_cyber_goggles.message.useSwitchState", CCGKey.getFancyName(mc.options.keyUse)))
			.addTo(tip);
		CCGLang.add(Component.translatable("create_cyber_goggles.message.pressToInteractOpposite", CCGKey.interactOpposite.getFancyName()))
			.addTo(tip);
		TipOverlay.show(tip);
	}
	public static void leftClick(LeftClickBlock event) {
		if (isServer()) return;
		wrench(event);
	}
	private static void wrench(LeftClickBlock event) {
		if (!CCG.config.misc.wrench.leftClickFastDismantle) return;
		if (dismantleDelay > 0) dismantleDelay--;
		var canDismantle = System.currentTimeMillis() - lastDismantleTime > dismantleDelay * 20;
		if (!canDismantle) return;
		var action = event.getAction();
		if (!(action == START || action == CLIENT_HOLD)) return;
		var player = mc.player;
		if (player == null || player.isCreative() || mc.gameMode == null) return;
		var handWithWrench = player.getMainHandItem().getItem() instanceof WrenchItem
			? InteractionHand.MAIN_HAND
			: player.getOffhandItem().getItem() instanceof WrenchItem ? InteractionHand.OFF_HAND : null;
		if (handWithWrench == null) return;
		var pos = event.getPos();
		var state = event.getLevel().getBlockState(pos);
		var block = state.getBlock();
		if (!(block instanceof IWrenchable || AllBlockTags.WRENCH_PICKUP.matches(state))) return;
		var result = getBlockHitResult();
		if (result == null) return;
		sendAction(Action.PRESS_SHIFT_KEY);
		isShiftKeyDown = true;
		mc.gameMode.useItemOn(player, handWithWrench, result);
		sendAction(Action.RELEASE_SHIFT_KEY);
		isShiftKeyDown = false;
		lastDismantleTime = System.currentTimeMillis();
		event.setCanceled(true);
	}
	public static void rightClick(RightClickBlock event) {
		if (isServer()) return;
		if (isShiftKeyDown) event.setCanceled(true);
		enacesdPipe(event);
		encasedCogWheel(event);
		chassis(event);
	}
	private static void enacesdPipe(RightClickBlock event) {
		if (!CCG.config.misc.wrench.betterEncasedPipe) return;
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
	private static void encasedCogWheel(RightClickBlock event) {
		if (!CCG.config.misc.wrench.betterEncasedCogwheel) return;
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
	private static void chassis(RightClickBlock event) {
		if (!CCG.config.misc.wrench.betterChassis) return;
		if (hasActivedValueBox()) return;
		var pos = event.getPos();
		var state = event.getLevel().getBlockState(pos);
		if (!(state.getBlock() instanceof AbstractChassisBlock acb)) return;
		if (event.getHand() != InteractionHand.MAIN_HAND) return;
		if (mc.player == null) return;
		if (mc.player.isShiftKeyDown()) return;
		if (hasItemInHand()) return;
		var clickedFace = event.getHitVec().getDirection();
		if (CCGKey.interactOpposite.isDown()) {
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
}
