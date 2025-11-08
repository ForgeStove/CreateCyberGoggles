package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.zurrtum.create.content.logistics.depot.EjectorBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
@Mixin(EjectorBlockEntity.class)
public abstract class EjectorBlockEntityMixin implements ItemRenderable, OutlineRenderable, Self<EjectorBlockEntity> {
	@Override
	public ItemStack ccg$getItemStack() {
		return self().depotBehaviour.getHeldItemStack();
	}
	@Override
	public void ccg$render() {
		outliner.chaseAABB("EjectorTargetBox" + this, getBounds(self().getTargetPosition()))
			.lineWidth(1 / 16f)
			.colored(CCG.CONFIG.outliner.outColor);
	}
}
