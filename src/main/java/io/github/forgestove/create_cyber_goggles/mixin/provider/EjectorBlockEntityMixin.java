package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.zurrtum.create.content.logistics.depot.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
@Mixin(value = EjectorBlockEntity.class, remap = false)
public abstract class EjectorBlockEntityMixin implements IItemRenderable, IOutlineRenderable {
	@Shadow public DepotBehaviour depotBehaviour;
	@Shadow
	public abstract BlockPos getTargetPosition();
	@Override
	public ItemStack ccg$getItemStack() {
		return depotBehaviour.getHeldItemStack();
	}
	@Override
	public void ccg$render() {
		outliner.chaseAABB("EjectorTargetBox" + this, getBounds(getTargetPosition()))
			.lineWidth(1 / 16f)
			.colored(CCG.CONFIG.outlineRenderer.windPushColor);
	}
}
