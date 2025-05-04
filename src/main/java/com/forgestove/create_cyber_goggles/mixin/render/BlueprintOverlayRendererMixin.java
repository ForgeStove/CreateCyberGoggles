package com.forgestove.create_cyber_goggles.mixin.render;
import com.forgestove.create_cyber_goggles.*;
import com.forgestove.create_cyber_goggles.content.Util;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.equipment.blueprint.BlueprintOverlayRenderer;
import com.simibubi.create.content.logistics.tableCloth.BlueprintOverlayShopContext;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.ChatFormatting;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static com.forgestove.create_cyber_goggles.content.event.MouseScroll.*;
@Mixin(BlueprintOverlayRenderer.class)
public abstract class BlueprintOverlayRendererMixin {
	@Shadow static boolean active;
	@Shadow static boolean empty;
	@Shadow static List<Pair<ItemStack, Boolean>> ingredients;
	@Shadow static List<ItemStack> results;
	@Shadow static boolean noOutput;
	@Shadow static boolean resultCraftable;
	@Shadow static BlueprintOverlayShopContext shopContext;
	@Inject(method = "renderOverlay", at = @At("HEAD"), cancellable = true)
	private static void renderOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo callbackInfo) {
		if (!CreateCyberGoggles.config.goggles.enhancedStoreRender) return;
		callbackInfo.cancel();
		var mc = Minecraft.getInstance();
		if (mc.options.hideGui || mc.screen != null) return;
		if (!active || empty) return;
		var invalidShop = shopContext != null && (
			ingredients.isEmpty() || ingredients.getFirst().getFirst().isEmpty() || shopContext.stockLevel() == 0
		);
		var w = 21 * ingredients.size();
		if (!noOutput) {
			w += 21 * results.size();
			w += 30;
		}
		var x = (guiGraphics.guiWidth() - w) / 2;
		var y = guiGraphics.guiHeight() - 100;
		if (shopContext != null) {
			TooltipRenderUtil.renderTooltipBackground(guiGraphics, x - 2, y + 1, w + 4, 19, 0, 0x55_000000, 0x55_000000, 0, 0);
			AllGuiTextures.TRADE_OVERLAY.render(guiGraphics, guiGraphics.guiWidth() / 2 - 48, y - 19);
			if (shopContext.purchases() > 0) {
				guiGraphics.renderItem(AllItems.SHOPPING_LIST.asStack(), guiGraphics.guiWidth() / 2 + 20, y - 20);
				guiGraphics.drawString(
					mc.font,
					Component.literal("x" + shopContext.purchases()),
					guiGraphics.guiWidth() / 2 + 20 + 16,
					y - 20 + 4,
					0xff_eeeeee,
					true
				);
			}
		}
		// Ingredients
		for (var pair : ingredients) {
			RenderSystem.enableBlend();
			(pair.getSecond() ? AllGuiTextures.HOTSLOT_ACTIVE : AllGuiTextures.HOTSLOT).render(guiGraphics, x, y);
			var itemStack = pair.getFirst();
			var count = shopContext != null && !shopContext.checkout() || pair.getSecond()
				? null
				: ChatFormatting.GOLD.toString() + itemStack.getCount();
			BlueprintOverlayRenderer.drawItemStack(guiGraphics, mc, x, y, itemStack, count);
			x += 21;
		}
		if (noOutput) return;
		// Arrow
		x += 5;
		RenderSystem.enableBlend();
		if (invalidShop) AllGuiTextures.HOTSLOT_ARROW_BAD.render(guiGraphics, x, y + 4);
		else AllGuiTextures.HOTSLOT_ARROW.render(guiGraphics, x, y + 4);
		x += 25;
		// Outputs
		var hotbarOffHandLeft = ResourceLocation.withDefaultNamespace("hud/hotbar_offhand_left");
		var hotbarSelection = ResourceLocation.withDefaultNamespace("hud/hotbar_selection");
		if (results.isEmpty()) {
			guiGraphics.blitSprite(hotbarOffHandLeft, x, y, 24, 23);
			GuiGameElement.of(Items.BARRIER).at(x + 3, y + 3).render(guiGraphics);
		} else {
			index += scrollDeltaY;
			scrollDeltaY = 0;
			if (index < 1) index = results.size();
			else if (index > results.size()) index = 1;
			var selectedX = 0;
			for (int i = 0, resultsSize = results.size(); i < resultsSize; i++) {
				var result = results.get(i);
				var slot = resultCraftable ? AllGuiTextures.HOTSLOT_SUPER_ACTIVE : AllGuiTextures.HOTSLOT;
				if (!invalidShop && shopContext != null && shopContext.stockLevel() > shopContext.purchases())
					slot = AllGuiTextures.HOTSLOT_ACTIVE;
				slot.render(guiGraphics, resultCraftable ? x - 1 : x, resultCraftable ? y - 1 : y);
				BlueprintOverlayRenderer.drawItemStack(guiGraphics, mc, x, y, result, null);
				if (i == index - 1) selectedX = x;
				x += 21;
			}
			if (selectedX != 0) guiGraphics.blitSprite(hotbarSelection, selectedX - 1, y - 1, 24, 23);
			Util.renderItemStack(guiGraphics, results.get(index - 1));
		}
		RenderSystem.disableBlend();
	}
}
