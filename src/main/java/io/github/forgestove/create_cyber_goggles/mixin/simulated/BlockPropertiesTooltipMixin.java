package io.github.forgestove.create_cyber_goggles.mixin.simulated;
import com.llamalad7.mixinextras.sugar.Local;
import dev.ryanhcode.sable.mixinterface.block_properties.BlockStateExtension;
import dev.simulated_team.simulated.client.BlockPropertiesTooltip;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.world.item.BlockItem;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(BlockPropertiesTooltip.class)
public abstract class BlockPropertiesTooltipMixin {
	@Shadow
	private static MutableComponent formatValue(String key, double value) {
		throw new UnsupportedOperationException();
	}
	@Inject(method = "getMassComponent", at = @At(value = "RETURN", ordinal = 0), cancellable = true)
	private static void modifyMassComponent(
		BlockStateExtension properties,
		BlockItem item,
		boolean showNumbers,
		CallbackInfoReturnable<Component> cir,
		@Local(name = "mass") double mass
	) {
		if (!CCG.config.aeronautics.alwaysShowMass) return;
		var comp = Component.translatable("create_cyber_goggles.tooltip.mass.normal");
		if (showNumbers) comp.append(formatValue("simulated.unit.mass", mass).withStyle(ChatFormatting.DARK_GRAY));
		cir.setReturnValue(comp);
	}
}
