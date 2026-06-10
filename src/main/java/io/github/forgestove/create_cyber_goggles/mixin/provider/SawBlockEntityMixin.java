package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.zurrtum.create.content.kinetics.saw.SawBlockEntity;
import io.github.forgestove.create_cyber_goggles.core.api.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(SawBlockEntity.class)
public abstract class SawBlockEntityMixin implements ItemRenderable, Self<SawBlockEntity> {
	@Override
	public ItemStack ccg$getItemStack() {
		var thiz = thiz();
		var inventory = thiz.inventory;
		for (var i = 0; i < inventory.getContainerSize(); i++) {
			var stackInSlot = inventory.getItem(i);
			if (!stackInSlot.isEmpty()) return stackInSlot;
		}
		return null;
	}
}
