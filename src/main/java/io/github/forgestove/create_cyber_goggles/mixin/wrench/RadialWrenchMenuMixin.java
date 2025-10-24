package io.github.forgestove.create_cyber_goggles.mixin.wrench;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.simibubi.create.content.contraptions.wrench.RadialWrenchMenu;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.WrenchMenuUtil;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.Map.Entry;

import static com.simibubi.create.content.contraptions.wrench.RadialWrenchMenu.BLOCK_BLACKLIST;
import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
@Mixin(RadialWrenchMenu.class)
public abstract class RadialWrenchMenuMixin {
	@Invoker("<init>")
	static RadialWrenchMenu init(BlockState state, BlockPos pos, Level level, List<Entry<Property<?>, String>> properties) {
		throw new AssertionError();
	}
	@Inject(method = "tryCreateFor", at = @At("HEAD"), cancellable = true, remap = false)
	private static void tryCreateFor(BlockState state, BlockPos pos, Level level, CallbackInfoReturnable<Optional<RadialWrenchMenu>> cir) {
		if (!CCG.CONFIG.wrench.enchancedRotationMenu) return;
		var isCreative = mc.player != null && mc.player.isCreative();
		if (!isCreative && BLOCK_BLACKLIST.contains(CatnipServices.REGISTRIES.getKeyOrThrow(state.getBlock()))) {
			cir.setReturnValue(Optional.empty());
			return;
		}
		var properties = state.getProperties()
			.stream()
			.<Entry<Property<?>, String>>map(property -> Map.entry(property, property.getName()))
			.filter(entry -> isCreative || !WrenchMenuUtil.PROPERTIES_BLACKLIST.contains(entry.getKey()))
			.toList();
		if (properties.isEmpty()) {
			cir.setReturnValue(Optional.empty());
			return;
		}
		cir.setReturnValue(Optional.of(init(state, pos, level, properties)));
	}
	@WrapOperation(
		method = "renderRadialSectors", at = @At(
		value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;getLevel()Lnet/minecraft/world/level/Level;"
	)
	)
	private Level fixNPE(BlockEntity instance, Operation<Level> original) {
		if (CCG.CONFIG.wrench.fixRotationMenu && instance == null) return null;
		return original.call(instance);
	}
	@WrapOperation(
		method = "renderRadialSectors", at = @At(
		value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/BlockEntity;setLevel(Lnet/minecraft/world/level/Level;)V"
	)
	)
	private void fixNPE(BlockEntity instance, Level level, Operation<Void> original) {
		if (CCG.CONFIG.wrench.fixRotationMenu && instance == null) return;
		original.call(instance, level);
	}
}
