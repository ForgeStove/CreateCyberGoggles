package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.content.kinetics.saw.SawBlockEntity;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(SawBlockEntity.class)
public abstract class SawBlockEntityMixin implements IItemRenderable, ISelf<SawBlockEntity> {
	@Override
	public ItemStack ccg$getItemStack() {
		return self().inventory.getStackInSlot(0);
	}
}
