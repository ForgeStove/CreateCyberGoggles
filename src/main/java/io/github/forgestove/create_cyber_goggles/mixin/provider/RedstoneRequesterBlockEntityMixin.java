package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterBlockEntity;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.*;

import java.util.List;
@Mixin(RedstoneRequesterBlockEntity.class)
public abstract class RedstoneRequesterBlockEntityMixin implements IHaveGoggleInformation, Self<DepotBlockEntity> {
	@Shadow public PackageOrderWithCrafts encodedRequest;
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		return GoggleTooltipUtil.redstoneRequester(tooltip, encodedRequest.stacks());
	}
}
