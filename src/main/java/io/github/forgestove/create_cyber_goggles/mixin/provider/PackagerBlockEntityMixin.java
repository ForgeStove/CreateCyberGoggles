package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import io.github.forgestove.create_cyber_goggles.api.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(PackagerBlockEntity.class)
public abstract class PackagerBlockEntityMixin implements ItemRenderable, Self<PackagerBlockEntity> {
	@Override
	public ItemStack ccg$getItemStack() {
		return thiz().heldBox;
	}
}
