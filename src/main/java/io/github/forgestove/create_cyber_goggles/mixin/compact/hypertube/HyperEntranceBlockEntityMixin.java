package io.github.forgestove.create_cyber_goggles.mixin.compact.hypertube;
import io.github.forgestove.create_cyber_goggles.api.SelfKineticInfo;
import org.spongepowered.asm.mixin.*;
@Pseudo
@Mixin(targets = "com.pedrorok.hypertube.blocks.blockentities.HyperEntranceBlockEntity")
public abstract class HyperEntranceBlockEntityMixin implements SelfKineticInfo {}
