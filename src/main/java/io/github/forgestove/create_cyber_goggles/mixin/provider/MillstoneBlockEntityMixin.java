package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.zurrtum.create.content.kinetics.millstone.MillstoneBlockEntity;
import io.github.forgestove.create_cyber_goggles.core.api.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(MillstoneBlockEntity.class)
public abstract class MillstoneBlockEntityMixin implements ItemRenderable, Self<MillstoneBlockEntity> {
	@Override
	public ItemStack ccg$getItemStack() {
		return thiz().capability.getItem(0);
	}
}
