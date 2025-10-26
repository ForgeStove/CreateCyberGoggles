package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.simibubi.create.content.logistics.filter.ListFilterItem;
import io.github.forgestove.create_cyber_goggles.CCG;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(value = ListFilterItem.class, remap = false)
public abstract class ListFilterItemMixin {
	@ModifyExpressionValue(method = "makeSummary", at = @At(value = "CONSTANT", args = "intValue=3"))
	private static int makeSummary(int original) {
		return CCG.CONFIG.goggles.enhancedInfo ? Integer.MAX_VALUE : original;
	}
}
