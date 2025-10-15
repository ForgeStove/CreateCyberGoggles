package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.encased.EncasedCogwheelBlock;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(EncasedCogwheelBlock.class)
public abstract class EncasedCogwheelBlockMixin extends RotatedPillarKineticBlock {
	public EncasedCogwheelBlockMixin(Properties properties) {
		super(properties);
	}
	@Override
	public float getParticleTargetRadius() {
		var thiz = (EncasedCogwheelBlock) (Object) this;
		return thiz.isLargeCog() ? 1.2f : super.getParticleTargetRadius();
	}
	@Override
	public float getParticleInitialRadius() {
		var thiz = (EncasedCogwheelBlock) (Object) this;
		return thiz.isLargeCog() ? 1f : super.getParticleTargetRadius();
	}
}
