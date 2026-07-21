package io.github.forgestove.create_cyber_goggles.core.tooltipRenderer;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterBlockItem;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.content.logistics.tableCloth.TableClothBlockItem;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.api.AutoTooltipRenderer;
import net.createmod.catnip.codecs.CatnipCodecUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
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
		var beData = stack.getComponents().get(DataComponents.BLOCK_ENTITY_DATA);
		if (beData == null || !beData.contains("EncodedRequest") || mc.level == null) return null;
		var encodedRequestTag = beData.copyTag().getCompound("EncodedRequest");
		var encodedRequest = CatnipCodecUtils.decode(PackageOrderWithCrafts.CODEC, mc.level.registryAccess(), encodedRequestTag)
			.orElse(PackageOrderWithCrafts.empty());
		if (encodedRequest.isEmpty()) return null;
		var items = new ArrayList<ItemStack>();
		encodedRequest.stacks().forEach(bigStack -> items.add(bigStack.stack.copyWithCount(bigStack.count)));
		if (!items.isEmpty()) return new OverlayData(items, 3);
		return null;
	}
}
