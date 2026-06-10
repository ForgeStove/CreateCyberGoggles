package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.zurrtum.create.content.equipment.clipboard.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.api.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(ClipboardBlockEntity.class)
public abstract class ClipboardBlockEntityMixin implements ItemRenderable, Self<ClipboardBlockEntity> {
	@Override
	public ItemStack ccg$getItemStack() {
		if (!CCG.config.tooltip.clipboard) return null;
		var thiz = thiz();
		var state = thiz.getBlockState();
		var level = thiz.getLevel();
		if (level == null) return null;
		return state.getCloneItemStack(level, thiz.getBlockPos(), true);
	}
}
