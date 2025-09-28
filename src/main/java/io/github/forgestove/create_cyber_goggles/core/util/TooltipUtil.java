package io.github.forgestove.create_cyber_goggles.core.util;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.equipment.armor.BacktankUtil;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity.FuelType;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity.State;
import com.simibubi.create.foundation.utility.CreateLang;
import io.github.forgestove.create_cyber_goggles.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
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
		CCGLang.translate("tooltip.leftTime")
			.style(ChatFormatting.GRAY)
			.add(CCGLang.text(isCreative ? "∞" : String.valueOf(remainingBurnTime / 20)).style(format))
			.space()
			.add(CCGLang.translate("tooltip.seconds").style(ChatFormatting.GRAY))
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
			CreateLang.translate("gui.schematicannon.shotsRemaining", CCGLang.number(shotsLeft).style(ChatFormatting.BLUE))
				.style(ChatFormatting.GRAY)
				.forGoggles(tooltip);
			if (shotsLeftWithItems != shotsLeft) CreateLang.translate(
					"gui.schematicannon.shotsRemainingWithBackup",
					CCGLang.number(shotsLeftWithItems).style(ChatFormatting.BLUE)
				)
				.style(ChatFormatting.GRAY)
				.forGoggles(tooltip);
		}
		if (!sbe.state.equals(State.RUNNING)) return;
		var progress = sbe.schematicProgress * 100;
		CCGLang.translate("tooltip.printProgress").forGoggles(tooltip);
		CCGLang.text(String.format("%d/%d", sbe.blocksPlaced, sbe.blocksToPlace))
			.text(String.format(" (%.2f%%)", progress))
			.color(Color.HSBtoRGB(sbe.schematicProgress * 0.33f, 1.0f, 1.0f))
			.forGoggles(tooltip);
		var totalBars = 30;
		var filledBars = (int) (sbe.schematicProgress * totalBars);
		CCGLang.text(ChatFormatting.GREEN, "|".repeat(filledBars))
			.add(CCGLang.text(ChatFormatting.GRAY, "|".repeat(totalBars - filledBars)))
			.forGoggles(tooltip);
	}
	public static void addBacktankTooltip(List<Component> tooltip, int capacityEnchantLevel, int airLevel, float speed, int leftTick) {
		if (!CCG.CONFIG.goggles.enhancedInfo) return;
		CreateLang.translate("gui.goggles.fluid_container").forGoggles(tooltip);
		CreateLang.translate("gui.goggles.fluid_container.capacity")
			.style(ChatFormatting.GRAY)
			.add(CCGLang.number(airLevel).style(ChatFormatting.GOLD))
			.text(ChatFormatting.GRAY, " / ")
			.add(CCGLang.number(BacktankUtil.maxAir(capacityEnchantLevel)).style(ChatFormatting.DARK_GRAY))
			.forGoggles(tooltip);
		if (speed == 0 || leftTick == 0) return;
		CCGLang.translate("tooltip.leftTime")
			.style(ChatFormatting.GRAY)
			.add(CCGLang.number(leftTick / 20).style(ChatFormatting.GOLD))
			.space()
			.add(CCGLang.translate("tooltip.seconds").style(ChatFormatting.GRAY))
			.forGoggles(tooltip);
	}
}
