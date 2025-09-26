package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.content.logistics.depot.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
@Mixin(value = EjectorBlockEntity.class, remap = false)
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
		var bounds = CCGUtil.getBounds(getTargetPosition());
		if (bounds == null) return;
		Outliner.getInstance()
			.chaseAABB("EjectorTargetBox" + this, bounds)
			.lineWidth(1 / 16f)
			.colored(CCG.CONFIG.outlineRenderer.windPushColor);
	}
}
