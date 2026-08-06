package io.github.forgestove.create_cyber_goggles.core.util;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.equipment.armor.*;
import com.simibubi.create.content.kinetics.base.*;
import com.simibubi.create.content.kinetics.base.IRotate.*;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelControllerBlockEntity;
import com.simibubi.create.content.kinetics.millstone.*;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.depot.DepotItemHandler;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity.FuelType;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity.State;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.config.AllConfigs;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.event.*;
import net.minecraft.network.chat.*;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
import static net.minecraft.ChatFormatting.*;
import static net.minecraft.network.chat.Component.translatable;
public final class GoggleTooltipUtil {
	public static void kinetic(List<Component> tooltip, @NotNull KineticBlockEntity kbe, float stress, float capacity) {
		var speed = kbe.getTheoreticalSpeed();
		var su = translatable("create.generic.unit.stress");
		if (StressImpact.isEnabled()) {
			var stressAtBase = kbe.calculateStressApplied();
			if (!Mth.equal(stressAtBase, 0)) {
				CCGLang.add(translatable("create.tooltip.stressImpact").withStyle(GRAY)).forGoggles(tooltip);
				CCGLang.number(stressAtBase * Math.abs(speed))
					.add(su)
					.style(AQUA)
					.space()
					.add(translatable("create.gui.goggles.at_current_speed").withStyle(DARK_GRAY))
					.forGoggles(tooltip, 1);
			}
		}
		CCGLang.add(translatable("create.gui.speedometer.title").withStyle(GRAY)).forGoggles(tooltip);
		SpeedLevel.getFormattedSpeedText(speed, kbe.isOverStressed()).forGoggles(tooltip);
		if (!CCGKey.showStress.isDown()) return;
		double stressFraction = stress / (capacity == 0 ? 1 : capacity);
		CCGLang.add(translatable("create.gui.stressometer.title")).style(GRAY).forGoggles(tooltip);
		if (speed == 0) {
			CCGLang.text(TooltipHelper.makeProgressBar(3, 0))
				.add(translatable("create.gui.stressometer.no_rotation"))
				.style(DARK_GRAY)
				.forGoggles(tooltip);
			return;
		}
		StressImpact.getFormattedStressText(stressFraction).forGoggles(tooltip);
		CCGLang.add(translatable("create.gui.stressometer.capacity").withStyle(GRAY)).forGoggles(tooltip);
		double remainingCapacity = capacity - stress;
		var stressTip = CCGLang.number(remainingCapacity).add(su).style(StressImpact.of(stressFraction).getRelativeColor());
		if (remainingCapacity != capacity) stressTip.text(" / ", GRAY).add(CCGLang.number(capacity).add(su).style(DARK_GRAY));
		stressTip.forGoggles(tooltip, 1);
	}
	public static void generatingKinetic(List<Component> tooltip, @NotNull GeneratingKineticBlockEntity gkbe) {
		var stressBase = gkbe.calculateAddedStressCapacity();
		if (!Mth.equal(stressBase, 0)) {
			CCGLang.add(translatable("create.gui.goggles.generator_stats")).forGoggles(tooltip);
			CCGLang.add(translatable("create.tooltip.capacityProvided").withStyle(GRAY)).forGoggles(tooltip);
			var speed = gkbe.getTheoreticalSpeed();
			var generatedSpeed = gkbe.getGeneratedSpeed();
			if (speed != generatedSpeed) stressBase *= generatedSpeed / speed;
			CCGLang.number(Math.abs(stressBase * speed))
				.add(translatable("create.generic.unit.stress"))
				.style(AQUA)
				.space()
				.add(translatable("create.gui.goggles.at_current_speed").withStyle(DARK_GRAY))
				.forGoggles(tooltip, 1);
		}
	}
	public static boolean fan(List<Component> tooltip, boolean pushing, float range) {
		if (!CCG.config.goggles.enhancedInfo || range == 0) return false;
		CCGLang.add(translatable("create_cyber_goggles.tooltip.windState")).forGoggles(tooltip);
		CCGLang.number(range)
			.space()
			.add(pushing ? translatable("create_cyber_goggles.tooltip.pushRange") : translatable("create_cyber_goggles.tooltip.pullRange"))
			.color(Outliner.getColor(pushing))
			.forGoggles(tooltip);
		return true;
	}
	public static boolean burner(List<Component> tooltip, int remainingBurnTime, boolean isCreative, FuelType activeFuel) {
		if (!CCG.config.goggles.enhancedInfo) return false;
		if (remainingBurnTime == 0 && !isCreative) return false;
		var format = switch (activeFuel) {
			case SPECIAL -> AQUA;
			case NORMAL -> GOLD;
			default -> DARK_PURPLE;
		};
		CCGLang.add(translatable("create_cyber_goggles.tooltip.burnerState")).forGoggles(tooltip);
		CCGLang.add(translatable("create_cyber_goggles.tooltip.leftTime").withStyle(GRAY))
			.text(isCreative ? "∞" : String.valueOf(remainingBurnTime / 20), format)
			.text(" / %d ".formatted(BlazeBurnerBlockEntity.INSERTION_THRESHOLD))
			.seconds()
			.forGoggles(tooltip);
		return true;
	}
	public static boolean cannon(List<Component> tooltip, @NotNull SchematicannonBlockEntity sbe) {
		if (!CCG.config.goggles.enhancedInfo) return false;
		CCGLang.add(translatable("create_cyber_goggles.tooltip.cannonState")).forGoggles(tooltip);
		CreateLang.translate("schematicannon.status." + sbe.statusMsg).style(GOLD).forGoggles(tooltip);
		if (sbe.missingItem != null) CCGLang.itemEntry(sbe.missingItem).forGoggles(tooltip);
		var shotsLeft = sbe.remainingFuel;
		var shotsLeftWithItems = shotsLeft + sbe.inventory.getStackInSlot(4).getCount() * sbe.getShotsPerGunpowder();
		if (sbe.hasCreativeCrate) {
			CCGLang.add(translatable("create.gui.schematicannon.gunpowderLevel", "" + 100)).forGoggles(tooltip);
			CCGLang.text("(").add(AllBlocks.CREATIVE_CRATE.get().getName()).text(")").style(DARK_PURPLE).forGoggles(tooltip);
		} else {
			var fillPercent = (int) (shotsLeft / (float) sbe.getShotsPerGunpowder() * 100);
			CCGLang.add(translatable("create.gui.schematicannon.gunpowderLevel", fillPercent)).forGoggles(tooltip);
			CCGLang.add(translatable(
					"create.gui.schematicannon.shotsRemaining",
					CCGLang.number(shotsLeft, BLUE).component()
				).withStyle(GRAY))
				.forGoggles(tooltip);
			if (shotsLeftWithItems != shotsLeft) CCGLang.add(translatable(
					"create.gui.schematicannon.shotsRemainingWithBackup",
					CCGLang.number(shotsLeftWithItems, BLUE).component()
				).withStyle(GRAY))
				.forGoggles(tooltip);
		}
		if (!sbe.state.equals(State.RUNNING)) return true;
		CCGLang.add(translatable("create_cyber_goggles.tooltip.printProgress")).forGoggles(tooltip);
		CCGLang.fraction(sbe.blocksPlaced, sbe.blocksToPlace).forGoggles(tooltip);
		CCGLang.progress(sbe.schematicProgress, 20).forGoggles(tooltip);
		return true;
	}
	public static boolean backtank(List<Component> tooltip, BacktankBlockEntity bbe, int capacityEnchantLevel, int leftTick) {
		if (!CCG.config.goggles.enhancedInfo) return false;
		CCGLang.add(translatable("create.gui.goggles.fluid_container")).forGoggles(tooltip);
		CCGLang.add(translatable("create.gui.goggles.fluid_container.capacity").withStyle(GRAY))
			.add(CCGLang.fraction(bbe.airLevel, BacktankUtil.maxAir(capacityEnchantLevel)).component())
			.forGoggles(tooltip);
		if (bbe.getSpeed() == 0 || leftTick == 0) return false;
		CCGLang.add(translatable("create_cyber_goggles.tooltip.leftTime").withStyle(GRAY))
			.number(leftTick / 20, GOLD)
			.space()
			.seconds(GRAY)
			.forGoggles(tooltip);
		return true;
	}
	public static void beltThroughput(List<Component> tooltip, double itemsPerSecond) {
		if (itemsPerSecond < 0.1) return;
		CCGLang.add(translatable("create_cyber_goggles.tooltip.beltThroughput").withStyle(GRAY)).forGoggles(tooltip);
		CCGLang.text(String.format("%.2f", itemsPerSecond), GOLD).text(" / ", DARK_GRAY).seconds(DARK_GRAY).forGoggles(tooltip, 1);
	}
	public static boolean pulse(List<Component> tooltip, int state, int maxState) {
		if (!CCG.config.goggles.enhancedInfo) return false;
		CCGLang.add(translatable("create_cyber_goggles.tooltip.pulse")).forGoggles(tooltip);
		CCGLang.fraction(state, maxState).forGoggles(tooltip);
		return true;
	}
	public static boolean depot(List<Component> tooltip, DepotItemHandler itemHandler) {
		if (!CCG.config.tooltip.depot) return false;
		if (itemHandler == null) return false;
		var stacks = new ArrayList<ItemStack>();
		var stackAdded = false;
		for (var i = 1; i < itemHandler.getSlots(); i++) {
			var stack = itemHandler.getStackInSlot(i);
			if (stack.isEmpty()) continue;
			stacks.add(stack);
			stackAdded = true;
		}
		if (!stackAdded) return false;
		CCGLang.add(translatable("create_cyber_goggles.tooltip.content")).forGoggles(tooltip);
		stacks.forEach(stack -> CCGLang.itemEntry(stack, CCGLang.itemName(stack).component()).forGoggles(tooltip, 1));
		return true;
	}
	public static boolean redstoneRequester(List<Component> tooltip, List<BigItemStack> bigStacks) {
		if (!CCG.config.tooltip.redstoneRequester) return false;
		if (bigStacks.isEmpty()) return false;
		var stacks = new ArrayList<ItemStack>();
		bigStacks.forEach(bigStack -> stacks.add(bigStack.stack.copyWithCount(bigStack.count)));
		CCGLang.add(translatable("create_cyber_goggles.tooltip.content")).forGoggles(tooltip);
		CCGLang.itemList(stacks, stacks.size() > 9 ? 9 : 3).forGoggles(tooltip);
		return true;
	}
	public static boolean basin(
		List<Component> tooltip,
		@NotNull List<ItemStack> inputItems,
		List<ItemStack> outputItems,
		@NotNull List<FluidStack> inputFluids,
		List<FluidStack> outputFluids,
		List<Integer> inputCapacities,
		List<Integer> outputCapacities
	) {
		if (inputItems.isEmpty() && outputItems.isEmpty() && inputFluids.isEmpty() && outputFluids.isEmpty()) return false;
		CCGLang.add(translatable("create.gui.goggles.basin_contents")).forGoggles(tooltip);
		addItems(tooltip, inputItems, translatable("create_cyber_goggles.tooltip.inputItems").withStyle(GRAY));
		addFluids(tooltip, inputFluids, inputCapacities, translatable("create_cyber_goggles.tooltip.inputFluids").withStyle(GRAY));
		addItems(tooltip, outputItems, translatable("create_cyber_goggles.tooltip.outputItems").withStyle(GRAY));
		addFluids(tooltip, outputFluids, outputCapacities, translatable("create_cyber_goggles.tooltip.outputFluids").withStyle(GRAY));
		return true;
	}
	private static void addItems(List<Component> tooltip, @NotNull List<ItemStack> items, MutableComponent header) {
		if (items.isEmpty()) return;
		CCGLang.add(header).forGoggles(tooltip, 1);
		items.forEach(stack -> CCGLang.itemEntry(stack, CCGLang.itemName(stack).component()).forGoggles(tooltip, 1));
	}
	private static void addFluids(
		List<Component> tooltip,
		@NotNull List<FluidStack> fluids,
		@NotNull List<Integer> capacities,
		MutableComponent header
	) {
		if (fluids.isEmpty()) return;
		CCGLang.add(header).forGoggles(tooltip, 1);
		for (var i = 0; i < fluids.size(); i++) {
			var fluidStack = fluids.get(i);
			var capacityMb = i < capacities.size() ? capacities.get(i) : Math.max(1000, fluidStack.getAmount());
			CCGLang.fluidEntry(fluidStack, capacityMb).forGoggles(tooltip, 1);
		}
	}
	public static boolean crushingController(List<Component> tooltip, CrushingWheelControllerBlockEntity cwcbe) {
		if (!CCG.config.tooltip.crushingController) return false;
		CCGLang.add(translatable("create_cyber_goggles.tooltip.crushingController"))
			.fraction(cwcbe.crushingspeed * 50, AllConfigs.server().kinetics.maxRotationSpeed.get())
			.forGoggles(tooltip);
		var inputCount = cwcbe.inventory.getStackInSlot(0).getCount();
		var processingSpeed = Mth.clamp(
			cwcbe.crushingspeed * 4 / (
				!cwcbe.inventory.appliedRecipe ? (float) Math.log(inputCount) / (float) Math.log(2) : 1
			), .25f, 20
		);
		var leftTick = (int) (cwcbe.inventory.remainingTime / processingSpeed);
		if (leftTick == 0) return false;
		CCGLang.add(translatable("create_cyber_goggles.tooltip.leftTime").withStyle(GRAY))
			.number(leftTick / 20, GOLD)
			.space()
			.seconds(GRAY)
			.forGoggles(tooltip);
		CCGLang.add(translatable("create_cyber_goggles.tooltip.expectedOutputs").withStyle(GRAY)).forGoggles(tooltip);
		if (mc.player == null || mc.player.isShiftKeyDown()) cwcbe.findRecipe().ifPresentOrElse(
			holder -> holder.value().getRollableResults().forEach(result -> {
				var stack = result.getStack();
				var chance = result.getChance();
				var label =
					CCGLang.itemName(stack).text(" x", DARK_GRAY).number(chance * 100).style(AQUA).text("%", DARK_GRAY).component();
				CCGLang.itemEntry(stack.copyWithCount(inputCount * stack.getCount()), label).forGoggles(tooltip);
			}), () -> CCGLang.itemName(ItemStack.EMPTY).forGoggles(tooltip, 2)
		);
		else cwcbe.findRecipe().ifPresentOrElse(
			holder -> holder.value().getRollableResults().forEach(result -> {
				var stack = result.getStack();
				var chance = result.getChance();
				var line = CCGLang.itemName(stack).text(" x", DARK_GRAY).number(inputCount * stack.getCount() * chance, GOLD).component();
				CCGLang.itemEntry(stack.copyWithCount(1), line).forGoggles(tooltip);
			}), () -> CCGLang.itemName(ItemStack.EMPTY).forGoggles(tooltip, 2)
		);
		return true;
	}
	public static boolean millstone(List<Component> tooltip, MillstoneBlockEntity mbe, MillingRecipe lastRecipe) {
		if (!CCG.config.tooltip.millstone) return false;
		CCGLang.add(translatable("create_cyber_goggles.tooltip.crushingController"))
			.add(CCGLang.fraction(mbe.getProcessingSpeed() * 16, AllConfigs.server().kinetics.maxRotationSpeed.get()))
			.forGoggles(tooltip);
		var processingSpeed = Math.max(1, mbe.getProcessingSpeed());
		var leftTick = (int) Math.ceil(mbe.timer / (double) processingSpeed);
		if (leftTick == 0) return false;
		CCGLang.add(translatable("create_cyber_goggles.tooltip.leftTime").withStyle(GRAY))
			.number(leftTick / 20, GOLD)
			.space()
			.seconds(GRAY)
			.forGoggles(tooltip);
		CCGLang.add(translatable("create_cyber_goggles.tooltip.expectedOutputs").withStyle(GRAY)).forGoggles(tooltip);
		if (lastRecipe == null) {
			CCGLang.itemName(ItemStack.EMPTY).forGoggles(tooltip, 2);
			return true;
		}
		var inputCount = Math.max(1, mbe.inputInv.getStackInSlot(0).getCount());
		if (mc.player == null || mc.player.isShiftKeyDown()) lastRecipe.getRollableResults().forEach(result -> {
			var stack = result.getStack();
			var chance = result.getChance();
			var label = CCGLang.itemName(stack).text(" x", DARK_GRAY).number(chance * 100, AQUA).text("%", DARK_GRAY).component();
			CCGLang.itemEntry(stack.copyWithCount(inputCount * stack.getCount()), label).forGoggles(tooltip);
		});
		else lastRecipe.getRollableResults().forEach(result -> {
			var stack = result.getStack();
			var chance = result.getChance();
			var line = CCGLang.itemName(stack).text(" x", DARK_GRAY).number(inputCount * stack.getCount() * chance, GOLD).component();
			CCGLang.itemEntry(stack.copyWithCount(1), line).forGoggles(tooltip);
		});
		return true;
	}
}
