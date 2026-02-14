package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import com.simibubi.create.content.equipment.blueprint.BlueprintOverlayRenderer;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.TableClothUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
@Mixin(value = BlueprintOverlayRenderer.class, remap = false)
public abstract class BlueprintOverlayRendererMixin {
	@Shadow static List<ItemStack> results;
	@Inject(method = "renderOverlay", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", ordinal = 1), cancellable = true)
	private static void renderOverlay(
		ForgeGui gui,
		GuiGraphics graphics,
		float partialTicks,
		int width,
		int height,
		CallbackInfo ci,
		@Local(name = "x") int x,
		@Local(name = "y") int y
	) {
		if (!CCG.config.goggles.betterStoreInfo) return;
		ci.cancel();
		TableClothUtil.clothStoreOverlay(graphics, x, y, results);
		RenderSystem.disableBlend();
	}
	@Inject(
		method = "renderOverlay", at = @At(
		value = "FIELD",
		target = "Lcom/simibubi/create/content/equipment/blueprint/BlueprintOverlayRenderer;active:Z",
		opcode = Opcodes.GETSTATIC
	)
	)
	private static void resetTCBE(ForgeGui gui, GuiGraphics graphics, float partialTicks, int width, int height, CallbackInfo ci) {
		if (!CCG.config.goggles.betterStoreInfo) return;
		TableClothUtil.tableOverlay(graphics);
	}
}
