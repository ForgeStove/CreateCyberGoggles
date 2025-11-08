package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.content.equipment.blueprint.BlueprintOverlayRenderer;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.TableClothUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
@Mixin(BlueprintOverlayRenderer.class)
public abstract class BlueprintOverlayRendererMixin {
	@Shadow static List<ItemStack> results;
	@Inject(method = "renderOverlay", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", ordinal = 1), cancellable = true)
	private static void renderOverlay(
		Minecraft mc,
		GuiGraphics graphics,
		CallbackInfo ci,
		@Local(name = "x") int x,
		@Local(name = "y") int y
	) {
		if (!CCG.CONFIG.goggles.betterStoreInfo) return;
		ci.cancel();
		TableClothUtil.clothStoreOverlay(graphics, x, y, results);
	}
	@Inject(
		method = "renderOverlay",
		at = @At(value = "FIELD", target = "Lcom/zurrtum/create/client/content/equipment/blueprint/BlueprintOverlayRenderer;active:Z")
	)
	private static void resetTCBE(Minecraft mc, GuiGraphics graphics, CallbackInfo ci) {
		if (!CCG.CONFIG.goggles.betterStoreInfo) return;
		TableClothUtil.tableOverlay(graphics);
	}
}
