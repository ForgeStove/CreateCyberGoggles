package io.github.forgestove.create_cyber_goggles.core.util;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.equipment.armor.*;
import com.simibubi.create.content.kinetics.base.*;
import com.simibubi.create.content.kinetics.base.IRotate.*;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity.FuelType;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity.State;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.CreateLang;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.event.CCGKey;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.minecraft.ChatFormatting.*;
public class TooltipUtil {
	public static void kinetic(List<Component> tooltip, @NotNull KineticBlockEntity kbe, float stress, float capacity) {
		var speed = kbe.getTheoreticalSpeed();
		if (StressImpact.isEnabled()) {
			var stressAtBase = kbe.calculateStressApplied();
			if (!Mth.equal(stressAtBase, 0)) {
				CreateLang.translate("tooltip.stressImpact").style(GRAY).forGoggles(tooltip);
				CreateLang.number(stressAtBase * Math.abs(speed))
					.translate("generic.unit.stress")
					.style(AQUA)
					.space()
					.add(CreateLang.translate("gui.goggles.at_current_speed").style(DARK_GRAY))
					.forGoggles(tooltip, 1);
			}
		}
		CreateLang.translate("gui.speedometer.title").style(GRAY).forGoggles(tooltip);
		SpeedLevel.getFormattedSpeedText(speed, kbe.isOverStressed()).forGoggles(tooltip);
		if (!CCGKey.showStress.isDown()) return;
		double stressFraction = stress / (capacity == 0 ? 1 : capacity);
		CreateLang.translate("gui.stressometer.title").style(GRAY).forGoggles(tooltip);
		if (speed == 0) CreateLang.text(TooltipHelper.makeProgressBar(3, 0))
			.translate("gui.stressometer.no_rotation")
			.style(DARK_GRAY)
			.forGoggles(tooltip);
		else {
			StressImpact.getFormattedStressText(stressFraction).forGoggles(tooltip);
			CreateLang.translate("gui.stressometer.capacity").style(GRAY).forGoggles(tooltip);
			double remainingCapacity = capacity - stress;
			var su = CreateLang.translate("generic.unit.stress");
			var stressTip = CreateLang.number(remainingCapacity).add(su).style(StressImpact.of(stressFraction).getRelativeColor());
			if (remainingCapacity != capacity) stressTip.text(GRAY, " / ").add(CreateLang.number(capacity).add(su).style(DARK_GRAY));
			stressTip.forGoggles(tooltip, 1);
		}
	}
	public static void generatingKinetic(List<Component> tooltip, @NotNull GeneratingKineticBlockEntity gkbe) {
		var stressBase = gkbe.calculateAddedStressCapacity();
		if (!Mth.equal(stressBase, 0)) {
			CreateLang.translate("gui.goggles.generator_stats").forGoggles(tooltip);
			CreateLang.translate("tooltip.capacityProvided").style(GRAY).forGoggles(tooltip);
			var speed = gkbe.getTheoreticalSpeed();
			var generatedSpeed = gkbe.getGeneratedSpeed();
			if (speed != generatedSpeed) stressBase *= generatedSpeed / speed;
			CreateLang.number(Math.abs(stressBase * speed))
				.translate("generic.unit.stress")
				.style(AQUA)
				.space()
				.add(CreateLang.translate("gui.goggles.at_current_speed").style(DARK_GRAY))
				.forGoggles(tooltip, 1);
		}
	}
	public static boolean fan(List<Component> tooltip, boolean pushing, float range, int divide) {
		if (range == 0) return false;
		CCGLang.translate("tooltip.windState").forGoggles(tooltip);
		CCGLang.number(range / divide)
			.space()
			.translate(pushing ? "tooltip.pushRange" : "tooltip.pullRange")
			.color(pushing ? CCG.CONFIG.outlineRenderer.windPushColor : CCG.CONFIG.outlineRenderer.windPullColor)
			.forGoggles(tooltip);
		return true;
	}
	public static boolean burner(List<Component> tooltip, int remainingBurnTime, boolean isCreative, FuelType activeFuel) {
		if (remainingBurnTime == 0 && !isCreative) return false;
		var format = switch (activeFuel) {
			case SPECIAL -> AQUA;
			case NORMAL -> GOLD;
			default -> DARK_PURPLE;
		};
		CCGLang.translate("tooltip.burnerState").forGoggles(tooltip);
		CCGLang.translate(GRAY, "tooltip.leftTime")
			.add(CCGLang.text(format, isCreative ? "∞" : String.valueOf(remainingBurnTime / 20)))
			.add(CCGLang.text(" / %d ".formatted(BlazeBurnerBlockEntity.INSERTION_THRESHOLD)))
			.add(CCGLang.seconds())
			.forGoggles(tooltip);
		return true;
	}
	public static void cannon(List<Component> tooltip, @NotNull SchematicannonBlockEntity sbe) {
		CCGLang.translate("tooltip.cannonState").forGoggles(tooltip);
		CreateLang.translate("schematicannon.status." + sbe.statusMsg).style(GOLD).forGoggles(tooltip);
		var shotsLeft = sbe.remainingFuel;
		var shotsLeftWithItems = shotsLeft + sbe.inventory.getStackInSlot(4).getCount() * sbe.getShotsPerGunpowder();
		if (sbe.hasCreativeCrate) {
			CreateLang.translate("gui.schematicannon.gunpowderLevel", "" + 100).forGoggles(tooltip);
			CCGLang.text("(").add(AllBlocks.CREATIVE_CRATE.get().getName()).text(")").style(DARK_PURPLE).forGoggles(tooltip);
		} else {
			var fillPercent = (int) (shotsLeft / (float) sbe.getShotsPerGunpowder() * 100);
			CreateLang.translate("gui.schematicannon.gunpowderLevel", fillPercent).forGoggles(tooltip);
			CreateLang.translate("gui.schematicannon.shotsRemaining", CCGLang.number(BLUE, shotsLeft)).style(GRAY).forGoggles(tooltip);
			if (shotsLeftWithItems != shotsLeft)
				CreateLang.translate("gui.schematicannon.shotsRemainingWithBackup", CCGLang.number(BLUE, shotsLeftWithItems))
					.style(GRAY)
					.forGoggles(tooltip);
		}
		if (!sbe.state.equals(State.RUNNING)) return;
		CCGLang.translate("tooltip.printProgress").forGoggles(tooltip);
		CCGLang.fraction(sbe.blocksPlaced, sbe.blocksToPlace).forGoggles(tooltip);
		CCGLang.progress(sbe.schematicProgress, 20).forGoggles(tooltip);
	}
	public static void backtank(List<Component> tooltip, BacktankBlockEntity bbe, int capacityEnchantLevel, int leftTick) {
		if (!CCG.CONFIG.goggles.enhancedInfo) return;
		CreateLang.translate("gui.goggles.fluid_container").forGoggles(tooltip);
		CreateLang.translate("gui.goggles.fluid_container.capacity")
			.style(GRAY)
			.add(CCGLang.fraction(bbe.airLevel, BacktankUtil.maxAir(capacityEnchantLevel)))
			.forGoggles(tooltip);
		if (bbe.getSpeed() == 0 || leftTick == 0) return;
		CCGLang.translate("tooltip.leftTime")
			.style(GRAY)
			.add(CCGLang.number(GOLD, leftTick / 20))
			.space()
			.add(CCGLang.seconds().style(GRAY))
			.forGoggles(tooltip);
	}
	public static void beltThroughput(List<Component> tooltip, int itemsPerSecond) {
		if (itemsPerSecond < 0.5) return;
		CCGLang.translate("tooltip.beltThroughput").style(GRAY).forGoggles(tooltip);
		CCGLang.number(itemsPerSecond)
			.style(GOLD)
			.add(CCGLang.text(" / ").style(DARK_GRAY).add(CCGLang.seconds().style(DARK_GRAY)))
			.forGoggles(tooltip, 1);
	}
}
