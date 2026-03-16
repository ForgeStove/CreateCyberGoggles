package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity.State;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.api.*;
import io.github.forgestove.create_cyber_goggles.core.util.GoggleTooltipUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
@Mixin(SchematicannonBlockEntity.class)
public abstract class SchematicannonBlockEntityMixin
	implements IHaveGoggleInformation, ItemRenderable, OutlineRenderable, Self<SchematicannonBlockEntity> {
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		return GoggleTooltipUtil.cannon(tooltip, self());
	}
	@Override
	public ItemStack ccg$getItemStack() {
		var sbe = self();
		if (sbe.state == State.STOPPED) return null;
		if (sbe.missingItem != null) return sbe.missingItem;
		if (sbe.flyingBlocks.isEmpty()) return null;
		return sbe.flyingBlocks.get(sbe.flyingBlocks.size() - 1).stack;
	}
	@Override
	public void ccg$render() {
		var currentTarget = self().printer.getCurrentTarget();
		if (currentTarget == null) return;
		outliner.chaseAABB("SchematiCannonTargetBox" + this, getBounds(currentTarget))
			.withFaceTextures(AllSpecialTextures.HIGHLIGHT_CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
			.lineWidth(1 / 16f)
			.colored(CCG.config.outliner.outColor);
	}
}
