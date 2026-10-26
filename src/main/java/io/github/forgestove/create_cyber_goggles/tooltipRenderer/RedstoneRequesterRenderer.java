package io.github.forgestove.create_cyber_goggles.tooltipRenderer;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterBlockItem;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.content.logistics.tableCloth.TableClothBlockItem;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.api.AutoTooltipRenderer;
import net.createmod.catnip.codecs.CatnipCodecUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static io.github.forgestove.create_cyber_goggles.util.CCGUtil.mc;
@AutoTooltipRenderer
public final class RedstoneRequesterRenderer extends AbstractItemGridRenderer {
	@Override
	public boolean supports(ItemStack stack) {
		return CCG.config.tooltip.redstoneRequester && (
			stack.getItem() instanceof RedstoneRequesterBlockItem || stack.getItem() instanceof TableClothBlockItem
		);
	}
	@Override
	public @Nullable OverlayData buildItemGrid(ItemStack stack) {
		CustomData beData = stack.getComponents().get(DataComponents.BLOCK_ENTITY_DATA);
		if (beData == null || !beData.contains("EncodedRequest") || mc.level == null) return null;
		CompoundTag encodedRequestTag = beData.copyTag().getCompound("EncodedRequest");
		PackageOrderWithCrafts encodedRequest = CatnipCodecUtils.decode(
				PackageOrderWithCrafts.CODEC,
				mc.level.registryAccess(),
				encodedRequestTag
			)
			.orElse(PackageOrderWithCrafts.empty());
		if (encodedRequest.isEmpty()) return null;
		var items = new ArrayList<ItemStack>();
		encodedRequest.stacks().forEach(bigStack -> items.add(bigStack.stack.copyWithCount(bigStack.count)));
		if (!items.isEmpty()) return new OverlayData(items, items.size() > 9 ? 9 : 3);
		return null;
	}
}
