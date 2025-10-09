package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.zurrtum.create.content.logistics.crate.CreativeCrateBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import io.github.forgestove.create_cyber_goggles.core.util.IItemRenderable;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
@Mixin(value = CreativeCrateBlockEntity.class, remap = false)
public abstract class CreativeCrateBlockEntityMixin implements IItemRenderable {
	@Shadow ServerFilteringBehaviour filtering;
	@Override
	public ItemStack ccg$getItemStack() {
		return filtering.getFilter();
	}
}
