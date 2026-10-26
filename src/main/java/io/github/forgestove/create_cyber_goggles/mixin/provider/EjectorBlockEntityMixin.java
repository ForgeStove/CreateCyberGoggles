package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.content.logistics.depot.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.api.*;
import io.github.forgestove.create_cyber_goggles.core.util.contract.Self;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.theme.Color;
import net.minecraft.core.*;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.*;
import org.spongepowered.asm.mixin.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
@Mixin(EjectorBlockEntity.class)
public abstract class EjectorBlockEntityMixin implements ItemRenderable, OutlineRenderable, Self<EjectorBlockEntity> {
	@Shadow DepotBehaviour depotBehaviour;
	@Override
	public ItemStack ccg$getItemStack() {
		return depotBehaviour.getHeldItemStack();
	}
	@Override
	public void ccg$render() {
		EjectorBlockEntity ebe = thiz();
		BlockPos targetPos = ebe.getTargetPosition();
		BlockPos blockPos = ebe.getBlockPos();
		outliner.showAABB("EjectorTargetBox" + this, getBounds(targetPos)).lineWidth(1 / 16f).colored(CCG.config.outliner.outColor);
		int color = cannotLaunch() ? 0xFFFF7171 : 0xFF9EDE73;
		outliner.showAABB("EjectorFromBox" + this, new AABB(0, 0, 0, 1, 0, 1).move(blockPos)).lineWidth(1 / 16f).colored(color);
		int xDiff = targetPos.getX() - blockPos.getX();
		int yDiff = targetPos.getY() - blockPos.getY();
		int zDiff = targetPos.getZ() - blockPos.getZ();
		var launcher = new EntityLauncher(Math.abs(xDiff + zDiff), yDiff);
		double totalFlyingTicks = launcher.getTotalFlyingTicks() + 3;
		int segments = (int) totalFlyingTicks / 3 + 1;
		double tickOffset = totalFlyingTicks / segments;
		var data = new DustParticleOptions(new Color(color).asVectorF(), 1);
		if (mc.level == null) return;
		for (var i = 0; i < segments; i++) {
			double ticks = (AnimationTickHolder.getRenderTime() / 3) % tickOffset + i * tickOffset;
			Vec3 vec = launcher.getGlobalPos(ticks, getFacing().getOpposite(), blockPos);
			mc.level.addParticle(data, vec.x, vec.y, vec.z, 0, 0, 0);
		}
	}
	@Shadow
	protected abstract boolean cannotLaunch();
	@Shadow
	protected abstract Direction getFacing();
}
