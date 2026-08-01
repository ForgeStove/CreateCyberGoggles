package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogwheelBlock;
import io.github.forgestove.create_cyber_goggles.api.Self;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(EncasedCogwheelBlock.class)
public abstract class EncasedCogwheelBlockMixin extends RotatedPillarKineticBlock implements Self<EncasedCogwheelBlock> {
	public EncasedCogwheelBlockMixin(Properties properties) {
		super(properties);
	}
	@Override
	public float getParticleTargetRadius() {
		return thiz().isLargeCog() ? 1.2f : super.getParticleTargetRadius();
	}
	@Override
	public float getParticleInitialRadius() {
		return thiz().isLargeCog() ? 1f : super.getParticleTargetRadius();
	}
}
