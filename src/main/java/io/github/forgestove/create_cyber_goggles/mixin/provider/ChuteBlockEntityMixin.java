package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.content.logistics.chute.ChuteBlockEntity;
import io.github.forgestove.create_cyber_goggles.core.util.IItemRenderable;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
@Mixin(value = ChuteBlockEntity.class, remap = false)
public abstract class ChuteBlockEntityMixin implements IItemRenderable {
	@Shadow ItemStack item;
	@Override
	public ItemStack ccg$getItemStack() {
		return item;
	}
}
