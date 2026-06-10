package io.github.forgestove.create_cyber_goggles.core.util;
import com.zurrtum.create.*;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.*;
import com.zurrtum.create.client.foundation.item.TooltipHelper;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.equipment.armor.*;
import com.zurrtum.create.content.kinetics.base.*;
import com.zurrtum.create.content.kinetics.base.IRotate.StressImpact;
import com.zurrtum.create.content.kinetics.crusher.CrushingWheelControllerBlockEntity;
import com.zurrtum.create.content.kinetics.millstone.*;
import com.zurrtum.create.content.logistics.BigItemStack;
import com.zurrtum.create.content.logistics.depot.DepotItemHandler;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlockEntity.FuelType;
import com.zurrtum.create.content.schematics.cannon.SchematicannonBlockEntity;
import com.zurrtum.create.content.schematics.cannon.SchematicannonBlockEntity.State;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.event.*;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
import static net.minecraft.ChatFormatting.*;
public final class GoggleTooltipUtil {
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
		SpeedGaugeTooltipBehaviour.getFormattedSpeedText(speed, kbe.isOverStressed()).forGoggles(tooltip);
		if (!CCGKey.showStress.keyMapping.isDown()) return;
		double stressFraction = stress / (capacity == 0 ? 1 : capacity);
		CreateLang.translate("gui.stressometer.title").style(GRAY).forGoggles(tooltip);
		if (speed == 0) {
			CreateLang.text(TooltipHelper.makeProgressBar(3, 0))
				.translate("gui.stressometer.no_rotation")
				.style(DARK_GRAY)
				.forGoggles(tooltip);
			return;
		}
		StressGaugeTooltipBehaviour.getFormattedStressText(stressFraction).forGoggles(tooltip);
		CreateLang.translate("gui.stressometer.capacity").style(GRAY).forGoggles(tooltip);
		double remainingCapacity = capacity - stress;
		var su = CreateLang.translate("generic.unit.stress");
		var stressTip = CreateLang.number(remainingCapacity).add(su).style(StressImpact.of(stressFraction).getRelativeColor());
		if (remainingCapacity != capacity) stressTip.text(GRAY, " / ").add(CreateLang.number(capacity).add(su).style(DARK_GRAY));
		stressTip.forGoggles(tooltip, 1);
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
	public static boolean fan(List<Component> tooltip, boolean pushing, float range) {
		if (!CCG.config.goggles.enhancedInfo || range == 0) return false;
		CCGLang.translate("tooltip.windState").forGoggles(tooltip);
		CCGLang.number(range)
			.space()
			.translate(pushing ? "tooltip.pushRange" : "tooltip.pullRange")
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
		CCGLang.translate("tooltip.burnerState").forGoggles(tooltip);
		CCGLang.translate("tooltip.leftTime", GRAY)
			.text(isCreative ? "∞" : String.valueOf(remainingBurnTime / 20), format)
			.text(" / %d ".formatted(BlazeBurnerBlockEntity.INSERTION_THRESHOLD))
			.seconds()
			.forGoggles(tooltip);
		return true;
	}
	public static boolean cannon(List<Component> tooltip, @NotNull SchematicannonBlockEntity sbe) {
		if (!CCG.config.goggles.enhancedInfo) return false;
		CCGLang.translate("tooltip.cannonState").forGoggles(tooltip);
		CreateLang.translate("schematicannon.status." + sbe.statusMsg).style(GOLD).forGoggles(tooltip);
		var shotsLeft = sbe.remainingFuel;
		var shotsLeftWithItems = shotsLeft + sbe.inventory.getItem(4).getCount() * sbe.getShotsPerGunpowder();
		if (sbe.hasCreativeCrate) {
			CreateLang.translate("gui.schematicannon.gunpowderLevel", "" + 100).forGoggles(tooltip);
			CCGLang.text("(").add(AllBlocks.CREATIVE_CRATE.getName()).text(")").style(DARK_PURPLE).forGoggles(tooltip);
		} else {
			var fillPercent = (int) (shotsLeft / (float) sbe.getShotsPerGunpowder() * 100);
			CreateLang.translate("gui.schematicannon.gunpowderLevel", fillPercent).forGoggles(tooltip);
			CreateLang.builder()
				.add(CreateLang.translateDirect("gui.schematicannon.shotsRemaining", CCGLang.number(shotsLeft, BLUE).component())
					.withStyle(GRAY))
				.forGoggles(tooltip);
			if (shotsLeftWithItems != shotsLeft) CreateLang.builder()
				.add(CreateLang.translateDirect("gui.schematicannon.shotsRemainingWithBackup",
					CCGLang.number(shotsLeftWithItems, BLUE).component()
				).withStyle(GRAY))
				.forGoggles(tooltip);
		}
		if (!sbe.state.equals(State.RUNNING)) return true;
		CCGLang.translate("tooltip.printProgress").forGoggles(tooltip);
		CCGLang.fraction(sbe.blocksPlaced, sbe.blocksToPlace).forGoggles(tooltip);
		CCGLang.progress(sbe.schematicProgress, 20).forGoggles(tooltip);
		return true;
	}
	public static boolean backtank(List<Component> tooltip, BacktankBlockEntity bbe, int capacityEnchantLevel, int leftTick) {
		if (!CCG.config.goggles.enhancedInfo) return false;
		CreateLang.translate("gui.goggles.fluid_container").forGoggles(tooltip);
		CreateLang.translate("gui.goggles.fluid_container.capacity")
			.style(GRAY)
			.add(CCGLang.fraction(bbe.airLevel, BacktankUtil.maxAir(capacityEnchantLevel)).component())
			.forGoggles(tooltip);
		if (bbe.getSpeed() == 0 || leftTick == 0) return false;
		CCGLang.translate("tooltip.leftTime", GRAY).number(leftTick / 20, GOLD).space().seconds(GRAY).forGoggles(tooltip);
		return true;
	}
	public static void beltThroughput(List<Component> tooltip, double itemsPerSecond) {
		if (itemsPerSecond < 0.1) return;
		CCGLang.translate("tooltip.beltThroughput", GRAY).forGoggles(tooltip);
		CCGLang.text(String.format("%.2f", itemsPerSecond), GOLD).text(" / ", DARK_GRAY).seconds(DARK_GRAY).forGoggles(tooltip, 1);
	}
	public static boolean pulse(List<Component> tooltip, int state, int maxState) {
		if (!CCG.config.goggles.enhancedInfo) return false;
		CCGLang.translate("tooltip.pulse").forGoggles(tooltip);
		CCGLang.fraction(state, maxState).forGoggles(tooltip);
		return true;
	}
	public static boolean depot(List<Component> tooltip, DepotItemHandler itemHandler) {
		if (!CCG.config.tooltip.depot) return false;
		if (itemHandler == null) return false;
		var stacks = new ArrayList<ItemStack>();
		var stackAdded = false;
		for (var i = 1; i < itemHandler.getContainerSize(); i++) {
			var stack = itemHandler.getItem(i);
			if (stack.isEmpty()) continue;
			stacks.add(stack);
			stackAdded = true;
		}
		if (!stackAdded) return false;
		CCGLang.translate("tooltip.content").forGoggles(tooltip);
		stacks.forEach(stack -> CCGLang.itemEntry(stack, CCGLang.item(stack).component()).forGoggles(tooltip, 1));
		return true;
	}
	public static boolean redstoneRequester(List<Component> tooltip, List<BigItemStack> bigStacks) {
		if (!CCG.config.tooltip.redstoneRequester) return false;
		if (bigStacks.isEmpty()) return false;
		var stacks = new ArrayList<ItemStack>();
		bigStacks.forEach(bigStack -> stacks.add(bigStack.stack.copyWithCount(bigStack.count)));
		CCGLang.translate("tooltip.content").forGoggles(tooltip);
		CCGLang.itemList(stacks, 3).forGoggles(tooltip);
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
		var hasItems = !inputItems.isEmpty() || !outputItems.isEmpty();
		var hasFluids = !inputFluids.isEmpty() || !outputFluids.isEmpty();
		if (!hasItems && !hasFluids) return false;
		CreateLang.translate("gui.goggles.basin_contents").forGoggles(tooltip);
		if (!inputItems.isEmpty()) {
			CCGLang.translate("tooltip.inputItems", GRAY).forGoggles(tooltip, 1);
			inputItems.forEach(stack -> CCGLang.itemEntry(stack, CCGLang.item(stack).component()).forGoggles(tooltip, 1));
		}
		if (!inputFluids.isEmpty()) {
			CCGLang.translate("tooltip.inputFluids", GRAY).forGoggles(tooltip, 1);
			for (var i = 0; i < inputFluids.size(); i++) {
				var fluidStack = inputFluids.get(i);
				var capacityMb = i < inputCapacities.size() ? inputCapacities.get(i) : Math.max(1000, fluidStack.getAmount());
				CCGLang.fluidEntry(fluidStack, capacityMb).forGoggles(tooltip, 1);
			}
		}
		if (!outputItems.isEmpty()) {
			CCGLang.translate("tooltip.outputItems", GRAY).forGoggles(tooltip, 1);
			outputItems.forEach(stack -> CCGLang.itemEntry(stack, CCGLang.item(stack).component()).forGoggles(tooltip, 1));
		}
		if (!outputFluids.isEmpty()) {
			CCGLang.translate("tooltip.outputFluids", GRAY).forGoggles(tooltip, 1);
			for (var i = 0; i < outputFluids.size(); i++) {
				var fluidStack = outputFluids.get(i);
				var capacityMb = i < outputCapacities.size() ? outputCapacities.get(i) : Math.max(1000, fluidStack.getAmount());
				CCGLang.fluidEntry(fluidStack, capacityMb).forGoggles(tooltip, 1);
			}
		}
		return true;
	}
	public static boolean crushingController(List<Component> tooltip, CrushingWheelControllerBlockEntity cwcbe) {
		if (!CCG.config.tooltip.crushingController) return false;
		CCGLang.translate("tooltip.crushingController")
			.fraction(cwcbe.crushingspeed * 50, AllConfigs.server().kinetics.maxRotationSpeed.get())
			.forGoggles(tooltip);
		var inputCount = cwcbe.inventory.getItem(0).getCount();
		var processingSpeed = Mth.clamp(
			cwcbe.crushingspeed * 4 / (
				!cwcbe.inventory.appliedRecipe ? (float) Math.log(inputCount) / (float) Math.log(2) : 1
			), .25f, 20
		);
		var leftTick = (int) (cwcbe.inventory.remainingTime / processingSpeed);
		if (leftTick == 0) return false;
		CCGLang.translate("tooltip.leftTime", GRAY).number(leftTick / 20, GOLD).space().seconds(GRAY).forGoggles(tooltip);
		CCGLang.translate("tooltip.expectedOutputs", GRAY).forGoggles(tooltip);
		var recipe = cwcbe.findRecipe();
		if (recipe == null) {
			CCGLang.item(ItemStack.EMPTY).forGoggles(tooltip, 2);
			return true;
		}
		if (mc.player == null || mc.player.isShiftKeyDown()) recipe.results().forEach(result -> {
			var stack = result.create();
			var chance = result.chance();
			var label = CCGLang.item(stack).text(" x", DARK_GRAY).number(chance * 100).style(AQUA).text("%", DARK_GRAY).component();
			CCGLang.itemEntry(stack.copyWithCount(inputCount * stack.getCount()), label).forGoggles(tooltip);
		});
		else recipe.results().forEach(result -> {
			var stack = result.create();
			var chance = result.chance();
			var line = CCGLang.item(stack).text(" x", DARK_GRAY).number(inputCount * stack.getCount() * chance, GOLD).component();
			CCGLang.itemEntry(stack.copyWithCount(1), line).forGoggles(tooltip);
		});
		return true;
	}
	public static boolean millstone(List<Component> tooltip, MillstoneBlockEntity mbe) {
		if (!CCG.config.tooltip.millstone) return false;
		var level = mbe.getLevel();
		if (level == null) return false;
		var capabilityItem = mbe.capability.getItem(0);
		if (capabilityItem.isEmpty()) return false;
		var input = new SingleRecipeInput(capabilityItem);
		var recipe = level.recipeAccess().getSynchronizedRecipes().getFirstMatch(AllRecipeTypes.MILLING, input, level);
		if (recipe.isEmpty()) return false;
		var lastRecipe = recipe.get().value();
		CCGLang.translate("tooltip.crushingController")
			.add(CCGLang.fraction(mbe.getProcessingSpeed() * 16, AllConfigs.server().kinetics.maxRotationSpeed.get()))
			.forGoggles(tooltip);
		var processingSpeed = Math.max(1, mbe.getProcessingSpeed());
		var leftTick = (int) Math.ceil(mbe.timer / (double) processingSpeed);
		if (leftTick == 0) return false;
		CCGLang.translate("tooltip.leftTime", GRAY).number(leftTick / 20, GOLD).space().seconds(GRAY).forGoggles(tooltip);
		CCGLang.translate("tooltip.expectedOutputs", GRAY).forGoggles(tooltip);
		var inputCount = Math.max(1, mbe.capability.getItem(0).getCount());
		if (mc.player == null || mc.player.isShiftKeyDown()) lastRecipe.results().forEach(result -> {
			var stack = result.create();
			var chance = result.chance();
			var label = CCGLang.item(stack).text(" x", DARK_GRAY).number(chance * 100, AQUA).text("%", DARK_GRAY).component();
			CCGLang.itemEntry(stack.copyWithCount(inputCount * stack.getCount()), label).forGoggles(tooltip);
		});
		else lastRecipe.results().forEach(result -> {
			var stack = result.create();
			var chance = result.chance();
			var line = CCGLang.item(stack).text(" x", DARK_GRAY).number(inputCount * stack.getCount() * chance, GOLD).component();
			CCGLang.itemEntry(stack.copyWithCount(1), line).forGoggles(tooltip);
		});
		return true;
	}
}
