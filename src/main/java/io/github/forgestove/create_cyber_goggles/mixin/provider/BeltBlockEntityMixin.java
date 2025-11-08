package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
@Mixin(BeltBlockEntity.class)
public abstract class BeltBlockEntityMixin implements ItemRenderable, Rate, Self<BeltBlockEntity> {
	@Unique public final Deque<Integer> ccg$itemHistory = new ArrayDeque<>();
	@Unique public double ccg$rate;
	@Unique public int ccg$lastTotalItems;
	@Inject(method = "tick", at = @At("TAIL"))
	private void tick(CallbackInfo ci) {
		var bbe = self();
		var level = bbe.getLevel();
		if (level == null || !level.isClientSide()) return;
		if (bbe.index != 0) return;
		var currentTotalItems = 0;
		for (var tis : bbe.getInventory().getTransportedItems())
			if (tis != null && tis.stack != null) currentTotalItems += tis.stack.getCount();
		var itemsPassed = Math.max(0, ccg$lastTotalItems - currentTotalItems);
		ccg$lastTotalItems = currentTotalItems;
		ccg$itemHistory.addLast(itemsPassed);
		if (ccg$itemHistory.size() > 60) ccg$itemHistory.pollFirst();
		ccg$rate = (ccg$rate + ccg$itemHistory.stream().mapToInt(Integer::intValue).average().orElse(0) * 20) / 2;
	}
	@Override
	public double ccg$getRate() {
		var controllerBE = self().getControllerBE();
		if (!CCG.CONFIG.goggles.enhancedInfo || self().getSpeed() == 0) return 0;
		return ((BeltBlockEntityMixin) (Object) controllerBE).ccg$rate;
	}
	@Override
	public ItemStack ccg$getItemStack() {
		var inventory = self().getInventory();
		if (inventory == null) return null;
		var stackAtOffset = inventory.getStackAtOffset(self().index);
		return stackAtOffset == null ? null : stackAtOffset.stack;
	}
}
