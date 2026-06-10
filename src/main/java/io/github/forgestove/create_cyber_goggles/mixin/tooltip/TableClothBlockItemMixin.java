package io.github.forgestove.create_cyber_goggles.mixin.tooltip;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.zurrtum.create.content.logistics.tableCloth.TableClothBlockItem;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;
@Mixin(TableClothBlockItem.class)
public abstract class TableClothBlockItemMixin {
	@WrapWithCondition(
		method = "appendHoverText", at = @At(
		value = "INVOKE",
		target = "Lcom/zurrtum/create/content/logistics/redstoneRequester/RedstoneRequesterBlock;appendRequesterTooltip"
			+ "(Lnet/minecraft/world/item/ItemStack;Ljava/util/function/Consumer;)V"
	)
	)
	public boolean wrapTooltip(ItemStack pStack, Consumer<Component> pTooltip) {
		return !CCG.config.tooltip.tableCloth;
	}
}
