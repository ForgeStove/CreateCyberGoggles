package io.github.forgestove.create_cyber_goggles.core.util;
import com.simibubi.create.content.contraptions.wrench.RadialWrenchMenuSubmitPacket;
import com.simibubi.create.content.logistics.factoryBoard.*;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.Nullable;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
public final class FactoryPanelUtil {
	/** 移动包发出后，把目标位置面板转向鼠标命中的目标面方向：moveTo 已把源方向克隆到目标 */
	public static void rotatePanelTo(HitResult hitResult, FactoryPanelPosition from, FactoryPanelPosition target) {
		var level = mc.level;
		if (level == null) return;
		var sourceState = level.getBlockState(from.pos()); // 源方向 = moveTo 克隆到目标的面板方向
		if (!(sourceState.getBlock() instanceof FactoryPanelBlock)) return;
		var newState = facingState(sourceState, hitResult, target);
		if (sameOrientation(sourceState, newState)) return; // 目标方向=源方向，无需转向
		sendToServer(new RadialWrenchMenuSubmitPacket(target.pos(), newState));
	}
	/** 按鼠标命中的目标面把工厂仪表方块状态定好方向（复刻放置逻辑，保证槽位映射一致） */
	public static BlockState facingState(BlockState base, HitResult hitResult, FactoryPanelPosition target) {
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
	public static boolean sameOrientation(BlockState a, BlockState b) {
		return a.getValue(FactoryPanelBlock.FACE) == b.getValue(FactoryPanelBlock.FACE)
			&& a.getValue(FactoryPanelBlock.FACING) == b.getValue(FactoryPanelBlock.FACING);
	}
	/** 是否满足中转绕行条件：方向不同 + 目标已有仪表(gauge) + 目标槽为空 */
	public static boolean shouldDetour(FactoryPanelPosition from, FactoryPanelPosition target) {
		var level = mc.level;
		if (level == null) return false;
		var sourceState = level.getBlockState(from.pos());
		var targetState = level.getBlockState(target.pos());
		if (!(sourceState.getBlock() instanceof FactoryPanelBlock)) return false;
		if (!(targetState.getBlock() instanceof FactoryPanelBlock)) return false; // 目标须已有仪表
		if (sameOrientation(sourceState, targetState)) return false; // 方向相同无需绕行
		return targetSlotEmpty(target);
	}
	/** 目标槽是否为空(该槽无面板行为；每槽都会初始化 behaviour 对象，须以 isActive 判定真实面板) */
	public static boolean targetSlotEmpty(FactoryPanelPosition target) {
		var level = mc.level;
		if (level == null) return false;
		var be = level.getBlockEntity(target.pos());
		if (!(be instanceof FactoryPanelBlockEntity fpbe)) return false;
		var behaviour = fpbe.panels.get(target.slot());
		return behaviour == null || !behaviour.isActive();
	}
	/** 中转绕行：①moveTo 中转点 → ②转向目标仪表方向 → ③moveTo 进目标空槽 */
	public static void relocateViaDetour(FactoryPanelPosition from, FactoryPanelPosition target) {
		var level = mc.level;
		if (level == null) return;
		var targetState = level.getBlockState(target.pos());
		if (!(targetState.getBlock() instanceof FactoryPanelBlock)) return;
		var detour = findDetourPosition(target, targetState);
		if (detour == null) return; // 找不到可用的中转点→放弃
		// ① 源面板 moveTo 中转点(克隆源方向，无需支撑，moveTo 直接 setBlock 建 gauge)
		sendToServer(new FactoryPanelConnectionPacket(detour, from, true));
		// ② 中转点面板转向目标仪表方向(中转点需能支撑该方向，findDetourPosition 已保证)
		sendToServer(new RadialWrenchMenuSubmitPacket(detour.pos(), targetState));
		// ③ 中转点面板 moveTo 目标空槽(方向已一致→不触发 return③，并入成功)
		sendToServer(new FactoryPanelConnectionPacket(target, detour, true));
	}
	/** 在目标位置附近就近找一个「空气且能支撑目标方向」的格做中转点(扫 6 邻域即可) */
	public static @Nullable FactoryPanelPosition findDetourPosition(FactoryPanelPosition target, BlockState targetState) {
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
