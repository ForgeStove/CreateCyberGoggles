package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.*;
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
public abstract class BeltBlockEntityMixin extends KineticBlockEntity implements IHaveGoggleInformation, IItemRenderable {
	@Unique private static final float ccg$SMOOTHING = 0.1f; // 平滑系数
	@Unique private final int[] ccg$itemHistory = new int[200];
	@Shadow public int index;
	@Unique private int ccg$lastItemCount;
	@Unique private int ccg$historyIndex = 0;
	@Unique private int ccg$totalItemsInWindow = 0; // 滑动窗口内的总物品数
	@Unique private float ccg$smoothedRate = 0; // 平滑后的速率
	public BeltBlockEntityMixin(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
		super(typeIn, pos, state);
	}
	@Shadow
	public abstract BeltBlockEntity getControllerBE();
	@Inject(method = "tick", at = @At("TAIL"))
	private void onTick(CallbackInfo ci) {
		if (level == null || !level.isClientSide) return;
		if (index != 0) return;
		var controllerBE = getControllerBE();
		if (controllerBE == null) return;
		var inventory = controllerBE.getInventory();
		if (inventory == null) return;
		var items = inventory.getTransportedItems();
		// 统计当前传送带上的物品总数
		var currentItemCount = 0;
		if (items != null) for (var transportedStack : items)
			if (transportedStack != null && transportedStack.stack != null && !transportedStack.stack.isEmpty())
				currentItemCount += transportedStack.stack.getCount();
		// 检测这一tick有多少物品离开了传送带
		var itemsPassedThisTick = 0;
		if (currentItemCount < ccg$lastItemCount) itemsPassedThisTick = ccg$lastItemCount - currentItemCount;
		ccg$lastItemCount = currentItemCount;
		// 更新滑动窗口：移除最旧的数据，添加新数据
		ccg$totalItemsInWindow -= ccg$itemHistory[ccg$historyIndex];
		ccg$itemHistory[ccg$historyIndex] = itemsPassedThisTick;
		ccg$totalItemsInWindow += itemsPassedThisTick;
		ccg$historyIndex = (ccg$historyIndex + 1) % 200;
		// 计算物品/秒：窗口内总物品数 / 窗口时间（10秒）
		var rawRate = ccg$totalItemsInWindow / 10.0f;
		// 消除滑动窗口滚动时的跳变
		if (ccg$smoothedRate == 0) ccg$smoothedRate = rawRate;
		else ccg$smoothedRate = ccg$smoothedRate * (1 - ccg$SMOOTHING) + rawRate * ccg$SMOOTHING;
	}
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		var add = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
		if (!CCG.CONFIG.goggles.enhancedInfo || getSpeed() == 0) return add;
		var controllerBE = getControllerBE();
		if (controllerBE != null) {
			var controllerMixin = (BeltBlockEntityMixin) (Object) controllerBE;
			TooltipUtil.beltThroughput(tooltip, (int) controllerMixin.ccg$smoothedRate);
		}
		return add;
	}
	@Override
	public ItemStack ccg$getItemStack() {
		var controllerBE = getControllerBE();
		if (controllerBE == null) return null;
		var inventory = controllerBE.getInventory();
		if (inventory == null) return null;
		var stackAtOffset = inventory.getStackAtOffset(index);
		return stackAtOffset == null ? null : stackAtOffset.stack;
	}
}
