package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelControllerBlockEntity;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(CrushingWheelControllerBlockEntity.class)
public abstract class CrushingWheelControllerBlockEntityMixin implements IItemRenderable, ISelf<CrushingWheelControllerBlockEntity> {
	@Override
	public ItemStack ccg$getItemStack() {
		return self().inventory.getStackInSlot(0);
	}
}
