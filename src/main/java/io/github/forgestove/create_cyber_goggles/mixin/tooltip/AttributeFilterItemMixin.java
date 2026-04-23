package io.github.forgestove.create_cyber_goggles.mixin.tooltip;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.simibubi.create.content.logistics.filter.AttributeFilterItem;
import io.github.forgestove.create_cyber_goggles.CCG;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(AttributeFilterItem.class)
public abstract class AttributeFilterItemMixin {
	@ModifyExpressionValue(method = "makeSummary", at = @At(value = "CONSTANT", args = "intValue=3"))
	private static int makeSummary(int original) {
		return CCG.config.tooltip.attributeFilter ? Integer.MAX_VALUE : original;
	}
}
