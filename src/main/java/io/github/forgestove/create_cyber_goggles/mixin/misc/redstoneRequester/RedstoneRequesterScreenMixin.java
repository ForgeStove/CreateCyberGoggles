package io.github.forgestove.create_cyber_goggles.mixin.misc.redstoneRequester;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.logistics.redstoneRequester.*;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.foundation.util.AmountUtil;
import net.createmod.catnip.platform.services.NetworkHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

import java.util.List;

import static io.github.forgestove.create_cyber_goggles.foundation.util.CCGUtil.getModifiedScrollAmount;
@Mixin(RedstoneRequesterScreen.class)
public abstract class RedstoneRequesterScreenMixin extends AbstractSimiContainerScreen<RedstoneRequesterMenu> {
	public RedstoneRequesterScreenMixin(RedstoneRequesterMenu container, Inventory inv, Component title) {
		super(container, inv, title);
	}
	@SuppressWarnings("unchecked")
	@WrapOperation(
		method = "mouseScrolled", at = @At(
		value = "INVOKE", target = "Ljava/util/List;set(ILjava/lang/Object;)Ljava/lang/Object;"
	)
	)
	public <E> E mouseScrolled(List<Integer> instance, int i, E e, Operation<E> original, @Local(name = "scrollY") double scrollY) {
		if (!CCG.config.misc.removeRequestLimit) return original.call(instance, i, e);
		Integer current = instance.get(i);
		int amount = getModifiedScrollAmount();
		// 值恰为 1 时修正步进：shift +63 / ctrl +9
		if (current == 1 && amount > 1) amount--;
		return (E) instance.set(i, Mth.clamp(current + (int) Math.signum(scrollY) * amount, 1, Integer.MAX_VALUE));
	}
	@ModifyArg(
		method = "renderForeground", at = @At(
		value = "INVOKE",
		target = "Lnet/minecraft/client/gui/GuiGraphics;renderItemDecorations(Lnet/minecraft/client/gui/Font;"
			+ "Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V"
	)
	)
	private String abbreviateCount(String text) {
		return AmountUtil.formatItemCountIfEnabled(Integer.parseInt(text));
	}
	@WrapOperation(
		method = "removed", at = @At(
		value = "INVOKE",
		target = "Lnet/createmod/catnip/platform/services/NetworkHelper;sendToServer("
			+ "Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;)V"
	)
	)
	private void skipConfigWhileMenuOpen(NetworkHelper network, CustomPacketPayload payload, Operation<Void> original) {
		if (minecraft != null && minecraft.player != null && minecraft.player.containerMenu == getMenu()) return;
		original.call(network, payload);
	}
}
