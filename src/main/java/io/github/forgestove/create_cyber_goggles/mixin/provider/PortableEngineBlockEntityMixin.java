package io.github.forgestove.create_cyber_goggles.mixin.provider;
import dev.simulated_team.simulated.content.blocks.portable_engine.PortableEngineBlockEntity;
import io.github.forgestove.create_cyber_goggles.api.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(PortableEngineBlockEntity.class)
public abstract class PortableEngineBlockEntityMixin implements ItemRenderable, Self<PortableEngineBlockEntity> {
	@Override
	public ItemStack ccg$getItemStack() {
		return thiz().inventory.slot.getStack();
	}
}
