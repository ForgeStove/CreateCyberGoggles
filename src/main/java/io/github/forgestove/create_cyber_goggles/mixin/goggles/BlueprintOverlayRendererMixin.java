package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.content.equipment.blueprint.BlueprintOverlayRenderer;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.event.TooltipOverlay;
import io.github.forgestove.create_cyber_goggles.core.util.TableClothUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.Opcodes;
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
		GuiGraphicsExtractor gui,
		CallbackInfo ci,
		@Local(name = "x") int x,
		@Local(name = "y") int y
	) {
		if (!CCG.config.goggles.betterStoreInfo) return;
		ci.cancel();
		TableClothUtil.clothStoreOverlay(gui, x, y, results);
	}
	@Inject(
		method = "renderOverlay", at = @At(
		value = "FIELD",
		target = "Lcom/zurrtum/create/client/content/equipment/blueprint/BlueprintOverlayRenderer;active:Z",
		opcode = Opcodes.GETSTATIC
	)
	)
	private static void resetTCBE(Minecraft mc, GuiGraphicsExtractor graphics, CallbackInfo ci) {
		if (!CCG.config.goggles.betterStoreInfo) return;
		TableClothUtil.tableOverlay(graphics);
	}
	@WrapOperation(
		method = "renderOverlay", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/client/gui/screens/inventory/tooltip/TooltipRenderUtil;extractTooltipBackground"
			+ "(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIIILnet/minecraft/resources/Identifier;)V"
	)
	)
	private static void renderOverlay(
		GuiGraphicsExtractor gui,
		int x,
		int y,
		int width,
		int height,
		Identifier sprite,
		Operation<Void> original
	) {
		if (!CCG.config.goggles.betterStoreInfo) {
			original.call(gui, x, y, width, height, sprite);
			return;
		}
		var theme = TooltipOverlay.getTheme();
		var back = theme.backColor().getRGB();
		var top = theme.topColor().getRGB();
		var bot = theme.botColor().getRGB();
		TooltipOverlay.renderTooltipBackground(gui, x, y, width, height, back, top, bot);
	}
}
