package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.content.equipment.blueprint.BlueprintOverlayRenderer;
import com.simibubi.create.content.logistics.tableCloth.BlueprintOverlayShopContext;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.event.*;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
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
	@Inject(method = "renderOverlay", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", ordinal = 1), cancellable = true)
	private static void renderOverlay(
		GuiGraphics guiGraphics,
		DeltaTracker deltaTracker,
		CallbackInfo callbackInfo,
		@Local(name = "x") int x,
		@Local(name = "y") int y,
		@Local(name = "invalidShop") boolean invalidShop
	) {
		if (!CCG.CONFIG.goggles.betterStoreInfo) return;
		callbackInfo.cancel();
		if (results.isEmpty()) {
			guiGraphics.blitSprite(ResourceLocation.withDefaultNamespace("hud/hotbar_offhand_left"), x, y, 24, 23);
			GuiGameElement.of(Items.BARRIER).at(x + 3, y + 3).render(guiGraphics);
		} else {
			MouseScroll.index += MouseScroll.scrollDeltaY;
			MouseScroll.scrollDeltaY = 0;
			if (MouseScroll.index < 1) MouseScroll.index = results.size();
			else if (MouseScroll.index > results.size()) MouseScroll.index = 1;
			var selectedX = 0;
			for (var i = 0; i < results.size(); i++) {
				var result = results.get(i);
				var slot = resultCraftable ? AllGuiTextures.HOTSLOT_SUPER_ACTIVE : AllGuiTextures.HOTSLOT;
				if (!invalidShop && shopContext != null && shopContext.stockLevel() > shopContext.purchases())
					slot = AllGuiTextures.HOTSLOT_ACTIVE;
				slot.render(guiGraphics, resultCraftable ? x - 1 : x, resultCraftable ? y - 1 : y);
				BlueprintOverlayRenderer.drawItemStack(guiGraphics, mc, x, y, result, null);
				if (i == MouseScroll.index - 1) selectedX = x;
				x += 21;
			}
			if (selectedX != 0)
				guiGraphics.blitSprite(ResourceLocation.withDefaultNamespace("hud/hotbar_selection"), selectedX - 1, y - 1, 24, 23);
			OverlayRenderer.renderItemStack(guiGraphics, results.get(MouseScroll.index - 1));
		}
		RenderSystem.disableBlend();
	}
}
