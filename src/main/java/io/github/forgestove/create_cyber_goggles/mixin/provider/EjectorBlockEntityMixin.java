package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.content.logistics.depot.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.api.*;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
@Mixin(EjectorBlockEntity.class)
public abstract class EjectorBlockEntityMixin implements ItemRenderable, OutlineRenderable, Self<EjectorBlockEntity> {
	@Shadow
	protected abstract boolean cannotLaunch();
	@Shadow
	public abstract Direction getFacing();
	@Override
	public ItemStack ccg$getItemStack() {
		return thiz().depotBehaviour.getHeldItemStack();
	}
	@Override
	public void ccg$render() {
		var ebe = thiz();
		var targetPos = ebe.getTargetPosition();
		var blockPos = ebe.getBlockPos();
		outliner.showAABB("EjectorTargetBox" + this, getBounds(targetPos)).lineWidth(1 / 16f).colored(CCG.config.outliner.outColor);
		var color = cannotLaunch() ? 0xFFFF7171 : 0xFF9EDE73;
		outliner.showAABB("EjectorFromBox" + this, new AABB(0, 0, 0, 1, 0, 1).move(blockPos)).lineWidth(1 / 16f).colored(color);
		var xDiff = targetPos.getX() - blockPos.getX();
		var yDiff = targetPos.getY() - blockPos.getY();
		var zDiff = targetPos.getZ() - blockPos.getZ();
		var launcher = new EntityLauncher(Math.abs(xDiff + zDiff), yDiff);
		var totalFlyingTicks = launcher.getTotalFlyingTicks() + 3;
		var segments = (int) totalFlyingTicks / 3 + 1;
		var tickOffset = totalFlyingTicks / segments;
		var data = new DustParticleOptions(color, 1);
		if (mc.level == null) return;
		for (var i = 0; i < segments; i++) {
			var ticks = (AnimationTickHolder.getRenderTime() / 3) % tickOffset + i * tickOffset;
			var vec = launcher.getGlobalPos(ticks, getFacing().getOpposite(), blockPos);
			mc.level.addParticle(data, vec.x, vec.y, vec.z, 0, 0, 0);
		}
	}
}
