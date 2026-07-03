package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.simibubi.create.api.equipment.goggles.*;
import com.simibubi.create.content.equipment.goggles.GoggleOverlayRenderer;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.event.TooltipOverlay;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.createmod.catnip.outliner.Outliner.OutlineEntry;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.HitResult.Type;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
@Mixin(GoggleOverlayRenderer.class)
public abstract class GoggleOverlayRendererMixin {
	@Unique private static BlockHitResult ccg$lastBlockHitResult;
	@Inject(method = "renderOverlay", at = @At("HEAD"), cancellable = true)
	private static void renderOverlay(CallbackInfo ci) {
		if (!CCG.config.goggles.disableInScreenGoggles || isInGame()) return;
		ci.cancel();
	}
	@WrapOperation(
		method = "renderOverlay", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;getPlayerMode()Lnet/minecraft/world/level/GameType;"
	)
	)
	private static GameType wrapGameMode(MultiPlayerGameMode instance, Operation<GameType> original) {
		return CCG.config.goggles.gameMode.enableInSpectator ? null : original.call(instance);
	}
	@WrapOperation(
		method = "renderOverlay", at = @At(
		value = "INVOKE", target = "Ljava/util/Map;values()Ljava/util/Collection;"
	)
	)
	private static Collection<OutlineEntry> wrapCollection(
		Map<Object, OutlineEntry> instance,
		Operation<Collection<OutlineEntry>> original
	) {
		return CCG.config.goggles.canRenderOnValueBox ? Collections.emptyList() : original.call(instance);
	}
	@WrapOperation(method = "renderOverlay", at = @At(value = "INVOKE", target = "Ljava/util/List;remove(I)Ljava/lang/Object;"))
	private static Object wrapRemove(List<Component> instance, int i, Operation<Component> original) {
		if (instance.isEmpty()) return null;
		return original.call(instance, i);
	}
	@WrapOperation(
		method = "renderOverlay", at = @At(
		value = "INVOKE",
		target = "Lcom/simibubi/create/foundation/gui/RemovedGuiUtils;drawHoveringText(Lnet/minecraft/client/gui/GuiGraphics;"
			+ "Ljava/util/List;IIIIIIIILnet/minecraft/client/gui/Font;)V"
	)
	)
	private static void wrapTooltipRender(
		GuiGraphics gui,
		List<Component> tooltip,
		int x,
		int y,
		int screenWidth,
		int screenHeight,
		int maxWidth,
		int back,
		int top,
		int bot,
		Font font,
		Operation<Void> original
	) {
		if (CCG.config.goggles.dedupTooltipLines) tooltip = GoggleTooltipDedupUtil.dedupAdjacentLines(tooltip);
		var hasItemList = false;
		for (var line : tooltip) {
			if (!TooltipComponentUtil.hasIcon(line)) continue;
			hasItemList = true;
			break;
		}
		if (!hasItemList) {
			original.call(gui, tooltip, x, y, screenWidth, screenHeight, maxWidth, back, top, bot, font);
			return;
		}
		var components = TooltipOverlay.buildTooltipComponents(tooltip, maxWidth, false);
		if (components.isEmpty()) return;
		var tooltipWidth = components.stream().mapToInt(c -> c.getWidth(mc.font)).max().orElse(0);
		var tooltipHeight = components.stream().mapToInt(ClientTooltipComponent::getHeight).sum() + (components.size() > 1 ? 2 : 0);
		TooltipOverlay.renderTooltip(gui, ItemStack.EMPTY, components, x, y, tooltipWidth, tooltipHeight, back, top, bot);
	}
	@ModifyExpressionValue(
		method = "renderOverlay", at = @At(
		value = "FIELD",
		target = "Lnet/minecraft/client/Minecraft;hitResult:Lnet/minecraft/world/phys/HitResult;",
		opcode = Opcodes.GETFIELD
	)
	)
	private static HitResult keepHitDuringFadeOut(HitResult original) {
		if (!CCG.config.goggles.enableFadeOut) return original;
		if (original instanceof BlockHitResult bhr && bhr.getType() == Type.BLOCK && mc.level != null) {
			var be = mc.level.getBlockEntity(bhr.getBlockPos());
			if (be instanceof IHaveGoggleInformation || be instanceof IHaveHoveringInformation) {
				ccg$lastBlockHitResult = bhr;
				return original;
			}
			// 非渲染方块：淡出时返回缓存的上一个渲染方块
			return ccg$isFadingOut() && ccg$lastBlockHitResult != null ? ccg$lastBlockHitResult : original;
		}
		return ccg$isFadingOut() && ccg$lastBlockHitResult != null ? ccg$lastBlockHitResult : original;
	}
	@Unique
	private static boolean ccg$isFadingOut() {
		if (!CCG.config.goggles.enableFadeOut) return false;
		var hit = mc.hitResult;
		if (hit instanceof BlockHitResult bhr && bhr.getType() == Type.BLOCK && mc.level != null) {
			var be = mc.level.getBlockEntity(bhr.getBlockPos());
			if (be instanceof IHaveGoggleInformation || be instanceof IHaveHoveringInformation) return false; // 当前方块会渲染 tooltip
		}
		return GoggleOverlayRenderer.hoverTicks > 0; // 无有效 tooltip 且之前有渲染 → 淡出
	}
	@ModifyExpressionValue(method = "renderOverlay", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", ordinal = 1))
	private static boolean suppressEmptyCheckDuringFadeOut(boolean original) {
		return !CCG.config.goggles.enableFadeOut ? original : original && !ccg$isFadingOut();
	}
	@WrapOperation(method = "renderOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F"))
	private static float wrapFadeClamp(float value, float min, float max, Operation<Float> original) {
		if (!CCG.config.goggles.enableFadeOut) return original.call(value, min, max);
		if (ccg$isFadingOut()) GoggleOverlayRenderer.hoverTicks = Math.max(0, GoggleOverlayRenderer.hoverTicks - 3);
		else if (GoggleOverlayRenderer.hoverTicks > 24) GoggleOverlayRenderer.hoverTicks = 24;
		return original.call(value, min, max);
	}
}
