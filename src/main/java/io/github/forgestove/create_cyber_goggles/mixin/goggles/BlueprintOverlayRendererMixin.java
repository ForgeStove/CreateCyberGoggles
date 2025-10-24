package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.content.equipment.blueprint.BlueprintOverlayRenderer;
import com.simibubi.create.content.logistics.tableCloth.*;
import com.simibubi.create.content.logistics.tableCloth.ShoppingListItem.ShoppingList;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.*;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
@Mixin(value = BlueprintOverlayRenderer.class, remap = false)
public abstract class BlueprintOverlayRendererMixin {
	@Shadow static List<ItemStack> results;
	@Shadow static boolean resultCraftable;
	@Shadow static BlueprintOverlayShopContext shopContext;
	@Unique private static TableClothBlockEntity ccg$tcbe;
	@Inject(method = "renderOverlay", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", ordinal = 1), cancellable = true)
	private static void renderOverlay(
		ForgeGui gui,
		GuiGraphics graphics,
		float partialTicks,
		int width,
		int height,
		CallbackInfo ci,
		@Local(name = "x") int x,
		@Local(name = "y") int y,
		@Local(name = "invalidShop") boolean invalidShop
	) {
		if (!CCG.CONFIG.goggles.betterStoreInfo) return;
		ci.cancel();
		OverlayUtil.clothStoreOverlay(graphics, x, y, invalidShop, results, resultCraftable, ccg$tcbe, shopContext);
		RenderSystem.disableBlend();
	}
	@Inject(method = "displayClothShop", at = @At("HEAD"))
	private static void displayClothShop(TableClothBlockEntity tcbe, int alreadyPurchased, ShoppingList list, CallbackInfo ci) {
		if (!CCG.CONFIG.goggles.betterStoreInfo) return;
		ccg$tcbe = tcbe;
	}
}
