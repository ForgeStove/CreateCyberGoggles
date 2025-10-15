package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.content.equipment.blueprint.BlueprintOverlayRenderer;
import com.simibubi.create.content.logistics.tableCloth.*;
import com.simibubi.create.content.logistics.tableCloth.ShoppingListItem.ShoppingList;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.event.KeyInput;
import io.github.forgestove.create_cyber_goggles.core.util.IItemIndex;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
@Mixin(BlueprintOverlayRenderer.class)
public abstract class BlueprintOverlayRendererMixin {
	@Shadow static List<ItemStack> results;
	@Shadow static boolean resultCraftable;
	@Shadow static BlueprintOverlayShopContext shopContext;
	@Unique private static TableClothBlockEntity ccg$tcbe;
	@Inject(method = "renderOverlay", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", ordinal = 1), cancellable = true)
	private static void renderOverlay(
		GuiGraphics guiGraphics,
		DeltaTracker deltaTracker,
		CallbackInfo ci,
		@Local(name = "x") int x,
		@Local(name = "y") int y,
		@Local(name = "invalidShop") boolean invalidShop
	) {
		if (!CCG.CONFIG.goggles.betterStoreInfo) return;
		ci.cancel();
		if (results.isEmpty()) {
			AllGuiTextures.HOTSLOT.render(guiGraphics, x, y);
			GuiGameElement.of(Items.BARRIER).at(x + 3, y + 3).render(guiGraphics);
			RenderSystem.disableBlend();
			return;
		}
		if (!(ccg$tcbe instanceof IItemIndex iItemIndex)) {
			RenderSystem.disableBlend();
			return;
		}
		var index = iItemIndex.ccg$getIndex() - KeyInput.scrollDeltaY;
		KeyInput.scrollDeltaY = 0;
		var size = results.size();
		if (index < 0) index = size - 1;
		else if (index >= size) index = 0;
		iItemIndex.ccg$setIndex(index);
		var selectedX = 0;
		for (var i = 0; i < size; i++) {
			var result = results.get(i);
			var slot = resultCraftable ? AllGuiTextures.HOTSLOT_SUPER_ACTIVE : AllGuiTextures.HOTSLOT;
			if (!invalidShop && shopContext != null && shopContext.stockLevel() > shopContext.purchases())
				slot = AllGuiTextures.HOTSLOT_ACTIVE;
			slot.render(guiGraphics, resultCraftable ? x - 1 : x, resultCraftable ? y - 1 : y);
			BlueprintOverlayRenderer.drawItemStack(guiGraphics, mc, x, y, result, null);
			if (i == index) selectedX = x;
			x += 21;
		}
		if (selectedX != 0) AllGuiTextures.HOTSLOT_SUPER_ACTIVE.render(guiGraphics, selectedX - 1, y - 1);
		RenderSystem.disableBlend();
	}
	@Inject(method = "displayClothShop", at = @At("HEAD"))
	private static void displayClothShop(TableClothBlockEntity tcbe, int alreadyPurchased, ShoppingList list, CallbackInfo ci) {
		if (!CCG.CONFIG.goggles.betterStoreInfo) return;
		ccg$tcbe = tcbe;
	}
}
