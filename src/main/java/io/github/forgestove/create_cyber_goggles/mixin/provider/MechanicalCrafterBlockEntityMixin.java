package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCrafterBlockEntity;
import com.zurrtum.create.content.kinetics.crafter.MechanicalCrafterBlockEntity.CrafterItemHandler;
import io.github.forgestove.create_cyber_goggles.core.util.IItemRenderable;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
@Mixin(value = MechanicalCrafterBlockEntity.class, remap = false)
public abstract class MechanicalCrafterBlockEntityMixin implements IItemRenderable {
	@Shadow protected CrafterItemHandler inventory;
	@Override
	public ItemStack ccg$getItemStack() {
		return inventory.getItem(0);
	}
}
