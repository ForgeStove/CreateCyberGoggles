package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.contraptions.wrench.RadialWrenchMenuSubmitPacket;
import com.simibubi.create.content.logistics.factoryBoard.*;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelSlot;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.core.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
@Mixin(value = FactoryPanelConnectionHandler.class, remap = false)
public abstract class FactoryPanelConnectionHandlerMixin {
	@Shadow static FactoryPanelPosition validRelocationTarget;
	@Shadow static FactoryPanelPosition connectingFrom;
	@Shadow static boolean relocating;
	@Shadow static AABB connectingFromBox;
	@Inject(
		method = "clientTick", at = @At(
		value = "INVOKE",
		target = "Lcom/simibubi/create/content/logistics/factoryBoard/FactoryPanelBlock;connectedDirection"
			+ "(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/core/Direction;"
	), cancellable = true
	)
	private static void clientTick(
		CallbackInfo ci,
		@Local(name = "pos") BlockPos pos,
		@Local(name = "slot") PanelSlot slot,
		@Local(name = "blockState") BlockState blockState,
		@Local(name = "offsetPos") Vec3 offsetPos
	) {
		if (!CCG.config.goggles.betterFactoryGauge) return;
		// 用目标方向状态重算槽位：rotatePanelTo 会转向目标面，槽位点击映射必须与之一致
		var targetState = ccg$facingState(blockState, mc.hitResult, new FactoryPanelPosition(pos, slot));
		var targetSlot = FactoryPanelBlock.getTargetedSlot(pos, targetState, offsetPos);
		validRelocationTarget = new FactoryPanelPosition(pos, targetSlot);
		Outliner.getInstance()
			.showAABB("target", FactoryPanelConnectionHandler.getBB(targetState, validRelocationTarget))
			.colored(0xeeeeee)
			.disableLineNormals()
			.lineWidth(1 / 16f);
		ci.cancel();
	}
	/** 按鼠标命中的目标面把工厂仪表方块状态定好方向（复刻放置逻辑，保证槽位映射一致） */
	@Unique
	private static BlockState ccg$facingState(BlockState base, HitResult hitResult, FactoryPanelPosition target) {
		if (!(hitResult instanceof BlockHitResult bhr)) return base;
		var level = mc.level;
		var player = mc.player;
		if (level == null || player == null || !(base.getBlock() instanceof FactoryPanelBlock fp)) return base;
		var ctx = new BlockPlaceContext(level, player, InteractionHand.MAIN_HAND, ItemStack.EMPTY, bhr);
		// 以目标位置重定向，模拟"在 target.pos() 按 clickedFace 面放置"，让 Create 计算合法方向
		var targetCtx = BlockPlaceContext.at(ctx, target.pos(), bhr.getDirection());
		var placed = fp.getStateForPlacement(targetCtx);
		return placed != null ? placed : base;
	}
	@WrapMethod(
		method = "checkForIssues(Lcom/simibubi/create/content/logistics/factoryBoard/FactoryPanelBehaviour;"
			+ "Lcom/simibubi/create/content/logistics/factoryBoard/FactoryPanelBehaviour;)Ljava/lang/String;"
	)
	private static String checkForIssues(FactoryPanelBehaviour from, FactoryPanelBehaviour to, Operation<String> original) {
		if (!CCG.config.goggles.betterFactoryGauge) return original.call(from, to);
		return null;
	}
	@WrapMethod(
		method = "checkForIssues(Lcom/simibubi/create/content/logistics/factoryBoard/FactoryPanelBehaviour;"
			+ "Lcom/simibubi/create/content/logistics/factoryBoard/FactoryPanelSupportBehaviour;)Ljava/lang/String;"
	)
	private static String checkForIssues(FactoryPanelBehaviour from, FactoryPanelSupportBehaviour to, Operation<String> original) {
		if (!CCG.config.goggles.betterFactoryGauge) return original.call(from, to);
		return null;
	}
	/** 重定位确认：先交原方法发移动包(moveTo 克隆源面板方向到目标)，再发旋转包把目标面板转向点击面方向 */
	@WrapMethod(method = "onRightClick")
	private static boolean onRightClick(Operation<Boolean> original) {
		var wasRelocating = relocating;
		var from = connectingFrom;
		var target = validRelocationTarget;
		var hitResult = mc.hitResult;
		var engaged = CCG.config.goggles.betterFactoryGauge
			&& wasRelocating
			&& from != null
			&& target != null
			&& mc.player != null
			&& !mc.player.isShiftKeyDown();
		if (!engaged) return original.call();
		// 方向不同 + 目标已有仪表 + 目标槽空 → 直 moveTo 会因 return③ 空转，改走中转绕行，避免误转目标仪表
		if (ccg$shouldDetour(from, target)) {
			ccg$relocateViaDetour(from, target);
			ccg$finishRelocation();
			return true;
		}
		// 时序：移动包先发(服务端 moveTo 在目标处建立克隆源方向的面板)，旋转包后发才有作用对象；
		// 两者同走底层 TCP 连接故保序。若反序则旋转先到、目标尚为空气而被服务端丢弃。
		boolean result = original.call();
		ccg$rotatePanelTo(hitResult, from, target);
		return result;
	}
	/** 是否满足中转绕行条件：方向不同 + 目标已有仪表(gauge) + 目标槽为空 */
	@Unique
	private static boolean ccg$shouldDetour(FactoryPanelPosition from, FactoryPanelPosition target) {
		var level = mc.level;
		if (level == null) return false;
		var sourceState = level.getBlockState(from.pos());
		var targetState = level.getBlockState(target.pos());
		if (!(sourceState.getBlock() instanceof FactoryPanelBlock)) return false;
		if (!(targetState.getBlock() instanceof FactoryPanelBlock)) return false; // 目标须已有仪表
		if (ccg$sameOrientation(sourceState, targetState)) return false; // 方向相同无需绕行
		return ccg$targetSlotEmpty(target);
	}
	/** 中转绕行：①moveTo 中转点 → ②转向目标仪表方向 → ③moveTo 进目标空槽 */
	@Unique
	private static void ccg$relocateViaDetour(FactoryPanelPosition from, FactoryPanelPosition target) {
		var level = mc.level;
		if (level == null) return;
		var targetState = level.getBlockState(target.pos());
		if (!(targetState.getBlock() instanceof FactoryPanelBlock)) return;
		var detour = ccg$findDetourPosition(target, targetState);
		if (detour == null) return; // 找不到可用的中转点→放弃
		// ① 源面板 moveTo 中转点(克隆源方向，无需支撑，moveTo 直接 setBlock 建 gauge)
		sendToServer(new FactoryPanelConnectionPacket(detour, from, true));
		// ② 中转点面板转向目标仪表方向(中转点需能支撑该方向，findDetourPosition 已保证)
		sendToServer(new RadialWrenchMenuSubmitPacket(detour.pos(), targetState));
		// ③ 中转点面板 moveTo 目标空槽(方向已一致→不触发 return③，并入成功)
		sendToServer(new FactoryPanelConnectionPacket(target, detour, true));
	}
	/** 清理重定位状态(走中转流程时未调用原方法，需自己清) */
	@Unique
	private static void ccg$finishRelocation() {
		connectingFrom = null;
		connectingFromBox = null;
		validRelocationTarget = null;
		relocating = false;
	}
	/** 移动包发出后，把目标位置面板转向鼠标命中的目标面方向：moveTo 已把源方向克隆到目标 */
	@Unique
	private static void ccg$rotatePanelTo(HitResult hitResult, FactoryPanelPosition from, FactoryPanelPosition target) {
		var level = mc.level;
		if (level == null) return;
		var sourceState = level.getBlockState(from.pos()); // 源方向 = moveTo 克隆到目标的面板方向
		if (!(sourceState.getBlock() instanceof FactoryPanelBlock)) return;
		var newState = ccg$facingState(sourceState, hitResult, target);
		if (ccg$sameOrientation(sourceState, newState)) return; // 目标方向=源方向，无需转向
		sendToServer(new RadialWrenchMenuSubmitPacket(target.pos(), newState));
	}
	@Unique
	private static boolean ccg$sameOrientation(BlockState a, BlockState b) {
		return a.getValue(FactoryPanelBlock.FACE) == b.getValue(FactoryPanelBlock.FACE)
			&& a.getValue(FactoryPanelBlock.FACING) == b.getValue(FactoryPanelBlock.FACING);
	}
	/** 目标槽是否为空(该槽无面板行为；每槽都会初始化 behaviour 对象，须以 isActive 判定真实面板) */
	@Unique
	private static boolean ccg$targetSlotEmpty(FactoryPanelPosition target) {
		var level = mc.level;
		if (level == null) return false;
		var be = level.getBlockEntity(target.pos());
		if (!(be instanceof FactoryPanelBlockEntity fpbe)) return false;
		var behaviour = fpbe.panels.get(target.slot());
		return behaviour == null || !behaviour.isActive();
	}
	/** 在目标位置附近就近找一个「空气且能支撑目标方向」的格做中转点(扫 6 邻域即可) */
	@Unique
	private static FactoryPanelPosition ccg$findDetourPosition(FactoryPanelPosition target, BlockState targetState) {
		var level = mc.level;
		if (level == null) return null;
		var origin = target.pos();
		for (var direction : Direction.values()) {
			var pos = origin.relative(direction);
			if (!level.getBlockState(pos).isAir()) continue;
			if (targetState.canSurvive(level, pos)) return new FactoryPanelPosition(pos, target.slot());
		}
		return null;
	}
}
