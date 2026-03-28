package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.content.kinetics.crafter.MechanicalCrafterBlockEntity;
import io.github.forgestove.create_cyber_goggles.core.api.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(MechanicalCrafterBlockEntity.class)
public abstract class MechanicalCrafterBlockEntityMixin implements ItemRenderable, Self<MechanicalCrafterBlockEntity> {
	@Override
	public ItemStack ccg$getItemStack() {
		return thiz().getInventory().getItem(0);
	}
}
