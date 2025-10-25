package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.content.logistics.box.PackageEntity;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(PackageEntity.class)
public abstract class PackageEntityMixin implements IItemRenderable, ISelf<PackageEntity> {
	@Override
	public ItemStack ccg$getItemStack() {
		return self().getBox();
	}
}
