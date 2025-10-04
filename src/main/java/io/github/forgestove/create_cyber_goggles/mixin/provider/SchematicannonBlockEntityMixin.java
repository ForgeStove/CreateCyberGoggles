package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.schematics.SchematicPrinter;
import com.simibubi.create.content.schematics.cannon.*;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity.State;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;

import java.util.List;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.getBounds;
@Mixin(value = SchematicannonBlockEntity.class, remap = false)
public abstract class SchematicannonBlockEntityMixin implements IHaveGoggleInformation, IItemRenderable, IOutlineRenderable {
	@Shadow public State state;
	@Shadow public SchematicPrinter printer;
	@Shadow public List<LaunchedItem> flyingBlocks;
	@Shadow public ItemStack missingItem;
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		if (!CCG.CONFIG.goggles.enhancedInfo) return false;
		TooltipUtil.cannon(tooltip, (SchematicannonBlockEntity) (Object) this);
		return true;
	}
	@Override
	public ItemStack ccg$getItemStack() {
		if (state == State.STOPPED) return null;
		return missingItem == null ? flyingBlocks.isEmpty() ? null : flyingBlocks.get(flyingBlocks.size() - 1).stack : missingItem;
	}
	@Override
	public void ccg$render() {
		var currentTarget = printer.getCurrentTarget();
		if (currentTarget == null) return;
		Outliner.getInstance()
			.chaseAABB("SchematiCannonTargetBox" + this, getBounds(currentTarget))
			.withFaceTextures(AllSpecialTextures.HIGHLIGHT_CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
			.lineWidth(1 / 16f)
			.colored(CCG.CONFIG.outlineRenderer.windPushColor);
	}
}
