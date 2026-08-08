package io.github.forgestove.create_cyber_goggles.compat.thirst;
import cn.mlus.thirst.content.purity.WaterPurity;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
public class WaterPurityHelper {
	public static CCGLangBuilder getWaterPurity(FluidStack fluidStack) {
		try {
			if (!WaterPurity.isEnabled()
				|| !WaterPurity.hasPurity(fluidStack) && !fluidStack.getFluid().equals(Fluids.WATER)
				|| WaterPurity.getPurity(fluidStack) == -1) return null;
			int purity = WaterPurity.getPurity(fluidStack);
			var color = getPurityColor(purity);
			return CCGLang.text(WaterPurity.getPurityText(purity)).space().fluidName(fluidStack).style(color);
		} catch (Exception e) {
			return null;
		}
	}
	public static ChatFormatting getPurityColor(int purity) {
		return switch (purity) {
			case 3 -> ChatFormatting.AQUA;
			case 2 -> ChatFormatting.BLUE;
			case 0 -> ChatFormatting.DARK_GRAY;
			default -> ChatFormatting.GRAY;
		};
	}
}
