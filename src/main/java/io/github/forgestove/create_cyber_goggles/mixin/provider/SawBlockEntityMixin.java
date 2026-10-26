package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.content.kinetics.saw.SawBlockEntity;
import com.simibubi.create.content.processing.recipe.ProcessingInventory;
import io.github.forgestove.create_cyber_goggles.api.ItemRenderable;
import io.github.forgestove.create_cyber_goggles.util.contract.Self;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(SawBlockEntity.class)
public abstract class SawBlockEntityMixin implements ItemRenderable, Self<SawBlockEntity> {
	@Override
	public ItemStack ccg$getItemStack() {
		SawBlockEntity thiz = thiz();
		ProcessingInventory inventory = thiz.inventory;
		for (var i = 0; i < inventory.getSlots(); i++) {
			ItemStack stackInSlot = inventory.getStackInSlot(i);
			if (!stackInSlot.isEmpty()) return stackInSlot;
		}
		return null;
	}
}
