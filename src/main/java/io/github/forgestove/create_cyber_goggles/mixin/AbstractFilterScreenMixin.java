package io.github.forgestove.create_cyber_goggles.mixin;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.simibubi.create.content.logistics.filter.AbstractFilterScreen;
import io.github.forgestove.create_cyber_goggles.core.api.Self;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(AbstractFilterScreen.class)
public abstract class AbstractFilterScreenMixin implements Self<AbstractFilterScreen<?>> {
	@WrapWithCondition(
		method = "containerTick",
		at = @At(
			value = "INVOKE",
			target = "Lio/github/fabricators_of_create/porting_lib/util/PlayerEntityHelper;closeScreen"
				+ "(Lnet/minecraft/world/entity/player/Player;)V"
		)
	)
	public boolean containerTick(Player player) {
		return thiz().getMenu().containerId != -1;
	}
}
