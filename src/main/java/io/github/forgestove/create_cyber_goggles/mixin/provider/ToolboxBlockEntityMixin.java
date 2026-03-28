package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.content.equipment.toolbox.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.api.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(ToolboxBlockEntity.class)
public abstract class ToolboxBlockEntityMixin implements ItemRenderable, Self<ToolboxBlockEntity> {
	@Override
	public ItemStack ccg$getItemStack() {
		if (!CCG.config.tooltip.toolbox) return null;
		var thiz = thiz();
		var state = thiz.getBlockState();
		var level = thiz.getLevel();
		if (level == null) return null;
		if (!(state.getBlock() instanceof ToolboxBlock tb)) return null;
		return tb.getCloneItemStack(level, thiz.getBlockPos(), state);
	}
}
