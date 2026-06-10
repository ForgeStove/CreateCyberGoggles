package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.zurrtum.create.client.AllSpecialTextures;
import com.zurrtum.create.content.schematics.cannon.SchematicannonBlockEntity;
import com.zurrtum.create.content.schematics.cannon.SchematicannonBlockEntity.State;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.api.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
@Mixin(SchematicannonBlockEntity.class)
public abstract class SchematicannonBlockEntityMixin implements ItemRenderable, OutlineRenderable, Self<SchematicannonBlockEntity> {
	@Override
	public ItemStack ccg$getItemStack() {
		var self = thiz();
		if (self.state == State.STOPPED) return null;
		var missingItem = self.missingItem;
		var flyingBlocks = self.flyingBlocks;
		return missingItem == null ? flyingBlocks.isEmpty() ? null : flyingBlocks.getLast().stack : missingItem;
	}
	@Override
	public void ccg$render() {
		var currentTarget = thiz().printer.getCurrentTarget();
		if (currentTarget == null) return;
		outliner.chaseAABB("SchematiCannonTargetBox" + this, getBounds(currentTarget))
			.withFaceTextures(AllSpecialTextures.HIGHLIGHT_CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
			.lineWidth(1 / 16f)
			.colored(CCG.config.outliner.outColor);
	}
}
