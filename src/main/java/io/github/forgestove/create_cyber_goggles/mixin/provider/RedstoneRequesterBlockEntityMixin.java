package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.content.logistics.redstoneRequester.RedstoneRequesterBlockEntity;
import io.github.forgestove.create_cyber_goggles.core.api.Self;
import io.github.forgestove.create_cyber_goggles.core.util.GoggleTooltipUtil;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;
@Mixin(RedstoneRequesterBlockEntity.class)
public abstract class RedstoneRequesterBlockEntityMixin implements IHaveGoggleInformation, Self<RedstoneRequesterBlockEntity> {
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		return GoggleTooltipUtil.redstoneRequester(tooltip, thiz().encodedRequest.stacks());
	}
}
