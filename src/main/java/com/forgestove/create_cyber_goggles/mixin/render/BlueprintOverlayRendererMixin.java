package com.forgestove.create_cyber_goggles.mixin.render;
import com.forgestove.create_cyber_goggles.content.config.CCGConfig;
import com.forgestove.create_cyber_goggles.content.util.*;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.content.equipment.blueprint.BlueprintOverlayRenderer;
import com.simibubi.create.content.logistics.tableCloth.BlueprintOverlayShopContext;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
@Mixin(BlueprintOverlayRenderer.class)
public abstract class BlueprintOverlayRendererMixin {
	@Unique private static final ResourceLocation HOTBAR_SELECTION = ResourceLocation.withDefaultNamespace("hud/hotbar_selection");
	@Unique private static final ResourceLocation HOTBAR_OFF_HAND_LEFT = ResourceLocation.withDefaultNamespace("hud/hotbar_offhand_left");
	@Shadow static List<ItemStack> results;
	@Shadow static boolean resultCraftable;
	@Shadow static BlueprintOverlayShopContext shopContext;
	@Inject(method = "renderOverlay", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", ordinal = 1), cancellable = true)
	private static void injectRenderOverlay(
		GuiGraphics guiGraphics,
		DeltaTracker deltaTracker,
		CallbackInfo callbackInfo,
		@Local(name = "x") int x,
		@Local(name = "y") int y,
		@Local boolean invalidShop
	) {
		if (!CCGConfig.get().goggles.enhancedStoreRender) return;
		callbackInfo.cancel();
		var mc = Minecraft.getInstance();
		if (results.isEmpty()) {
			guiGraphics.blitSprite(HOTBAR_OFF_HAND_LEFT, x, y, 24, 23);
			GuiGameElement.of(Items.BARRIER).at(x + 3, y + 3).render(guiGraphics);
		} else {
			StaticManager.index += StaticManager.scrollDeltaY;
			StaticManager.scrollDeltaY = 0;
			if (StaticManager.index < 1) StaticManager.index = results.size();
			else if (StaticManager.index > results.size()) StaticManager.index = 1;
			var selectedX = 0;
			for (var i = 0; i < results.size(); i++) {
				var result = results.get(i);
				var slot = resultCraftable ? AllGuiTextures.HOTSLOT_SUPER_ACTIVE : AllGuiTextures.HOTSLOT;
				if (!invalidShop && shopContext != null && shopContext.stockLevel() > shopContext.purchases())
					slot = AllGuiTextures.HOTSLOT_ACTIVE;
				slot.render(guiGraphics, resultCraftable ? x - 1 : x, resultCraftable ? y - 1 : y);
				BlueprintOverlayRenderer.drawItemStack(guiGraphics, mc, x, y, result, null);
				if (i == StaticManager.index - 1) selectedX = x;
				x += 21;
			}
			if (selectedX != 0) guiGraphics.blitSprite(HOTBAR_SELECTION, selectedX - 1, y - 1, 24, 23);
			Common.renderItemStack(guiGraphics, results.get(StaticManager.index - 1));
		}
		RenderSystem.disableBlend();
	}
}
