package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.logistics.redstoneRequester.*;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.getModifiedScrollAmount;
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
		var current = instance.get(i);
		var amount = getModifiedScrollAmount();
		// 值恰为 1 时修正步进：shift +63 / ctrl +9，结果正好对齐 64 / 10
		if (current == 1 && amount > 1) amount--;
		return (E) instance.set(i, Mth.clamp(current + (int) Math.signum(scrollY) * amount, 1, Integer.MAX_VALUE));
	}
}
