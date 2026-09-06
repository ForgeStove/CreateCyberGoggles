package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.logistics.factoryBoard.*;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelSlot;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.FactoryPanelUtil;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
@Mixin(FactoryPanelConnectionHandler.class)
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
		var targetState = FactoryPanelUtil.facingState(blockState, mc.hitResult, new FactoryPanelPosition(pos, slot));
		var targetSlot = FactoryPanelBlock.getTargetedSlot(pos, targetState, offsetPos);
		validRelocationTarget = new FactoryPanelPosition(pos, targetSlot);
		Outliner.getInstance()
			.showAABB("target", FactoryPanelConnectionHandler.getBB(targetState, validRelocationTarget))
			.colored(0xeeeeee)
			.disableLineNormals()
			.lineWidth(1 / 16f);
		ci.cancel();
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
		if (FactoryPanelUtil.shouldDetour(from, target)) {
			FactoryPanelUtil.relocateViaDetour(from, target);
			connectingFrom = null;
			connectingFromBox = null;
			validRelocationTarget = null;
			relocating = false;
			return true;
		}
		// 时序：移动包先发(服务端 moveTo 在目标处建立克隆源方向的面板)，旋转包后发才有作用对象；
		// 两者同走底层 TCP 连接故保序。若反序则旋转先到、目标尚为空气而被服务端丢弃。
		boolean result = original.call();
		FactoryPanelUtil.rotatePanelTo(hitResult, from, target);
		return result;
	}
}
