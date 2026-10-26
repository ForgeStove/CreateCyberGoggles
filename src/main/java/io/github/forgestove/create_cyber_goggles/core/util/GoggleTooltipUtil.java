package io.github.forgestove.create_cyber_goggles.core.util;
import com.simibubi.create.*;
import com.simibubi.create.content.equipment.armor.*;
import com.simibubi.create.content.kinetics.base.*;
import com.simibubi.create.content.kinetics.base.IRotate.*;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelControllerBlockEntity;
import com.simibubi.create.content.kinetics.fan.*;
import com.simibubi.create.content.kinetics.millstone.*;
import com.simibubi.create.content.logistics.depot.*;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity.FuelType;
import com.simibubi.create.content.redstone.diodes.BrassDiodeBlockEntity;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity.State;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.config.AllConfigs;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.event.*;
import io.github.forgestove.create_cyber_goggles.core.util.contract.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.*;

import static net.minecraft.ChatFormatting.*;
import static net.minecraft.network.chat.Component.translatable;
public final class GoggleTooltipUtil {
	public static void kinetic(List<Component> tooltip, @NotNull KineticBlockEntity be, float stress, float capacity) {
		float speed = be.getTheoreticalSpeed();
		MutableComponent su = translatable("create.generic.unit.stress");
		if (StressImpact.isEnabled()) {
			float stressAtBase = be.calculateStressApplied();
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
		SpeedLevel.getFormattedSpeedText(speed, be.isOverStressed()).forGoggles(tooltip);
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
		CCGLangBuilder stressTip = CCGLang.number(remainingCapacity).add(su).style(StressImpact.of(stressFraction).getRelativeColor());
		if (remainingCapacity != capacity) stressTip.text(" / ", GRAY).add(CCGLang.number(capacity).add(su).style(DARK_GRAY));
		stressTip.forGoggles(tooltip, 1);
	}
	public static void generatingKinetic(List<Component> tooltip, @NotNull GeneratingKineticBlockEntity be) {
		float stressBase = be.calculateAddedStressCapacity();
		if (!Mth.equal(stressBase, 0)) {
			CCGLang.add(translatable("create.gui.goggles.generator_stats")).forGoggles(tooltip);
			CCGLang.add(translatable("create.tooltip.capacityProvided").withStyle(GRAY)).forGoggles(tooltip);
			float speed = be.getTheoreticalSpeed();
			float generatedSpeed = be.getGeneratedSpeed();
			if (speed != generatedSpeed) stressBase *= generatedSpeed / speed;
			CCGLang.number(Math.abs(stressBase * speed))
				.add(translatable("create.generic.unit.stress"))
				.style(AQUA)
				.space()
				.add(translatable("create.gui.goggles.at_current_speed").withStyle(DARK_GRAY))
				.forGoggles(tooltip, 1);
		}
	}
	public static boolean fan(List<Component> tooltip, @NonNull EncasedFanBlockEntity be) {
		AirCurrent airCurrent = be.getAirCurrent();
		return airCurrent(tooltip, airCurrent.pushing, airCurrent.maxDistance);
	}
	public static boolean nozzle(List<Component> tooltip, NozzleBlockEntity be) {
		var data = (NozzleTooltipData) be;
		return airCurrent(tooltip, data.ccg$getPushing(), data.ccg$getRange() / 2F);
	}
	private static boolean airCurrent(List<Component> tooltip, boolean pushing, float range) {
		if (!CCG.config.goggles.enhancedInfo || range == 0) return false;
		CCGLang.add(translatable("create_cyber_goggles.tooltip.windState")).forGoggles(tooltip);
		CCGLang.number(range)
			.space()
			.add(pushing ? translatable("create_cyber_goggles.tooltip.pushRange") : translatable("create_cyber_goggles.tooltip.pullRange"))
			.color(Outliner.getColor(pushing))
			.forGoggles(tooltip);
		return true;
	}
	public static boolean burner(List<Component> tooltip, BlazeBurnerBlockEntity be) {
		if (!CCG.config.goggles.enhancedInfo) return false;
		int remainingBurnTime = be.getRemainingBurnTime();
		boolean isCreative = be.isCreative();
		FuelType activeFuel = be.getActiveFuel();
		if (remainingBurnTime == 0 && !isCreative) return false;
		ChatFormatting format = switch (activeFuel) {
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
	public static boolean cannon(List<Component> tooltip, @NotNull SchematicannonBlockEntity be) {
		if (!CCG.config.goggles.enhancedInfo) return false;
		CCGLang.add(translatable("create_cyber_goggles.tooltip.cannonState")).forGoggles(tooltip);
		CreateLang.translate("schematicannon.status." + be.statusMsg).style(GOLD).forGoggles(tooltip);
		if (be.missingItem != null) CCGLang.itemEntry(be.missingItem).forGoggles(tooltip);
		int shotsLeft = be.remainingFuel;
		var shotsLeftWithItems = shotsLeft + be.inventory.getStackInSlot(4).getCount() * be.getShotsPerGunpowder();
		if (be.hasCreativeCrate) {
			CCGLang.add(translatable("create.gui.schematicannon.gunpowderLevel", "" + 100)).forGoggles(tooltip);
			CCGLang.text("(").add(AllBlocks.CREATIVE_CRATE.get().getName()).text(")").style(DARK_PURPLE).forGoggles(tooltip);
		} else {
			var fillPercent = (int) (shotsLeft / (float) be.getShotsPerGunpowder() * 100);
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
		if (!be.state.equals(State.RUNNING)) return true;
		CCGLang.add(translatable("create_cyber_goggles.tooltip.printProgress")).forGoggles(tooltip);
		CCGLang.fraction(be.blocksPlaced, be.blocksToPlace).forGoggles(tooltip);
		CCGLang.progress(be.schematicProgress, 20).forGoggles(tooltip);
		return true;
	}
	public static boolean backtank(List<Component> tooltip, BacktankBlockEntity be) {
		if (!CCG.config.goggles.enhancedInfo) return false;
		var data = (BacktankTooltipData) be;
		int capacityEnchantLevel = data.ccg$getCapacityEnchantLevel();
		int leftTick = data.ccg$getLeftTick();
		CCGLang.add(translatable("create.gui.goggles.fluid_container")).forGoggles(tooltip);
		CCGLang.add(translatable("create.gui.goggles.fluid_container.capacity").withStyle(GRAY))
			.add(CCGLang.fraction(be.airLevel, BacktankUtil.maxAir(capacityEnchantLevel)).component())
			.forGoggles(tooltip);
		if (be.getSpeed() == 0 || leftTick == 0) return false;
		CCGLang.add(translatable("create_cyber_goggles.tooltip.leftTime").withStyle(GRAY))
			.number(leftTick / 20, GOLD)
			.space()
			.seconds(GRAY)
			.forGoggles(tooltip);
		return true;
	}
	public static boolean belt(List<Component> tooltip, BeltBlockEntity be) {
		if (!CCG.config.goggles.enhancedInfo) return false;
		CCGLang.add(translatable("create_cyber_goggles.tooltip.isController").withStyle(GRAY)).is(be.isController()).forGoggles(tooltip);
		if (be.getSpeed() == 0) return false;
		var controllerBE = be.getControllerBE();
		if (controllerBE == null) return true;
		var itemsPerSecond = ((BeltTooltipData) controllerBE).ccg$getRate();
		if (itemsPerSecond < 0.1) return true;
		CCGLang.add(translatable("create_cyber_goggles.tooltip.beltThroughput").withStyle(GRAY)).forGoggles(tooltip);
		CCGLang.text(String.format("%.2f", itemsPerSecond), GOLD).text(" / ", DARK_GRAY).seconds(DARK_GRAY).forGoggles(tooltip, 1);
		return true;
	}
	public static boolean pulse(List<Component> tooltip, BrassDiodeBlockEntity be) {
		if (!CCG.config.goggles.enhancedInfo) return false;
		var data = (PulseTooltipData) be;
		var state = data.ccg$getState();
		var maxState = data.ccg$getMaxState();
		CCGLang.add(translatable("create_cyber_goggles.tooltip.pulse")).forGoggles(tooltip);
		CCGLang.fraction(state, maxState).forGoggles(tooltip);
		return true;
	}
	public static boolean depot(List<Component> tooltip, DepotBlockEntity be) {
		if (!CCG.config.tooltip.depot) return false;
		DepotItemHandler itemHandler = be.getBehaviour(DepotBehaviour.TYPE).itemHandler;
		if (itemHandler == null) return false;
		var stacks = new ArrayList<ItemStack>();
		var stackAdded = false;
		for (var i = 1; i < itemHandler.getSlots(); i++) {
			ItemStack stack = itemHandler.getStackInSlot(i);
			if (stack.isEmpty()) continue;
			stacks.add(stack);
			stackAdded = true;
		}
		if (!stackAdded) return false;
		CCGLang.add(translatable("create_cyber_goggles.tooltip.content")).forGoggles(tooltip);
		stacks.forEach(stack -> CCGLang.itemEntry(stack, CCGLang.itemName(stack).component()).forGoggles(tooltip, 1));
		return true;
	}
	public static boolean redstoneRequester(List<Component> tooltip, RedstoneRequesterBlockEntity be) {
		if (!CCG.config.tooltip.redstoneRequester) return false;
		var bigStacks = be.encodedRequest.stacks();
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
			FluidStack fluidStack = fluids.get(i);
			int capacityMb = i < capacities.size() ? capacities.get(i) : Math.max(1000, fluidStack.getAmount());
			CCGLang.fluidEntry(fluidStack, capacityMb).forGoggles(tooltip, 1);
		}
	}
	public static boolean crushingController(List<Component> tooltip, CrushingWheelControllerBlockEntity be, boolean isPlayerSneaking) {
		if (!CCG.config.tooltip.crushingController) return false;
		CCGLang.add(translatable("create_cyber_goggles.tooltip.crushingController"))
			.fraction(be.crushingspeed * 50, AllConfigs.server().kinetics.maxRotationSpeed.get())
			.forGoggles(tooltip);
		int inputCount = be.inventory.getStackInSlot(0).getCount();
		float processingSpeed = Mth.clamp(
			be.crushingspeed * 4 / (
				!be.inventory.appliedRecipe ? (float) Math.log(inputCount) / (float) Math.log(2) : 1
			), .25f, 20
		);
		var leftTick = (int) (be.inventory.remainingTime / processingSpeed);
		if (leftTick == 0) return false;
		CCGLang.add(translatable("create_cyber_goggles.tooltip.leftTime").withStyle(GRAY))
			.number(leftTick / 20, GOLD)
			.space()
			.seconds(GRAY)
			.forGoggles(tooltip);
		CCGLang.add(translatable("create_cyber_goggles.tooltip.expectedOutputs").withStyle(GRAY)).forGoggles(tooltip);
		if (isPlayerSneaking) be.findRecipe().ifPresentOrElse(
			holder -> holder.value().getRollableResults().forEach(result -> {
				ItemStack stack = result.getStack();
				float chance = result.getChance();
				MutableComponent label = CCGLang.itemName(stack)
					.text(" x", DARK_GRAY)
					.number(chance * 100)
					.style(AQUA)
					.text("%", DARK_GRAY)
					.component();
				CCGLang.itemEntry(stack.copyWithCount(inputCount * stack.getCount()), label).forGoggles(tooltip);
			}), () -> CCGLang.itemName(ItemStack.EMPTY).forGoggles(tooltip, 2)
		);
		else be.findRecipe().ifPresentOrElse(
			holder -> holder.value().getRollableResults().forEach(result -> {
				ItemStack stack = result.getStack();
				float chance = result.getChance();
				MutableComponent line = CCGLang.itemName(stack)
					.text(" x", DARK_GRAY)
					.number(inputCount * stack.getCount() * chance, GOLD)
					.component();
				CCGLang.itemEntry(stack.copyWithCount(1), line).forGoggles(tooltip);
			}), () -> CCGLang.itemName(ItemStack.EMPTY).forGoggles(tooltip, 2)
		);
		return true;
	}
	public static boolean millstone(List<Component> tooltip, MillstoneBlockEntity be, boolean isPlayerSneaking) {
		if (!CCG.config.tooltip.millstone) return false;
		Level level = be.getLevel();
		if (level == null) return false;
		Optional<RecipeHolder<MillingRecipe>> found = AllRecipeTypes.MILLING.find(new RecipeWrapper(be.inputInv), level);
		if (found.isEmpty()) return false;
		MillingRecipe lastRecipe = found.get().value();
		CCGLang.add(translatable("create_cyber_goggles.tooltip.crushingController"))
			.add(CCGLang.fraction(be.getProcessingSpeed() * 16, AllConfigs.server().kinetics.maxRotationSpeed.get()))
			.forGoggles(tooltip);
		int processingSpeed = Math.max(1, be.getProcessingSpeed());
		var leftTick = (int) Math.ceil(be.timer / (double) processingSpeed);
		if (leftTick == 0) return false;
		CCGLang.add(translatable("create_cyber_goggles.tooltip.leftTime").withStyle(GRAY))
			.number(leftTick / 20, GOLD)
			.space()
			.seconds(GRAY)
			.forGoggles(tooltip);
		CCGLang.add(translatable("create_cyber_goggles.tooltip.expectedOutputs").withStyle(GRAY)).forGoggles(tooltip);
		int inputCount = Math.max(1, be.inputInv.getStackInSlot(0).getCount());
		if (isPlayerSneaking) lastRecipe.getRollableResults().forEach(result -> {
			ItemStack stack = result.getStack();
			float chance = result.getChance();
			MutableComponent label = CCGLang.itemName(stack)
				.text(" x", DARK_GRAY)
				.number(chance * 100, AQUA)
				.text("%", DARK_GRAY)
				.component();
			CCGLang.itemEntry(stack.copyWithCount(inputCount * stack.getCount()), label).forGoggles(tooltip);
		});
		else lastRecipe.getRollableResults().forEach(result -> {
			ItemStack stack = result.getStack();
			float chance = result.getChance();
			MutableComponent line = CCGLang.itemName(stack)
				.text(" x", DARK_GRAY)
				.number(inputCount * stack.getCount() * chance, GOLD)
				.component();
			CCGLang.itemEntry(stack.copyWithCount(1), line).forGoggles(tooltip);
		});
		return true;
	}
}
