package io.github.forgestove.create_cyber_goggles.mixin.goggles;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.filter.*;
import com.simibubi.create.content.logistics.filter.FilterItem.FilterType;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute.ItemAttributeEntry;
import com.simibubi.create.foundation.utility.CreateLang;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

import static com.simibubi.create.content.logistics.filter.FilterItem.getFilterItems;
@Mixin(FilterItem.class)
public abstract class FilterItemMixin {
	@Shadow public FilterType type;
	@Inject(method = "makeSummary", at = @At("HEAD"), cancellable = true)
	private void makeSummary(ItemStack filter, CallbackInfoReturnable<List<Component>> returnable) {
		if (!CCG.CONFIG.goggles.enhancedInfo) return;
		List<Component> list = new ArrayList<>();
		if (filter.isComponentsPatchEmpty()) {
			returnable.setReturnValue(list);
			return;
		}
		if (type == FilterType.REGULAR) {
			var filterItems = getFilterItems(filter);
			boolean blacklist = filter.getOrDefault(AllDataComponents.FILTER_ITEMS_BLACKLIST, false);
			list.add((
				blacklist ? CreateLang.translateDirect("gui.filter.deny_list") : CreateLang.translateDirect("gui.filter.allow_list")
			).withStyle(ChatFormatting.GOLD));
			var count = 0;
			for (var i = 0; i < filterItems.getSlots(); i++) {
				var filterStack = filterItems.getStackInSlot(i);
				if (filterStack.isEmpty()) continue;
				list.add(Component.literal("- ").append(filterStack.getHoverName()).withStyle(ChatFormatting.GRAY));
				count++;
			}
			if (count == 0) {
				returnable.setReturnValue(Collections.emptyList());
				return;
			}
		}
		if (type == FilterType.ATTRIBUTE) {
			var whitelistMode = filter.get(AllDataComponents.ATTRIBUTE_FILTER_WHITELIST_MODE);
			list.add((
				whitelistMode == AttributeFilterWhitelistMode.WHITELIST_CONJ
					? CreateLang.translateDirect("gui.attribute_filter.allow_list_conjunctive")
					: whitelistMode == AttributeFilterWhitelistMode.WHITELIST_DISJ ? CreateLang.translateDirect(
						"gui.attribute_filter.allow_list_disjunctive") : CreateLang.translateDirect("gui.attribute_filter.deny_list")
			).withStyle(ChatFormatting.GOLD));
			var count = 0;
			for (var attributeEntry : filter.<List<ItemAttributeEntry>>getOrDefault(
				AllDataComponents.ATTRIBUTE_FILTER_MATCHED_ATTRIBUTES,
				new ArrayList<>()
			)) {
				var attribute = attributeEntry.attribute();
				if (attribute == null) continue;
				list.add(Component.literal("- ").append(attribute.format(attributeEntry.inverted())));
				count++;
			}
			if (count == 0) {
				returnable.setReturnValue(Collections.emptyList());
				return;
			}
		}
		if (type == FilterType.PACKAGE) {
			var address = PackageItem.getAddress(filter);
			if (!address.isBlank()) list.add(CreateLang.text("-> ")
				.style(ChatFormatting.GRAY)
				.add(CreateLang.text(address).style(ChatFormatting.GOLD))
				.component());
		}
		returnable.setReturnValue(list);
	}
}
