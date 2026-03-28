package io.github.forgestove.create_cyber_goggles.mixin.provider;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.api.*;
import io.github.forgestove.create_cyber_goggles.core.util.GoggleTooltipUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
@Mixin(value = BeltBlockEntity.class, remap = false)
public abstract class BeltBlockEntityMixin extends KineticBlockEntity
	implements IHaveGoggleInformation, ItemRenderable, Self<BeltBlockEntity> {
	@Unique public final Deque<Integer> ccg$itemHistory = new ArrayDeque<>();
	@Unique public double ccg$rate;
	@Unique public int ccg$lastTotalItems;
	public BeltBlockEntityMixin(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
	}
	@Inject(method = "tick", at = @At("TAIL"))
	private void tick(CallbackInfo ci) {
		if (level == null || !level.isClientSide) return;
		if (thiz().index != 0) return;
		var currentTotalItems = 0;
		for (var ts : thiz().getInventory().getTransportedItems())
			if (ts != null && ts.stack != null) currentTotalItems += ts.stack.getCount();
		var itemsPassed = Math.max(0, ccg$lastTotalItems - currentTotalItems);
		ccg$lastTotalItems = currentTotalItems;
		ccg$itemHistory.addLast(itemsPassed);
		if (ccg$itemHistory.size() > 60) ccg$itemHistory.pollFirst();
		ccg$rate = (ccg$rate + ccg$itemHistory.stream().mapToInt(Integer::intValue).average().orElse(0) * 20) / 2;
	}
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		var sup = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
		if (!CCG.config.goggles.enhancedInfo || getSpeed() == 0) return sup;
		var controllerBE = thiz().getControllerBE();
		if (controllerBE != null) GoggleTooltipUtil.beltThroughput(tooltip, ((BeltBlockEntityMixin) (Object) controllerBE).ccg$rate);
		return sup;
	}
	@Override
	public ItemStack ccg$getItemStack() {
		var inventory = thiz().getInventory();
		if (inventory == null) return null;
		var stackAtOffset = inventory.getStackAtOffset(thiz().index);
		return stackAtOffset == null ? null : stackAtOffset.stack;
	}
}
