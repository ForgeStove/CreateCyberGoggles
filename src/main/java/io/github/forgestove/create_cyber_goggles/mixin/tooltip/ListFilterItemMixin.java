package io.github.forgestove.create_cyber_goggles.mixin.tooltip;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.logistics.filter.ListFilterItem;
import com.simibubi.create.foundation.mixin.accessor.ItemStackHandlerAccessor;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.util.CCGCreateLang;
import io.github.forgestove.create_cyber_goggles.util.contract.Self;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
@Mixin(ListFilterItem.class)
public abstract class ListFilterItemMixin implements Self<ListFilterItem> {
	@Inject(method = "makeSummary", at = @At("HEAD"), cancellable = true)
	private void makeSummary(ItemStack filter, CallbackInfoReturnable<List<Component>> cir) {
		if (!CCG.config.tooltip.listFilter) return;
		ItemStackHandler filterItems = thiz().getFilterItemHandler(filter);
		var accessor = (ItemStackHandlerAccessor) filterItems;
		NonNullList<ItemStack> stacks = accessor.create$getStacks();
		boolean allEmpty = stacks.stream().allMatch(ItemStack::isEmpty);
		if (allEmpty) {
			cir.setReturnValue(Collections.emptyList());
			return;
		}
		Boolean blacklist = filter.getOrDefault(AllDataComponents.FILTER_ITEMS_BLACKLIST, false);
		List<Component> list = new ArrayList<>();
		list.add((
			blacklist ? CCGCreateLang.translateDirect("gui.filter.deny_list") : CCGCreateLang.translateDirect("gui.filter.allow_list")
		).withStyle(ChatFormatting.GOLD));
		Boolean respectNBT = filter.getOrDefault(AllDataComponents.FILTER_ITEMS_RESPECT_NBT, false);
		list.add((respectNBT
			? CCGCreateLang.translateDirect("gui.filter.respect_data")
			: CCGCreateLang.translateDirect("gui.filter.ignore_data")
		).withStyle(ChatFormatting.GOLD));
		cir.setReturnValue(list);
	}
}
