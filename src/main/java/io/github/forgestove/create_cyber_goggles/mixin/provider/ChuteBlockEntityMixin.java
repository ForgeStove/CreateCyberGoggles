package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.content.logistics.chute.ChuteBlockEntity;
import io.github.forgestove.create_cyber_goggles.core.api.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(ChuteBlockEntity.class)
public abstract class ChuteBlockEntityMixin implements ItemRenderable, Self<ChuteBlockEntity> {
	@Override
	public ItemStack ccg$getItemStack() {
		return thiz().getItem();
	}
}
