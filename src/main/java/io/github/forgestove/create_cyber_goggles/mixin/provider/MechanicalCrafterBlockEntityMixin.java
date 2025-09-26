package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.content.kinetics.crafter.MechanicalCrafterBlockEntity;
import com.simibubi.create.content.kinetics.crafter.MechanicalCrafterBlockEntity.Inventory;
import io.github.forgestove.create_cyber_goggles.core.util.IItemRenderable;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
@Mixin(MechanicalCrafterBlockEntity.class)
public abstract class MechanicalCrafterBlockEntityMixin implements IItemRenderable {
	@Shadow protected Inventory inventory;
	@Override
	public ItemStack ccg$getItemStack() {
		return inventory.getItem(0);
	}
}
