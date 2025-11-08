package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCrafterBlockEntity;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(MechanicalCrafterBlockEntity.class)
public abstract class MechanicalCrafterBlockEntityMixin implements ItemRenderable, Self<MechanicalCrafterBlockEntity> {
	@Override
	public ItemStack ccg$getItemStack() {
		return self().getInventory().getItem(0);
	}
}
