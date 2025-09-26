package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.content.logistics.box.PackageEntity;
import io.github.forgestove.create_cyber_goggles.core.util.IItemRenderable;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
@Mixin(value = PackageEntity.class, remap = false)
public abstract class PackageEntityMixin implements IItemRenderable {
	@Shadow public ItemStack box;
	@Override
	public ItemStack ccg$getItemStack() {
		return box;
	}
}
