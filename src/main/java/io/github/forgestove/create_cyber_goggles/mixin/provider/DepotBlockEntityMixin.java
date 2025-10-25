package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
@Mixin(DepotBlockEntity.class)
public abstract class DepotBlockEntityMixin implements IItemRenderable, ISelf<DepotBlockEntity> {
	@Override
	public ItemStack ccg$getItemStack() {
		return self().getHeldItem();
	}
}
