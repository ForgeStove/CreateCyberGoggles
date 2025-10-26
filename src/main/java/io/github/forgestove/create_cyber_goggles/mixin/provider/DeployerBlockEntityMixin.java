package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.content.kinetics.deployer.DeployerBlockEntity;
import io.github.forgestove.create_cyber_goggles.core.util.ItemRenderable;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
@Mixin(DeployerBlockEntity.class)
public abstract class DeployerBlockEntityMixin implements ItemRenderable {
	@Shadow protected ItemStack heldItem;
	@Override
	public ItemStack ccg$getItemStack() {
		return heldItem;
	}
}
