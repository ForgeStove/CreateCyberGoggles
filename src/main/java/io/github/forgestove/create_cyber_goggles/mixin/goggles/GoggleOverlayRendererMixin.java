package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.simibubi.create.api.equipment.goggles.*;
import com.simibubi.create.content.equipment.goggles.GoggleOverlayRenderer;
import com.simibubi.create.infrastructure.config.AllConfigs;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.event.*;
import io.github.forgestove.create_cyber_goggles.core.util.TooltipComponentUtil;
import net.createmod.catnip.gui.element.GuiGameElement.GuiRenderBuilder;
import net.createmod.catnip.gui.element.RenderElement;
import net.createmod.catnip.outliner.Outliner.OutlineEntry;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
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
	@Unique private static final int ccg$FADE_TICKS = 24;
	@Unique private static HitResult ccg$lastHitResult;
	@Unique private static int ccg$fadeTicks;
	/** 本帧是否用旧目标顶替了 hitResult。与 {@link #ccg$fadeTicks} 区分：倒计时在"正看向有信息的目标"时也会被压满 */
	@Unique private static boolean ccg$fading;
	@Unique private static int ccg$Offset;
	@Inject(method = "renderOverlay", at = @At("HEAD"), cancellable = true)
	private static void renderOverlay(CallbackInfo ci) {
		OverlayManager.clearFrame(); // 帧开始清空已占用区域
		// 计算 Goggle 让开偏移：需在世界场景同样生效，故先算再判断是否禁用（此前世界场景提前 return 导致 ccg$Offset 恒 0）
		var window = mc.getWindow();
		var goggleY = window.getGuiScaledHeight() / 2 + AllConfigs.client().overlayOffsetY.get();
		ccg$Offset = OverlayManager.prevUpperBottom > 0 ? Math.max(0, OverlayManager.prevUpperBottom + 22 - goggleY) : 0;
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
		y += ccg$Offset;
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
	@WrapOperation(
		method = "renderOverlay", at = @At(
		value = "INVOKE",
		target = "Lnet/createmod/catnip/gui/element/GuiGameElement$GuiRenderBuilder;at(FFF)"
			+ "Lnet/createmod/catnip/gui/element/RenderElement;"
	)
	)
	private static RenderElement adjustIcon(GuiRenderBuilder instance, float x, float y, float z, Operation<GuiRenderBuilder> original) {
		return original.call(instance, x, y + ccg$Offset, z);
	}
	@WrapOperation(
		method = "renderOverlay", at = @At(
		value = "INVOKE", target = "Lnet/createmod/catnip/gui/element/RenderElement;render(Lnet/minecraft/client/gui/GuiGraphics;)V"
	)
	)
	private static void wrapRenderElement(RenderElement instance, GuiGraphics gui, Operation<Void> original) {
		original.call(instance, gui);
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
		if (ccg$hasInfo(original)) { // 当前目标有覆盖层信息：记住它，供失焦后继续渲染
			ccg$lastHitResult = original;
			ccg$fadeTicks = ccg$FADE_TICKS;
			ccg$fading = false;
			return original; // 淡入仍交给 Create 的 hoverTicks 驱动
		}
		if (ccg$lastHitResult != null && ccg$fadeTicks > 0) {
			if (!ccg$fading) // 刚失焦：从当前淡化进度接续，避免淡出开始时突跳到全不透明
				ccg$fadeTicks = Math.min(ccg$fadeTicks, GoggleOverlayRenderer.hoverTicks);
			else ccg$fadeTicks = Math.max(0, ccg$fadeTicks - 3);
			if (ccg$fadeTicks > 0) {
				ccg$fading = true;
				return ccg$lastHitResult;
			}
			GoggleOverlayRenderer.hoverTicks = 0; // 淡出结束，交回真实目标并从 0 重新淡入
		}
		ccg$lastHitResult = null;
		ccg$fading = false;
		return original;
	}
	@Unique
	private static boolean ccg$hasInfo(HitResult hit) {
		if (hit instanceof BlockHitResult bhr && bhr.getType() == Type.BLOCK) {
			if (mc.level == null) return false;
			var blockPos = bhr.getBlockPos();
			return mc.level.getBlockEntity(blockPos) instanceof IHaveCustomOverlayIcon || mc.level.getBlockState(blockPos)
				.getBlock() instanceof IProxyHoveringInformation;
		}
		//放置在地面的实现目镜信息接口的实体
		return hit instanceof EntityHitResult ehr && ehr.getEntity() instanceof IHaveCustomOverlayIcon;
	}
	@ModifyExpressionValue(method = "renderOverlay", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", ordinal = 1))
	private static boolean suppressEmptyCheckDuringFadeOut(boolean original) {
		return !CCG.config.goggles.enableFadeOut ? original : original && !ccg$isFadingOut();
	}
	/**
	 * 淡出状态自持：不能用 Create 的 {@code hoverTicks} 判断（会被第三方注入扰动，普通方块上也会 +1），
	 * 也不能只看倒计时是否被压满（正看向有信息的目标时同样是满的）
	 */
	@Unique
	private static boolean ccg$isFadingOut() {
		return CCG.config.goggles.enableFadeOut && ccg$fading;
	}
	@WrapOperation(method = "renderOverlay", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F"))
	private static float wrapFadeClamp(float value, float min, float max, Operation<Float> original) {
		if (!CCG.config.goggles.enableFadeOut) return original.call(value, min, max);
		if (ccg$isFadingOut()) {
			// 淡出期间 Create 看到的仍是旧目标，它的 hoverTicks 会一直涨，淡化值改由我们的倒计时直接给出
			GoggleOverlayRenderer.hoverTicks = ccg$fadeTicks;
			return Mth.clamp(ccg$fadeTicks / (float) ccg$FADE_TICKS, min, max);
		}
		if (GoggleOverlayRenderer.hoverTicks > ccg$FADE_TICKS) GoggleOverlayRenderer.hoverTicks = ccg$FADE_TICKS;
		return original.call(value, min, max);
	}
}
