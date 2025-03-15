package com.ForgeStove.create_cyber_goggles.mixin.Goggles;
import com.ForgeStove.create_cyber_goggles.Config;
import com.ForgeStove.create_cyber_goggles.Event.KeyInputEvent;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.equipment.blueprint.BlueprintOverlayRenderer;
import com.simibubi.create.content.logistics.tableCloth.BlueprintOverlayShopContext;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.ChatFormatting;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.*;
import net.minecraft.world.item.TooltipFlag.Default;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static com.simibubi.create.content.equipment.blueprint.BlueprintOverlayRenderer.drawItemStack;
@Mixin(BlueprintOverlayRenderer.class) public abstract class BlueprintOverlayRendererMixin {
	@Shadow static boolean active;
	@Shadow static boolean empty;
	@Shadow static List<Pair<ItemStack, Boolean>> ingredients;
	@Shadow static List<ItemStack> results;
	@Shadow static boolean noOutput;
	@Shadow static boolean resultCraftable;
	@Shadow static BlueprintOverlayShopContext shopContext;
	@Unique private static int createCyberGoggles$index = 1;
	@Inject(method = "renderOverlay", at = @At("HEAD"), cancellable = true)
	private static void renderOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo callbackInfo) {
		if (!Config.EnhancedGogglesInfo.get()) return;
		callbackInfo.cancel();
		Minecraft mc = Minecraft.getInstance();
		if (mc.options.hideGui || mc.screen != null) return;
		if (!active || empty) return;
		boolean invalidShop = shopContext != null && (
				ingredients.isEmpty() || ingredients.getFirst().getFirst().isEmpty() || shopContext.stockLevel() == 0
		);
		int w = 21 * ingredients.size();
		if (!noOutput) {
			w += 21 * results.size();
			w += 30;
		}
		int x = (guiGraphics.guiWidth() - w) / 2;
		int y = guiGraphics.guiHeight() - 100;
		if (shopContext != null) {
			TooltipRenderUtil.renderTooltipBackground(
					guiGraphics,
					x - 2,
					y + 1,
					w + 4,
					19,
					0,
					0x55_000000,
					0x55_000000,
					0,
					0
			);
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
		for (Pair<ItemStack, Boolean> pair : ingredients) {
			RenderSystem.enableBlend();
			(pair.getSecond() ? AllGuiTextures.HOTSLOT_ACTIVE : AllGuiTextures.HOTSLOT).render(guiGraphics, x, y);
			ItemStack itemStack = pair.getFirst();
			String count = shopContext != null && !shopContext.checkout() || pair.getSecond()
					? null
					: ChatFormatting.GOLD.toString() + itemStack.getCount();
			drawItemStack(guiGraphics, mc, x, y, itemStack, count);
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
		if (results.isEmpty()) {
			AllGuiTextures.HOTSLOT.render(guiGraphics, x, y);
			GuiGameElement.of(Items.BARRIER).at(x + 3, y + 3).render(guiGraphics);
		} else for (ItemStack result : results) {
			AllGuiTextures slot = resultCraftable ? AllGuiTextures.HOTSLOT_SUPER_ACTIVE : AllGuiTextures.HOTSLOT;
			if (!invalidShop && shopContext != null && shopContext.stockLevel() > shopContext.purchases())
				slot = AllGuiTextures.HOTSLOT_ACTIVE;
			slot.render(guiGraphics, resultCraftable ? x - 1 : x, resultCraftable ? y - 1 : y);
			drawItemStack(guiGraphics, mc, x, y, result, null);
			x += 21;
		}
		if (shopContext == null || shopContext.checkout()) {
			RenderSystem.disableBlend();
			return;
		}
		createCyberGoggles$index += KeyInputEvent.scrollKeyboard;
		if (createCyberGoggles$index < 1) createCyberGoggles$index = results.size();
		else if (createCyberGoggles$index > results.size()) createCyberGoggles$index = 1;
		KeyInputEvent.scrollKeyboard = 0;
		ItemStack result = results.get(createCyberGoggles$index - 1);
		ClientLevel level = mc.level;
		LocalPlayer player = mc.player;
		Default tooltipFlag = mc.options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL;
		List<Component> tooltipLines = result.getTooltipLines(TooltipContext.of(level), player, tooltipFlag);
		Font font = mc.font;
		int tooltipHeight = tooltipLines.size() * font.lineHeight + 8;
		int width = guiGraphics.guiWidth() / 2;
		int height = guiGraphics.guiHeight() / 2;
		guiGraphics.renderItem(result, width + 10, height - 15);
		guiGraphics.renderItemDecorations(font, result, width + 10, height - 15);
		int mouseY = Math.max(0, height - Math.max(0, tooltipHeight - 80));
		guiGraphics.renderComponentTooltip(font, tooltipLines, width + 20, mouseY);
		RenderSystem.disableBlend();
	}
}
