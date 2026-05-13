package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.logistics.factoryBoard.*;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelSlot;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(FactoryPanelConnectionHandler.class)
public abstract class FactoryPanelConnectionHandlerMixin {
	@Shadow static FactoryPanelPosition validRelocationTarget;
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
		@Local(name = "blockState") BlockState blockState
	) {
		if (!CCG.config.goggles.betterFactoryGauge) return;
		validRelocationTarget = new FactoryPanelPosition(pos, slot);
		Outliner.getInstance()
			.showAABB("target", FactoryPanelConnectionHandler.getBB(blockState, validRelocationTarget))
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
}
