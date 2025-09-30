package io.github.forgestove.create_cyber_goggles.core.util;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.equipment.armor.*;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity.FuelType;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity.State;
import com.simibubi.create.foundation.utility.CreateLang;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
public class TooltipUtil {
	public static boolean addFanTooltip(List<Component> tooltip, boolean pushing, float range, int divide) {
		if (range == 0) return false;
		CCGLang.translate("tooltip.windState").forGoggles(tooltip);
		CCGLang.number(range / divide)
			.space()
			.translate(pushing ? "tooltip.pushRange" : "tooltip.pullRange")
			.color(pushing ? CCG.CONFIG.outlineRenderer.windPushColor : CCG.CONFIG.outlineRenderer.windPullColor)
			.forGoggles(tooltip);
		return true;
	}
	public static boolean addBurnerTooltip(List<Component> tooltip, int remainingBurnTime, boolean isCreative, FuelType activeFuel) {
		if (remainingBurnTime == 0 && !isCreative) return false;
		var format = switch (activeFuel) {
			case SPECIAL -> ChatFormatting.AQUA;
			case NORMAL -> ChatFormatting.GOLD;
			default -> ChatFormatting.DARK_PURPLE;
		};
		CCGLang.translate("tooltip.burnerState").forGoggles(tooltip);
		CCGLang.translate(ChatFormatting.GRAY, "tooltip.leftTime")
			.add(CCGLang.text(format, isCreative ? "∞" : String.valueOf(remainingBurnTime / 20)))
			.add(CCGLang.text(" / %d ".formatted(BlazeBurnerBlockEntity.INSERTION_THRESHOLD)))
			.add(CCGLang.seconds())
			.forGoggles(tooltip);
		return true;
	}
	public static void addCannonTooltip(List<Component> tooltip, @NotNull SchematicannonBlockEntity sbe) {
		CCGLang.translate("tooltip.cannonState").forGoggles(tooltip);
		CreateLang.translate("schematicannon.status." + sbe.statusMsg).style(ChatFormatting.GOLD).forGoggles(tooltip);
		var shotsLeft = sbe.remainingFuel;
		var shotsLeftWithItems = shotsLeft + sbe.inventory.getStackInSlot(4).getCount() * sbe.getShotsPerGunpowder();
		if (sbe.hasCreativeCrate) {
			CreateLang.translate("gui.schematicannon.gunpowderLevel", "" + 100).forGoggles(tooltip);
			CCGLang.text("(").add(AllBlocks.CREATIVE_CRATE.get().getName()).text(")").style(ChatFormatting.DARK_PURPLE).forGoggles(tooltip);
		} else {
			var fillPercent = (int) (shotsLeft / (float) sbe.getShotsPerGunpowder() * 100);
			CreateLang.translate("gui.schematicannon.gunpowderLevel", fillPercent).forGoggles(tooltip);
			CreateLang.translate("gui.schematicannon.shotsRemaining", CCGLang.number(ChatFormatting.BLUE, shotsLeft))
				.style(ChatFormatting.GRAY)
				.forGoggles(tooltip);
			if (shotsLeftWithItems != shotsLeft)
				CreateLang.translate("gui.schematicannon.shotsRemainingWithBackup", CCGLang.number(ChatFormatting.BLUE,
						shotsLeftWithItems))
					.style(ChatFormatting.GRAY)
					.forGoggles(tooltip);
		}
		if (!sbe.state.equals(State.RUNNING)) return;
		CCGLang.translate("tooltip.printProgress").forGoggles(tooltip);
		CCGLang.fraction(sbe.blocksPlaced, sbe.blocksToPlace).forGoggles(tooltip);
		CCGLang.progress(sbe.schematicProgress, 20).forGoggles(tooltip);
	}
	public static void addBacktankTooltip(List<Component> tooltip, BacktankBlockEntity bbe, int capacityEnchantLevel, int leftTick) {
		if (!CCG.CONFIG.goggles.enhancedInfo) return;
		CreateLang.translate("gui.goggles.fluid_container").forGoggles(tooltip);
		CreateLang.translate("gui.goggles.fluid_container.capacity")
			.style(ChatFormatting.GRAY)
			.add(CCGLang.fraction(bbe.airLevel, BacktankUtil.maxAir(capacityEnchantLevel)))
			.forGoggles(tooltip);
		if (bbe.getSpeed() == 0 || leftTick == 0) return;
		CCGLang.translate("tooltip.leftTime")
			.style(ChatFormatting.GRAY)
			.add(CCGLang.number(ChatFormatting.GOLD, leftTick / 20))
			.space()
			.add(CCGLang.seconds().style(ChatFormatting.GRAY))
			.forGoggles(tooltip);
	}
}
