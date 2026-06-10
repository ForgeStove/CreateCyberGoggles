package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.zurrtum.create.content.kinetics.crusher.CrushingWheelControllerBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.api.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(CrushingWheelControllerBlockEntity.class)
public abstract class CrushingWheelControllerBlockEntityMixin implements ItemRenderable, Self<CrushingWheelControllerBlockEntity> {
	@Override
	public ItemStack ccg$getItemStack() {
		if (!CCG.config.tooltip.crushingController) return null;
		var thiz = thiz();
		if (thiz.processingEntity instanceof ItemEntity ie) return ie.getItem();
		var inventory = thiz.inventory;
		for (var i = 0; i < inventory.getContainerSize(); i++) {
			var stackInSlot = inventory.getItem(i);
			if (!stackInSlot.isEmpty()) return stackInSlot;
		}
		return null;
	}
}
