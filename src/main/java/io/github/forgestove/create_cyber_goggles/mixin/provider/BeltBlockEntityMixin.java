package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.transport.*;
import io.github.forgestove.create_cyber_goggles.api.*;
import io.github.forgestove.create_cyber_goggles.core.factory.BeltThroughput;
import io.github.forgestove.create_cyber_goggles.core.util.contract.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
@Mixin(BeltBlockEntity.class)
public abstract class BeltBlockEntityMixin extends KineticBlockEntity
	implements IHaveGoggleInformation, ItemRenderable, BeltTooltipData, Self<BeltBlockEntity> {
	@Unique private final BeltThroughput ccg$throughput = new BeltThroughput();
	public BeltBlockEntityMixin(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
	}
	@Inject(method = "tick", at = @At("TAIL"))
	private void tick(CallbackInfo ci) {
		if (level == null || !level.isClientSide) return;
		if (thiz().index != 0) return;
		var totalItems = 0;
		for (TransportedItemStack tis : thiz().getInventory().getTransportedItems())
			if (tis != null && tis.stack != null) totalItems += tis.stack.getCount();
		ccg$throughput.tick(totalItems);
	}
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		return GoggleTooltip.dispatch(tooltip, thiz(), isPlayerSneaking, () -> super.addToGoggleTooltip(tooltip, isPlayerSneaking));
	}
	@Override
	public double ccg$getRate() {
		return ccg$throughput.rate();
	}
	@Override
	public ItemStack ccg$getItemStack() {
		BeltInventory inventory = thiz().getInventory();
		if (inventory == null) return null;
		TransportedItemStack stackAtOffset = inventory.getStackAtOffset(thiz().index);
		return stackAtOffset == null ? null : stackAtOffset.stack;
	}
}
