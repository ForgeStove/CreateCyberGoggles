package io.github.forgestove.create_cyber_goggles.core.util.contract;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity.FuelType;
public interface BurnerTooltipData {
	int ccg$getRemainingBurnTime();
	FuelType ccg$getActiveFuel();
}
