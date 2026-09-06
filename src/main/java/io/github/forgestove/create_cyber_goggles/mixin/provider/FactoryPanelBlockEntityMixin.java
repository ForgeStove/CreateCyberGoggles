package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.content.logistics.factoryBoard.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.api.OutlineRenderable;
import io.github.forgestove.create_cyber_goggles.core.util.contract.Self;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
@Mixin(value = FactoryPanelBlockEntity.class, remap = false)
public abstract class FactoryPanelBlockEntityMixin implements OutlineRenderable, Self<FactoryPanelBlockEntity> {
	@Override
	public void ccg$render() {
		var thiz = thiz();
		var level = thiz.getLevel();
		if (level == null) return;
		var panels = thiz.panels;
		if (panels == null || panels.isEmpty()) return;
		var hitResult = getBlockHitResult();
		if (hitResult == null) return;
		var panelSlot = FactoryPanelBlock.getTargetedSlot(thiz.getBlockPos(), thiz.getBlockState(), hitResult.getLocation());
		var behaviour = panels.get(panelSlot);
		if (behaviour == null || !behaviour.isActive()) return;
		var hashMap = new Object2IntOpenHashMap<AABB>();
		for (var targetedBy : behaviour.targetedBy.values()) {
			var panelPosition = targetedBy.from;
			var pos = panelPosition.pos();
			var box = FactoryPanelConnectionHandler.getBB(level.getBlockState(pos), panelPosition);
			hashMap.put(box, CCG.config.outliner.inColor);
		}
		for (var panelPosition : behaviour.targeting) {
			var pos = panelPosition.pos();
			var box = FactoryPanelConnectionHandler.getBB(level.getBlockState(pos), panelPosition);
			var newColor = CCG.config.outliner.outColor;
			hashMap.compute(box, (k, old) -> old == null ? newColor : blendColors(old, newColor));
		}
		hashMap.object2IntEntrySet().forEach(entry -> {
			var color = entry.getIntValue();
			var box = entry.getKey();
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
	@Unique private AttachFace ccg$lastFace;
	@Unique private Direction ccg$lastFacing;
	/** 方向变化时清 lastShape：rotate 转向只改 blockState 不广播 Redraw，故在 getShape 前自愈缓存，保证碰撞/渲染用新方向重算 */
	@Inject(method = "getShape", at = @At("HEAD"))
	private void ccg$recomputeShapeIfFacingChanged(CallbackInfoReturnable<VoxelShape> cir) {
		var state = thiz().getBlockState();
		if (!(state.getBlock() instanceof FactoryPanelBlock)) return;
		var face = state.getValue(FactoryPanelBlock.FACE);
		var facing = state.getValue(FactoryPanelBlock.FACING);
		if (ccg$lastFace != face || ccg$lastFacing != facing) {
			thiz().lastShape = null;
			ccg$lastFace = face;
			ccg$lastFacing = facing;
		}
	}
}
