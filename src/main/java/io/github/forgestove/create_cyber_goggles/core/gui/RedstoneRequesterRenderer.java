package io.github.forgestove.create_cyber_goggles.core.gui;
import com.zurrtum.create.content.logistics.redstoneRequester.RedstoneRequesterBlockItem;
import com.zurrtum.create.content.logistics.tableCloth.TableClothBlockItem;
import com.zurrtum.create.infrastructure.component.PackageOrderWithCrafts;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
public final class RedstoneRequesterRenderer extends AbstractItemGridRenderer {
	@Override
	public boolean supports(ItemStack stack) {
		return CCG.config.tooltip.redstoneRequester && (
			stack.getItem() instanceof RedstoneRequesterBlockItem || stack.getItem() instanceof TableClothBlockItem
		);
	}
	@Override
	public @Nullable OverlayData buildItemGrid(ItemStack stack) {
		var beData = stack.getComponents().get(DataComponents.CUSTOM_DATA);
		if (beData == null || beData.isEmpty() || !beData.copyTag().contains("EncodedRequest") || mc.level == null) return null;
		var encodedRequestTag = beData.copyTag().getCompoundOrEmpty("EncodedRequest");
		// In Fabric Create Fly, use the codec directly
		var encodedRequest = PackageOrderWithCrafts.CODEC.parse(
				mc.level.registryAccess().createSerializationContext(NbtOps.INSTANCE),
				encodedRequestTag
			)
			.resultOrPartial(_ -> {})
			.orElse(PackageOrderWithCrafts.empty());
		if (encodedRequest.isEmpty()) return null;
		var items = new ArrayList<ItemStack>();
		encodedRequest.stacks().forEach(bigStack -> items.add(bigStack.stack.copyWithCount(bigStack.count)));
		if (!items.isEmpty()) return new OverlayData(items, 3);
		return null;
	}
}
