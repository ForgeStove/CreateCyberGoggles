package io.github.forgestove.create_cyber_goggles.mixin.misc.jei;
import com.simibubi.create.compat.jei.GhostIngredientHandler;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterMenu;
import com.simibubi.create.foundation.gui.menu.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.compat.jei.RequestPageGhostTarget;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler.Target;
import mezz.jei.api.ingredients.ITypedIngredient;
import net.minecraft.client.renderer.Rect2i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
/** 红石请求器分页：JEI 拖入目标改为当前页 9 格（映射到 ghostInventory 对应页的槽） */
@Mixin(GhostIngredientHandler.class)
public abstract class GhostIngredientHandlerMixin<T extends GhostItemMenu<?>> {
	@Inject(
		method = "getTargetsTyped(Lcom/simibubi/create/foundation/gui/menu/AbstractSimiContainerScreen;"
			+ "Lmezz/jei/api/ingredients/ITypedIngredient;Z)Ljava/util/List;", at = @At("HEAD"), cancellable = true
	)
	private <I> void pageTargets(
		AbstractSimiContainerScreen<T> gui,
		ITypedIngredient<I> ingredient,
		boolean doStart,
		CallbackInfoReturnable<List<Target<I>>> cir
	) {
		if (!CCG.config.misc.redstoneRequesterLargeRequest) return;
		if (!(gui.getMenu() instanceof RedstoneRequesterMenu menu)) return;
		if (ingredient.getType() != VanillaTypes.ITEM_STACK) return;
		var targets = new ArrayList<Target<I>>();
		var x = gui.getGuiLeft() + 27;
		var y = gui.getGuiTop() + 28;
		for (var i = 0; i < 9; i++)
			targets.add(new RequestPageGhostTarget<>(menu, i, new Rect2i(x + 20 * i, y, 16, 16)));
		cir.setReturnValue(targets);
	}
}
