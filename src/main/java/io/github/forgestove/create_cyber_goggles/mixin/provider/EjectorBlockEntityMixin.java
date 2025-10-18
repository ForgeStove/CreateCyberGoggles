package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.logistics.depot.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.theme.Color;
import net.minecraft.core.*;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
@Mixin(value = EjectorBlockEntity.class, remap = false)
public abstract class EjectorBlockEntityMixin extends KineticBlockEntity implements IItemRenderable, IOutlineRenderable {
	@Unique public EntityLauncher ccg$launcher;
	@Shadow DepotBehaviour depotBehaviour;
	public EjectorBlockEntityMixin(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
	}
	@Shadow
	public abstract BlockPos getTargetPosition();
	@Shadow
	protected abstract Direction getFacing();
	@Override
	public ItemStack ccg$getItemStack() {
		return depotBehaviour.getHeldItemStack();
	}
	@Override
	public void ccg$render() {
		var targetPos = getTargetPosition();
		outliner.chaseAABB("EjectorTargetBox" + this, getBounds(targetPos))
			.lineWidth(1 / 16f)
			.colored(CCG.CONFIG.outlineRenderer.windPushColor);
		var xDiff = targetPos.getX() - worldPosition.getX();
		var yDiff = targetPos.getY() - worldPosition.getY();
		var zDiff = targetPos.getZ() - worldPosition.getZ();
		if (ccg$launcher == null) ccg$launcher = new EntityLauncher(Math.abs(xDiff + zDiff), yDiff);
		var totalFlyingTicks = ccg$launcher.getTotalFlyingTicks() + 3;
		var segments = (int) totalFlyingTicks / 3 + 1;
		var tickOffset = totalFlyingTicks / segments;
		var data = new DustParticleOptions(new Color(0x9EDE73).asVectorF(), 1);
		if (mc.level == null) return;
		for (var i = 0; i < segments; i++) {
			var ticks = (AnimationTickHolder.getRenderTime() / 3) % tickOffset + i * tickOffset;
			var vec = ccg$launcher.getGlobalPos(ticks, getFacing().getOpposite(), worldPosition);
			mc.level.addParticle(data, vec.x, vec.y, vec.z, 0, 0, 0);
		}
	}
}
