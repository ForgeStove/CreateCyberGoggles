package io.github.forgestove.create_cyber_goggles.mixin.goggles.provider;
import com.simibubi.create.content.kinetics.crafter.MechanicalCrafterBlockEntity;
import io.github.forgestove.create_cyber_goggles.api.ItemRenderable;
import io.github.forgestove.create_cyber_goggles.foundation.util.Self;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(MechanicalCrafterBlockEntity.class)
public abstract class MechanicalCrafterBlockEntityMixin implements ItemRenderable, Self<MechanicalCrafterBlockEntity> {
	@Override
	public ItemStack ccg$getItemStack() {
		return thiz().getInventory().getItem(0);
	}
}
