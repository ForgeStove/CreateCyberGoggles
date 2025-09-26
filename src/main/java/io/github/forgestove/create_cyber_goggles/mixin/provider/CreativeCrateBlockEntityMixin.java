package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.content.logistics.crate.CreativeCrateBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import io.github.forgestove.create_cyber_goggles.core.util.IItemRenderable;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
@Mixin(CreativeCrateBlockEntity.class)
public abstract class CreativeCrateBlockEntityMixin implements IItemRenderable {
	@Shadow FilteringBehaviour filtering;
	@Override
	public ItemStack ccg$getItemStack() {
		return filtering.getFilter();
	}
}
