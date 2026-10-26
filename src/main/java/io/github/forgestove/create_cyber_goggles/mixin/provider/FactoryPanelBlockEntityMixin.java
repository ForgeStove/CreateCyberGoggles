package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.content.logistics.factoryBoard.*;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelSlot;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.api.OutlineRenderable;
import io.github.forgestove.create_cyber_goggles.core.util.contract.Self;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.EnumMap;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
@Mixin(value = FactoryPanelBlockEntity.class, remap = false)
public abstract class FactoryPanelBlockEntityMixin implements OutlineRenderable, Self<FactoryPanelBlockEntity> {
	@Unique private AttachFace ccg$lastFace;
	@Unique private Direction ccg$lastFacing;
	@Override
	public void ccg$render() {
		FactoryPanelBlockEntity thiz = thiz();
		Level level = thiz.getLevel();
		if (level == null) return;
		EnumMap<PanelSlot, FactoryPanelBehaviour> panels = thiz.panels;
		if (panels == null || panels.isEmpty()) return;
		BlockHitResult hitResult = getBlockHitResult();
		if (hitResult == null) return;
		PanelSlot panelSlot = FactoryPanelBlock.getTargetedSlot(thiz.getBlockPos(), thiz.getBlockState(), hitResult.getLocation());
		FactoryPanelBehaviour behaviour = panels.get(panelSlot);
		if (behaviour == null || !behaviour.isActive()) return;
		var hashMap = new Object2IntOpenHashMap<AABB>();
		for (FactoryPanelConnection targetedBy : behaviour.targetedBy.values()) {
			FactoryPanelPosition panelPosition = targetedBy.from;
			BlockPos pos = panelPosition.pos();
			AABB box = FactoryPanelConnectionHandler.getBB(level.getBlockState(pos), panelPosition);
			hashMap.put(box, CCG.config.outliner.inColor);
		}
		for (FactoryPanelPosition panelPosition : behaviour.targeting) {
			BlockPos pos = panelPosition.pos();
			AABB box = FactoryPanelConnectionHandler.getBB(level.getBlockState(pos), panelPosition);
			int newColor = CCG.config.outliner.outColor;
			hashMap.compute(box, (k, old) -> old == null ? newColor : blendColors(old, newColor));
		}
		hashMap.object2IntEntrySet().forEach(entry -> {
			int color = entry.getIntValue();
			AABB box = entry.getKey();
			outliner.showAABB("FactoryPanelIOBox" + box.getCenter(), box)
				.withFaceTextures(AllSpecialTextures.CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
				.lineWidth(1 / 16f)
				.colored(color);
		});
	}
	@Override
	public int ccg$getRenderDelay() {
		return 0;
	}
	/** 方向变化时清 lastShape：rotate 转向只改 blockState 不广播 Redraw，故在 getShape 前自愈缓存，保证碰撞/渲染用新方向重算 */
	@Inject(method = "getShape", at = @At("HEAD"))
	private void ccg$recomputeShapeIfFacingChanged(CallbackInfoReturnable<VoxelShape> cir) {
		BlockState state = thiz().getBlockState();
		if (!(state.getBlock() instanceof FactoryPanelBlock)) return;
		AttachFace face = state.getValue(FactoryPanelBlock.FACE);
		Direction facing = state.getValue(FactoryPanelBlock.FACING);
		if (ccg$lastFace != face || ccg$lastFacing != facing) {
			thiz().lastShape = null;
			ccg$lastFace = face;
			ccg$lastFacing = facing;
		}
	}
}
