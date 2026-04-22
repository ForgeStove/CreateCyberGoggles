package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.logistics.filter.ListFilterItem;
import com.simibubi.create.foundation.utility.CreateLang;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.api.Self;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
@Mixin(ListFilterItem.class)
public abstract class ListFilterItemMixin implements Self<ListFilterItem> {
	@Inject(method = "makeSummary", at = @At("HEAD"), cancellable = true)
	private void makeSummary(ItemStack filter, CallbackInfoReturnable<List<Component>> cir) {
		if (!CCG.config.tooltip.listFilter) return;
		var blacklist = filter.getOrDefault(AllDataComponents.FILTER_ITEMS_BLACKLIST, false);
		List<Component> list = new ArrayList<>();
		list.add((
			blacklist ? CreateLang.translateDirect("gui.filter.deny_list") : CreateLang.translateDirect("gui.filter.allow_list")
		).withStyle(ChatFormatting.GOLD));
		var respectNBT = filter.getOrDefault(AllDataComponents.FILTER_ITEMS_RESPECT_NBT, false);
		list.add(CreateLang.translateDirect(respectNBT ? "gui.filter.respect_data" : "gui.filter.ignore_data")
			.withStyle(ChatFormatting.GOLD));
		cir.setReturnValue(list);
	}
}
