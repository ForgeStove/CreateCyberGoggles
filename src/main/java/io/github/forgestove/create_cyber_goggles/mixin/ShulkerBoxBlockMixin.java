package io.github.forgestove.create_cyber_goggles.mixin;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.*;
import org.spongepowered.asm.mixin.*;

import java.util.List;
@Debug(export = true)
@Mixin(ShulkerBoxBlock.class)
public abstract class ShulkerBoxBlockMixin extends BaseEntityBlock {
	protected ShulkerBoxBlockMixin(Properties properties) {
		super(properties);
	}
	@WrapMethod(method = "appendHoverText")
	public void appendHoverText(ItemStack stack, BlockGetter level, List<Component> tooltip, TooltipFlag flag, Operation<Void> original) {
		if (!CCG.config.tooltip.container) {
			original.call(stack, level, tooltip, flag);
			return;
		}
		super.appendHoverText(stack, level, tooltip, flag);
		var compoundtag = BlockItem.getBlockEntityData(stack);
		if (compoundtag != null && compoundtag.contains("LootTable", 8)) tooltip.add(Component.literal("???????"));
	}
}
