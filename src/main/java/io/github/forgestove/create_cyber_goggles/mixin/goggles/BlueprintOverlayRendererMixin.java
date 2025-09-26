package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.content.equipment.blueprint.BlueprintOverlayRenderer;
import com.simibubi.create.content.logistics.tableCloth.BlueprintOverlayShopContext;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.event.*;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
@Mixin(value = BlueprintOverlayRenderer.class, remap = false)
public abstract class BlueprintOverlayRendererMixin {
	@Unique private static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
	@Shadow static List<ItemStack> results;
	@Shadow static boolean resultCraftable;
	@Shadow static BlueprintOverlayShopContext shopContext;
	@Inject(method = "renderOverlay", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", ordinal = 1), cancellable = true)
	private static void renderOverlay(
		ForgeGui gui,
		GuiGraphics guiGraphics,
		float partialTicks,
		int width,
		int height,
		CallbackInfo callbackInfo,
		@Local(name = "x") int x,
		@Local(name = "y") int y,
		@Local(name = "invalidShop") boolean invalidShop
	) {
		if (!CCG.CONFIG.goggles.betterStoreInfo) return;
		callbackInfo.cancel();
		if (results.isEmpty()) {
			guiGraphics.blit(WIDGETS_LOCATION, x, y, 24, 23, 22, 22);
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
				BlueprintOverlayRenderer.drawItemStack(guiGraphics, Minecraft.getInstance(), x, y, result, null);
				if (i == MouseScroll.index - 1) selectedX = x;
				x += 21;
			}
			if (selectedX != 0) guiGraphics.blit(WIDGETS_LOCATION, selectedX - 1, y - 1, 0, 22, 23, 23);
			OverlayRenderer.renderItemStack(guiGraphics, results.get(MouseScroll.index - 1));
		}
		RenderSystem.disableBlend();
	}
}
