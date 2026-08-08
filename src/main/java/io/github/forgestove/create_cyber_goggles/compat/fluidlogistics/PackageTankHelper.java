package io.github.forgestove.create_cyber_goggles.compat.fluidlogistics;
import io.github.forgestove.create_cyber_goggles.core.factory.CCGMods;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
public class PackageTankHelper {
	private static final ResourceLocation CFL_COMPRESSED_TANK_ID = CCGMods.fluidlogistics.rl("compressed_storage_tank");
	public static int getCFLTankAmount(ItemStack stack) {
		if (!isCFLCompressedTank(stack)) return 0;
		var handler = stack.getCapability(FluidHandler.ITEM);
		if (handler == null) return 0;
		var fluid = handler.getFluidInTank(0);
		return fluid.isEmpty() ? 0 : fluid.getAmount();
	}
	public static boolean isCFLCompressedTank(ItemStack stack) {
		if (stack.isEmpty()) return false;
		return BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(CFL_COMPRESSED_TANK_ID);
	}
}
