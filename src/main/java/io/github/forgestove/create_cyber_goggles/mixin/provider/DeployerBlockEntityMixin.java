package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.zurrtum.create.content.kinetics.deployer.DeployerBlockEntity;
import io.github.forgestove.create_cyber_goggles.core.api.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(DeployerBlockEntity.class)
public abstract class DeployerBlockEntityMixin implements ItemRenderable, Self<DeployerBlockEntity> {
	@Override
	public ItemStack ccg$getItemStack() {
		return thiz().heldItem;
	}
}
