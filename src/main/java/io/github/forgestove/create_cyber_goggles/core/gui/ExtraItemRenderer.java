package io.github.forgestove.create_cyber_goggles.core.gui;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.AllTags.AllItemTags;
import com.simibubi.create.content.equipment.toolbox.ToolboxInventory;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterBlockItem;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.content.redstone.link.controller.LinkedControllerItem;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.createmod.catnip.codecs.CatnipCodecUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.Function;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
public final class ExtraItemRenderer implements TooltipOverlayRenderer {
	private static final int PAD = 4;
	private static @Nullable OverlayData packageItem(ItemStack stack) {
		if (!CCG.config.tooltip.packageItem || !(stack.getItem() instanceof PackageItem)) return null;
		if (!stack.has(AllDataComponents.PACKAGE_CONTENTS)) return null;
		var contents = PackageItem.getContents(stack);
		List<ItemStack> items = new ArrayList<>();
		for (var i = 0; i < contents.getSlots(); i++) {
			var itemstack = contents.getStackInSlot(i);
			if (itemstack.isEmpty()) continue;
			items.add(itemstack);
		}
		return items.isEmpty() ? null : new OverlayData(items, 3);
	}
	private static @Nullable OverlayData enderChest(ItemStack stack) {
		if (CCG.config.tooltip.enderChest && stack.is(Items.ENDER_CHEST)) {
			var items = EnderChestTooltipUtil.getCachedItems();
			if (!items.isEmpty()) return new OverlayData(items, 9);
		}
		return null;
	}
	private static @Nullable OverlayData toolbox(ItemStack stack) {
		if (!CCG.config.tooltip.toolbox || !AllItemTags.TOOLBOXES.matches(stack)) return null;
		var inventory = stack.getComponents().get(AllDataComponents.TOOLBOX_INVENTORY);
		if (inventory == null) return null;
		var compartments = 8;
		var stacksPerCompartment = ToolboxInventory.STACKS_PER_COMPARTMENT;
		List<ItemStack> items = new ArrayList<>();
		Set<Integer> zeroCountSlots = new HashSet<>();
		for (var compartment = 0; compartment < compartments; compartment++) {
			var baseIndex = compartment * stacksPerCompartment;
			var consolidated = ItemStack.EMPTY;
			var totalCount = 0;
			for (var offset = 0; offset < stacksPerCompartment; offset++) {
				var slotIndex = baseIndex + offset;
				if (slotIndex >= inventory.getSlots()) break;
				var slot = inventory.getStackInSlot(slotIndex);
				if (slot.isEmpty()) continue;
				if (consolidated.isEmpty()) {
					consolidated = slot.copyWithCount(1);
					totalCount = slot.getCount();
				} else if (ItemStack.isSameItemSameComponents(consolidated, slot)) totalCount += slot.getCount();
			}
			if (consolidated.isEmpty()) {
				var filter = readToolboxFilter(inventory, compartment);
				if (filter.isEmpty()) {
					items.add(ItemStack.EMPTY);
					continue;
				}
				items.add(filter.copyWithCount(1));
				zeroCountSlots.add(items.size() - 1);
				continue;
			}
			items.add(consolidated.copyWithCount(totalCount));
		}
		if (items.isEmpty()) return null;
		return new OverlayData(items, 4, zeroCountSlots);
	}
	private static ItemStack readToolboxFilter(ToolboxInventory inventory, int compartment) {
		if (mc.level == null) return ItemStack.EMPTY;
		var access = mc.level.registryAccess();
		var tag = inventory.serializeNBT(access);
		if (!tag.contains("Compartments", 9)) return ItemStack.EMPTY;
		var compartmentsTag = tag.getList("Compartments", 10);
		if (compartment < 0 || compartment >= compartmentsTag.size()) return ItemStack.EMPTY;
		var filterTag = compartmentsTag.getCompound(compartment);
		if (filterTag.isEmpty() || !filterTag.contains("id", 8)) return ItemStack.EMPTY;
		return ItemStack.parse(access, filterTag).orElse(ItemStack.EMPTY);
	}
	private static @Nullable OverlayData redstoneRequester(ItemStack stack) {
		if (!CCG.config.tooltip.redstoneRequester || !(stack.getItem() instanceof RedstoneRequesterBlockItem)) return null;
		var beData = stack.getComponents().get(DataComponents.BLOCK_ENTITY_DATA);
		if (beData == null || !beData.contains("EncodedRequest") || mc.level == null) return null;
		var encodedRequestTag = beData.copyTag().getCompound("EncodedRequest");
		var encodedRequest = CatnipCodecUtils.decode(PackageOrderWithCrafts.CODEC, mc.level.registryAccess(), encodedRequestTag)
			.orElse(PackageOrderWithCrafts.empty());
		if (encodedRequest.isEmpty()) return null;
		var items = new ArrayList<ItemStack>();
		encodedRequest.stacks().forEach(bigStack -> items.add(bigStack.stack.copyWithCount(bigStack.count)));
		if (!items.isEmpty()) return new OverlayData(items, items.size());
		return null;
	}
	private static @Nullable OverlayData linkedController(ItemStack stack) {
		if (!CCG.config.tooltip.linkedController || !(stack.getItem() instanceof LinkedControllerItem)) return null;
		var frequencyItems = LinkedControllerItem.getFrequencyItems(stack);
		var items = new ArrayList<ItemStack>(12);
		var hasAnyItem = false;
		for (var row = 0; row < 2; row++)
			for (var column = 0; column < 6; column++) {
				var slotIndex = column * 2 + row;
				var slot = frequencyItems.getStackInSlot(slotIndex);
				if (!slot.isEmpty()) hasAnyItem = true;
				items.add(slot.copyWithCount(1));
			}
		return hasAnyItem ? new OverlayData(items, 6) : null;
	}
	private static int resolveColumns(@NotNull OverlayData data) {
		return Mth.clamp(data.columns, 1, Math.max(1, data.items.size()));
	}
	@Override
	public boolean supports(ItemStack stack) {
		return buildItemGrid(stack) != null;
	}
	@Override
	public int width(ItemStack stack) {
		var data = buildItemGrid(stack);
		if (data == null) return 0;
		if (data.items != null) return resolveColumns(data) * SlotUtil.SIZE + PAD * 2;
		return 0;
	}
	@Override
	public int height(ItemStack stack) {
		var data = buildItemGrid(stack);
		if (data == null) return 0;
		if (data.items != null) {
			var columns = resolveColumns(data);
			var rows = Math.max(1, Mth.ceil((float) data.items.size() / columns));
			return rows * SlotUtil.SIZE + PAD * 2;
		}
		return 0;
	}
	@Override
	public void render(GuiGraphics graphics, ItemStack stack, int x, int y) {
		var data = buildItemGrid(stack);
		if (data == null) return;
		var color = NativeImageUtil.getColor(stack);
		var r = color.getRed() / 255F;
		var g = color.getGreen() / 255F;
		var b = color.getBlue() / 255F;
		if (data.items != null) renderItemGrid(graphics, data.items, resolveColumns(data), x, y, r, g, b, data.zeroCountSlots);
	}
	private void renderItemGrid(
		GuiGraphics graphics,
		@NotNull List<ItemStack> items,
		int columns,
		int x,
		int y,
		float r,
		float g,
		float b,
		Set<Integer> zeroCountSlots
	) {
		var rows = Math.max(1, Mth.ceil((float) items.size() / columns));
		var panelWidth = columns * SlotUtil.SIZE + PAD * 2;
		var panelHeight = rows * SlotUtil.SIZE + PAD * 2;
		var pose = graphics.pose();
		pose.pushPose();
		pose.translate(x, y, 600F);
		OverlayPanelRenderer.renderPanel(graphics, panelWidth, panelHeight, r, g, b);
		for (var i = 0; i < items.size(); i++) {
			var col = i % columns;
			var row = i / columns;
			var slotX = PAD + col * SlotUtil.SIZE;
			var slotY = PAD + row * SlotUtil.SIZE;
			OverlayPanelRenderer.renderTintedSlot(graphics, slotX, slotY, r, g, b);
			var item = items.get(i);
			graphics.renderItem(item, slotX + 1, slotY + 1);
			if (zeroCountSlots.contains(i)) graphics.renderItemDecorations(mc.font, item, slotX + 1, slotY + 1, "0");
			else graphics.renderItemDecorations(mc.font, item, slotX + 1, slotY + 1);
		}
		pose.popPose();
	}
	private OverlayData buildItemGrid(ItemStack stack) {
		for (var resolver : List.<Function<ItemStack, OverlayData>>of(
			ExtraItemRenderer::packageItem,
			ExtraItemRenderer::linkedController,
			ExtraItemRenderer::redstoneRequester,
			ExtraItemRenderer::toolbox,
			ExtraItemRenderer::enderChest
		)) {
			var data = resolver.apply(stack);
			if (data != null) return data;
		}
		return null;
	}
	public record OverlayData(List<ItemStack> items, int columns, Set<Integer> zeroCountSlots) {
		public OverlayData(List<ItemStack> items, int columns) {
			this(items, columns, Set.of());
		}
	}
}
