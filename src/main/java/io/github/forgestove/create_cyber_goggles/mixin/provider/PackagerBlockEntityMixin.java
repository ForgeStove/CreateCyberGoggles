package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import io.github.forgestove.create_cyber_goggles.core.util.IItemRenderable;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
@Mixin(value = PackagerBlockEntity.class, remap = false)
public abstract class PackagerBlockEntityMixin implements IItemRenderable {
	@Shadow public ItemStack heldBox;
	@Override
	public ItemStack ccg$getItemStack() {
		return heldBox;
	}
}
