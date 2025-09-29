package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.content.logistics.depot.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.getBounds;
@Mixin(EjectorBlockEntity.class)
public abstract class EjectorBlockEntityMixin implements IItemRenderable, IOutlineRenderable {
	@Shadow DepotBehaviour depotBehaviour;
	@Shadow
	public abstract BlockPos getTargetPosition();
	@Override
	public ItemStack ccg$getItemStack() {
		return depotBehaviour.getHeldItemStack();
	}
	@Override
	public void ccg$render() {
		Outliner.getInstance()
			.chaseAABB("EjectorTargetBox" + this, getBounds(getTargetPosition()))
			.lineWidth(1 / 16f)
			.colored(CCG.CONFIG.outlineRenderer.windPushColor);
	}
}
