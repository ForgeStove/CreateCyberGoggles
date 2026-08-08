package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.content.decoration.placard.PlacardBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.api.ItemRenderable;
import io.github.forgestove.create_cyber_goggles.core.util.contract.Self;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(PlacardBlockEntity.class)
public abstract class PlacardBlockEntityMixin implements ItemRenderable, Self<PlacardBlockEntity> {
	@Override
	public ItemStack ccg$getItemStack() {
		if (!CCG.config.tooltip.packageEntity) return null;
		return thiz().getHeldItem();
	}
}
