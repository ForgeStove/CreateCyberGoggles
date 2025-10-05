package io.github.forgestove.create_cyber_goggles.mixin.wrench;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.simibubi.create.content.contraptions.wrench.RadialWrenchMenu;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.mixin.accessor.RadialWrenchMenuAccessor;
import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static com.simibubi.create.content.contraptions.wrench.RadialWrenchMenu.BLOCK_BLACKLIST;
import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.*;
@Mixin(RadialWrenchMenu.class)
public abstract class RadialWrenchMenuMixin {
	@Unique private static final Map<Property<?>, String> PROPERTIES_BLACKLIST = new HashMap<>();
	static {
		Stream.of(
			EYE,
			VAULT_STATE,
			DISTANCE,
			SNOWY,
			HAS_BOOK,
			HAS_RECORD,
			LEVEL_CAULDRON,
			LEVEL_COMPOSTER,
			LEVEL_FLOWING,
			LEVEL_HONEY,
			LEVEL,
			LAYERS,
			BITES,
			CANDLES,
			EGGS,
			STAGE,
			PICKLES,
			MOISTURE,
			FLOWER_AMOUNT,
			BED_PART,
			AGE_1,
			AGE_2,
			AGE_3,
			AGE_4,
			AGE_5,
			AGE_7,
			AGE_15,
			AGE_25,
			NOTEBLOCK_INSTRUMENT,
			SIGNAL_FIRE,
			BlazeBurnerBlock.HEAT_LEVEL
		).forEach(property -> PROPERTIES_BLACKLIST.put(property, property.getName()));
	}
	@Inject(method = "tryCreateFor", at = @At("HEAD"), cancellable = true)
	private static void tryCreateFor(
		BlockState state,
		BlockPos pos,
		Level level,
		CallbackInfoReturnable<Optional<RadialWrenchMenu>> returnable
	) {
		if (!CCG.CONFIG.wrench.enchancedWrench) return;
		var isCreative = mc.player != null && mc.player.isCreative();
		if (!isCreative && BLOCK_BLACKLIST.contains(RegisteredObjectsHelper.getKeyOrThrow(state.getBlock()))) {
			returnable.setReturnValue(Optional.empty());
			return;
		}
		var properties = state.getProperties()
			.stream()
			.<Entry<Property<?>, String>>map(property -> Map.entry(property, property.getName()))
			.filter(entry -> isCreative || !PROPERTIES_BLACKLIST.containsKey(entry.getKey()))
			.toList();
		if (properties.isEmpty()) {
			returnable.setReturnValue(Optional.empty());
			return;
		}
		returnable.setReturnValue(Optional.of(RadialWrenchMenuAccessor.create(state, pos, level, properties)));
	}
	@WrapOperation(
		method = "renderRadialSectors", at = @At(
		value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;getLevel()Lnet/minecraft/world/level/Level;"
	)
	)
	private Level renderRadialSectors(BlockEntity instance, Operation<Level> original) {
		if (!CCG.CONFIG.wrench.enchancedWrench) return null;
		if (instance == null) return null;
		return original.call(instance);
	}
	@WrapOperation(
		method = "renderRadialSectors", at = @At(
		value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;setLevel(Lnet/minecraft/world/level/Level;)V"
	)
	)
	private void renderRadialSectors(BlockEntity instance, Level level, Operation<Void> original) {
		if (!CCG.CONFIG.wrench.enchancedWrench) return;
		if (instance == null) return;
		original.call(instance, level);
	}
}
