package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.content.equipment.blueprint.BlueprintOverlayRenderer;
import com.simibubi.create.content.logistics.tableCloth.ShoppingListItem.ShoppingList;
import com.simibubi.create.content.logistics.tableCloth.TableClothBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.TableClothUtil;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
@Mixin(BlueprintOverlayRenderer.class)
public abstract class BlueprintOverlayRendererMixin {
	@Shadow static List<ItemStack> results;
	@Shadow static boolean active;
	@Unique private static TableClothBlockEntity ccg$tcbe;
	@Inject(method = "renderOverlay", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", ordinal = 1), cancellable = true)
	private static void renderOverlay(
		GuiGraphics graphics,
		DeltaTracker deltaTracker,
		CallbackInfo ci,
		@Local(name = "x") int x,
		@Local(name = "y") int y,
		@Local(name = "invalidShop") boolean invalidShop
	) {
		if (!CCG.CONFIG.goggles.betterStoreInfo) return;
		ci.cancel();
		TableClothUtil.clothStoreOverlay(graphics, x, y, results, ccg$tcbe);
		RenderSystem.disableBlend();
	}
	@Inject(
		method = "renderOverlay",
		at = @At(value = "FIELD", target = "Lcom/simibubi/create/content/equipment/blueprint/BlueprintOverlayRenderer;active:Z")
	)
	private static void resetTCBE(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		if (!CCG.CONFIG.goggles.betterStoreInfo) return;
		if (active) return;
		TableClothUtil.tableOverlay(graphics);
	}
	@Inject(method = "displayClothShop", at = @At("HEAD"))
	private static void displayClothShop(TableClothBlockEntity tcbe, int alreadyPurchased, ShoppingList list, CallbackInfo ci) {
		if (!CCG.CONFIG.goggles.betterStoreInfo) return;
		ccg$tcbe = tcbe;
	}
}
