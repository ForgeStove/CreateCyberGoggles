package io.github.forgestove.create_cyber_goggles.mixin.tooltip;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import org.spongepowered.asm.mixin.*;

import java.util.List;
@Mixin(ShulkerBoxBlock.class)
public abstract class ShulkerBoxBlockMixin extends BaseEntityBlock {
	@Shadow @Final private static Component UNKNOWN_CONTENTS;
	protected ShulkerBoxBlockMixin(Properties properties) {
		super(properties);
	}
	@WrapMethod(method = "appendHoverText")
	public void appendHoverText(
		ItemStack stack,
		TooltipContext context,
		List<Component> tooltipComponents,
		TooltipFlag tooltipFlag,
		Operation<Void> original
	) {
		if (!CCG.config.tooltip.container) {
			original.call(stack, context, tooltipComponents, tooltipFlag);
			return;
		}
		super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
		if (stack.has(DataComponents.CONTAINER_LOOT)) tooltipComponents.add(UNKNOWN_CONTENTS);
	}
}
