package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.zurrtum.create.content.logistics.filter.ListFilterItem;
import io.github.forgestove.create_cyber_goggles.CCG;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(ListFilterItem.class)
public abstract class ListFilterItemMixin {
	@ModifyExpressionValue(method = "makeSummary", at = @At(value = "CONSTANT", args = "intValue=3"))
	private static int makeSummary(int original) {
		return CCG.config.goggles.enhancedInfo ? Integer.MAX_VALUE : original;
	}
}
