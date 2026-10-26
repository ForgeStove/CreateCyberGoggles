package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.content.equipment.clipboard.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.api.ItemRenderable;
import io.github.forgestove.create_cyber_goggles.core.util.contract.Self;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(ClipboardBlockEntity.class)
public abstract class ClipboardBlockEntityMixin implements ItemRenderable, Self<ClipboardBlockEntity> {
	@Override
	public ItemStack ccg$getItemStack() {
		if (!CCG.config.tooltip.clipboard) return null;
		ClipboardBlockEntity thiz = thiz();
		BlockState state = thiz.getBlockState();
		Level level = thiz.getLevel();
		if (level == null) return null;
		if (!(state.getBlock() instanceof ClipboardBlock block)) return null;
		return block.getCloneItemStack(level, thiz.getBlockPos(), state);
	}
}
