package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.zurrtum.create.client.AllSpecialTextures;
import com.zurrtum.create.content.schematics.SchematicPrinter;
import com.zurrtum.create.content.schematics.cannon.*;
import com.zurrtum.create.content.schematics.cannon.SchematicannonBlockEntity.State;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;

import java.util.List;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
@Mixin(value = SchematicannonBlockEntity.class, remap = false)
public abstract class SchematicannonBlockEntityMixin implements IItemRenderable, IOutlineRenderable {
	@Shadow public State state;
	@Shadow public SchematicPrinter printer;
	@Shadow public List<LaunchedItem> flyingBlocks;
	@Shadow public ItemStack missingItem;
	@Override
	public ItemStack ccg$getItemStack() {
		if (state == State.STOPPED) return null;
		return missingItem == null ? flyingBlocks.isEmpty() ? null : flyingBlocks.getLast().stack : missingItem;
	}
	@Override
	public void ccg$render() {
		var currentTarget = printer.getCurrentTarget();
		if (currentTarget == null) return;
		outliner.chaseAABB("SchematiCannonTargetBox" + this, getBounds(currentTarget))
			.withFaceTextures(AllSpecialTextures.HIGHLIGHT_CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
			.lineWidth(1 / 16f)
			.colored(CCG.CONFIG.outlineRenderer.windPushColor);
	}
}
