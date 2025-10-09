package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.zurrtum.create.content.logistics.packager.PackagerBlockEntity;
import io.github.forgestove.create_cyber_goggles.core.util.IItemRenderable;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
@Mixin(PackagerBlockEntity.class)
public abstract class PackagerBlockEntityMixin implements IItemRenderable {
	@Shadow public ItemStack heldBox;
	@Override
	public ItemStack ccg$getItemStack() {
		return heldBox;
	}
}
