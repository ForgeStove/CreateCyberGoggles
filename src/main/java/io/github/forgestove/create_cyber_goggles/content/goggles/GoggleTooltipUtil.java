package io.github.forgestove.create_cyber_goggles.content.goggles;
import com.simibubi.create.*;
import com.simibubi.create.content.equipment.armor.*;
import com.simibubi.create.content.fluids.*;
import com.simibubi.create.content.fluids.PipeConnection.Flow;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.kinetics.base.*;
import com.simibubi.create.content.kinetics.base.IRotate.*;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelControllerBlockEntity;
import com.simibubi.create.content.kinetics.fan.*;
import com.simibubi.create.content.kinetics.millstone.*;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.depot.*;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity.FuelType;
import com.simibubi.create.content.redstone.diodes.BrassDiodeBlockEntity;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity.State;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.infrastructure.config.AllConfigs;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.content.goggles.tooltip.data.*;
import io.github.forgestove.create_cyber_goggles.foundation.events.CCGKey;
import io.github.forgestove.create_cyber_goggles.foundation.lang.*;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.*;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.*;

import static net.minecraft.ChatFormatting.*;
public final class GoggleTooltipUtil {
	public static void kinetic(List<Component> tooltip, @NotNull KineticBlockEntity be, float stress, float capacity) {
		float speed = be.getTheoreticalSpeed();
		LangBuilder su = CCGCreateLang.translate("generic.unit.stress");
		if (StressImpact.isEnabled()) {
			float stressAtBase = be.calculateStressApplied();
			if (!Mth.equal(stressAtBase, 0)) {
				CCGCreateLang.translate("tooltip.stressImpact").style(GRAY).forGoggles(tooltip);
				CCGCreateLang.number(stressAtBase * Math.abs(speed))
					.add(su)
					.style(AQUA)
					.space()
					.add(CCGCreateLang.translate("gui.goggles.at_current_speed").style(DARK_GRAY))
					.forGoggles(tooltip, 1);
			}
		}
		CCGCreateLang.translate("gui.speedometer.title").style(GRAY).forGoggles(tooltip);
		SpeedLevel.getFormattedSpeedText(speed, be.isOverStressed()).forGoggles(tooltip);
		if (!CCGKey.showStress.isDown()) return;
		double stressFraction = stress / (capacity == 0 ? 1 : capacity);
		CCGCreateLang.translate("gui.stressometer.title").style(GRAY).forGoggles(tooltip);
		if (speed == 0) {
			CCGCreateLang.text(TooltipHelper.makeProgressBar(3, 0))
				.add(CCGCreateLang.translate("gui.stressometer.no_rotation"))
				.style(DARK_GRAY)
				.forGoggles(tooltip);
			return;
		}
		StressImpact.getFormattedStressText(stressFraction).forGoggles(tooltip);
		CCGCreateLang.translate("gui.stressometer.capacity").style(GRAY).forGoggles(tooltip);
		double remainingCapacity = capacity - stress;
		LangBuilder stressTip = CCGCreateLang.number(remainingCapacity).add(su).style(StressImpact.of(stressFraction).getRelativeColor());
		if (remainingCapacity != capacity) stressTip.text(GRAY, " / ").add(CCGCreateLang.number(capacity).add(su).style(DARK_GRAY));
		stressTip.forGoggles(tooltip, 1);
	}
	public static void generatingKinetic(List<Component> tooltip, @NotNull GeneratingKineticBlockEntity be) {
		float stressBase = be.calculateAddedStressCapacity();
		if (!Mth.equal(stressBase, 0)) {
			CCGCreateLang.translate("gui.goggles.generator_stats").forGoggles(tooltip);
			CCGCreateLang.translate("tooltip.capacityProvided").style(GRAY).forGoggles(tooltip);
			float speed = be.getTheoreticalSpeed();
			float generatedSpeed = be.getGeneratedSpeed();
			if (speed != generatedSpeed) stressBase *= generatedSpeed / speed;
			CCGCreateLang.number(Math.abs(stressBase * speed))
				.add(CCGCreateLang.translate("generic.unit.stress"))
				.style(AQUA)
				.space()
				.add(CCGCreateLang.translate("gui.goggles.at_current_speed").style(DARK_GRAY))
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
		CCGLang.of().translate("tooltip.windState").forGoggles(tooltip);
		CCGLang.of()
			.number(range)
			.space()
			.add(pushing ? CCGLang.of().translate("tooltip.pushRange") : CCGLang.of().translate("tooltip.pullRange"))
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
		CCGLang.of().translate("tooltip.burnerState").forGoggles(tooltip);
		CCGLang.of()
			.translate("tooltip.leftTime", GRAY)
			.text(isCreative ? "∞" : String.valueOf(remainingBurnTime / 20), format)
			.text(" / %d ".formatted(BlazeBurnerBlockEntity.INSERTION_THRESHOLD))
			.seconds()
			.forGoggles(tooltip);
		return true;
	}
	public static boolean cannon(List<Component> tooltip, @NotNull SchematicannonBlockEntity be) {
		if (!CCG.config.goggles.enhancedInfo) return false;
		CCGLang.of().translate("tooltip.cannonState").forGoggles(tooltip);
		CCGCreateLang.translate("schematicannon.status." + be.statusMsg).style(GOLD).forGoggles(tooltip);
		if (be.missingItem != null) CCGLang.of().itemEntry(be.missingItem).forGoggles(tooltip);
		int shotsLeft = be.remainingFuel;
		int shotsLeftWithItems = shotsLeft + be.inventory.getStackInSlot(4).getCount() * be.getShotsPerGunpowder();
		if (be.hasCreativeCrate) {
			CCGCreateLang.translate("gui.schematicannon.gunpowderLevel", "" + 100).forGoggles(tooltip);
			CCGLang.of().text("(").add(AllBlocks.CREATIVE_CRATE.get().getName()).text(")").style(DARK_PURPLE).forGoggles(tooltip);
		} else {
			var fillPercent = (int) (shotsLeft / (float) be.getShotsPerGunpowder() * 100);
			CCGCreateLang.translate("gui.schematicannon.gunpowderLevel", fillPercent).forGoggles(tooltip);
			CCGCreateLang.builder()
				.add(CCGCreateLang.translateDirect("gui.schematicannon.shotsRemaining", CCGLang.of().number(shotsLeft, BLUE).component())
					.withStyle(GRAY))
				.forGoggles(tooltip);
			if (shotsLeftWithItems != shotsLeft) CCGCreateLang.builder()
				.add(CCGCreateLang.translateDirect(
					"gui.schematicannon.shotsRemainingWithBackup",
					CCGLang.of().number(shotsLeftWithItems, BLUE).component()
				).withStyle(GRAY))
				.forGoggles(tooltip);
		}
		if (!be.state.equals(State.RUNNING)) return true;
		CCGLang.of().translate("tooltip.printProgress").forGoggles(tooltip);
		CCGLang.of().fraction(be.blocksPlaced, be.blocksToPlace).forGoggles(tooltip);
		CCGLang.of().progress(be.schematicProgress, 20).forGoggles(tooltip);
		return true;
	}
	public static boolean backtank(List<Component> tooltip, BacktankBlockEntity be) {
		if (!CCG.config.goggles.enhancedInfo) return false;
		var data = (BacktankTooltipData) be;
		int capacityEnchantLevel = data.ccg$getCapacityEnchantLevel();
		int leftTick = data.ccg$getLeftTick();
		CCGCreateLang.translate("gui.goggles.fluid_container").forGoggles(tooltip);
		CCGCreateLang.translate("gui.goggles.fluid_container.capacity")
			.style(GRAY)
			.add(CCGLang.of().fraction(be.airLevel, BacktankUtil.maxAir(capacityEnchantLevel)).component())
			.forGoggles(tooltip);
		if (be.getSpeed() == 0 || leftTick == 0) return false;
		CCGLang.of().translate("tooltip.leftTime", GRAY).number(leftTick / 20, GOLD).space().seconds(GRAY).forGoggles(tooltip);
		return true;
	}
	public static boolean belt(List<Component> tooltip, BeltBlockEntity be) {
		if (!CCG.config.goggles.enhancedInfo) return false;
		CCGLang.of().translate("tooltip.isController", GRAY).is(be.isController()).forGoggles(tooltip);
		if (be.getSpeed() == 0) return false;
		BeltBlockEntity controllerBE = be.getControllerBE();
		if (controllerBE == null) return true;
		double itemsPerSecond = ((BeltTooltipData) controllerBE).ccg$getRate();
		if (itemsPerSecond < 0.1) return true;
		CCGLang.of().translate("tooltip.beltThroughput", GRAY).forGoggles(tooltip);
		CCGLang.of().text(String.format("%.2f", itemsPerSecond), GOLD).text(" / ", DARK_GRAY).seconds(DARK_GRAY).forGoggles(tooltip, 1);
		return true;
	}
	public static boolean pulse(List<Component> tooltip, BrassDiodeBlockEntity be) {
		if (!CCG.config.goggles.enhancedInfo) return false;
		var data = (PulseTooltipData) be;
		int state = data.ccg$getState();
		int maxState = data.ccg$getMaxState();
		CCGLang.of().translate("tooltip.pulse").forGoggles(tooltip);
		CCGLang.of().fraction(state, maxState).forGoggles(tooltip);
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
		CCGLang.of().translate("tooltip.content").forGoggles(tooltip);
		stacks.forEach(stack -> CCGLang.of().itemEntry(stack, CCGLang.of().itemName(stack).component()).forGoggles(tooltip, 1));
		return true;
	}
	public static boolean redstoneRequester(List<Component> tooltip, RedstoneRequesterBlockEntity be) {
		if (!CCG.config.tooltip.redstoneRequester) return false;
		List<BigItemStack> bigStacks = be.encodedRequest.stacks();
		if (bigStacks.isEmpty()) return false;
		var stacks = new ArrayList<ItemStack>();
		bigStacks.forEach(bigStack -> stacks.add(bigStack.stack.copyWithCount(bigStack.count)));
		CCGLang.of().translate("tooltip.content").forGoggles(tooltip);
		CCGLang.of().itemList(stacks, stacks.size() > 9 ? 9 : 3).forGoggles(tooltip);
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
		CCGCreateLang.translate("gui.goggles.basin_contents").forGoggles(tooltip);
		addItems(tooltip, inputItems, CCGLang.of().translate("tooltip.inputItems", GRAY));
		addFluids(tooltip, inputFluids, inputCapacities, CCGLang.of().translate("tooltip.inputFluids", GRAY));
		addItems(tooltip, outputItems, CCGLang.of().translate("tooltip.outputItems", GRAY));
		addFluids(tooltip, outputFluids, outputCapacities, CCGLang.of().translate("tooltip.outputFluids", GRAY));
		return true;
	}
	private static void addItems(List<Component> tooltip, @NotNull List<ItemStack> items, CCGLangBuilder header) {
		if (items.isEmpty()) return;
		header.forGoggles(tooltip, 1);
		items.forEach(stack -> CCGLang.of().itemEntry(stack, CCGLang.of().itemName(stack).component()).forGoggles(tooltip, 1));
	}
	private static void addFluids(
		List<Component> tooltip,
		@NotNull List<FluidStack> fluids,
		@NotNull List<Integer> capacities,
		CCGLangBuilder header
	) {
		if (fluids.isEmpty()) return;
		header.forGoggles(tooltip, 1);
		for (var i = 0; i < fluids.size(); i++) {
			FluidStack fluidStack = fluids.get(i);
			int capacityMb = i < capacities.size() ? capacities.get(i) : Math.max(1000, fluidStack.getAmount());
			CCGLang.of().fluidEntry(fluidStack, capacityMb).forGoggles(tooltip, 1);
		}
	}
	public static boolean crushingController(List<Component> tooltip, CrushingWheelControllerBlockEntity be, boolean isPlayerSneaking) {
		if (!CCG.config.tooltip.crushingController) return false;
		CCGLang.of()
			.translate("tooltip.crushingController")
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
		CCGLang.of().translate("tooltip.leftTime", GRAY).number(leftTick / 20, GOLD).space().seconds(GRAY).forGoggles(tooltip);
		CCGLang.of().translate("tooltip.expectedOutputs", GRAY).forGoggles(tooltip);
		if (isPlayerSneaking) be.findRecipe().ifPresentOrElse(
			holder -> holder.value().getRollableResults().forEach(result -> {
				ItemStack stack = result.getStack();
				float chance = result.getChance();
				MutableComponent label = CCGLang.of()
					.itemName(stack)
					.text(" x", DARK_GRAY)
					.number(chance * 100)
					.style(AQUA)
					.text("%", DARK_GRAY)
					.component();
				CCGLang.of().itemEntry(stack.copyWithCount(inputCount * stack.getCount()), label).forGoggles(tooltip);
			}), () -> CCGLang.of().itemName(ItemStack.EMPTY).forGoggles(tooltip, 2)
		);
		else be.findRecipe().ifPresentOrElse(
			holder -> holder.value().getRollableResults().forEach(result -> {
				ItemStack stack = result.getStack();
				float chance = result.getChance();
				MutableComponent line = CCGLang.of()
					.itemName(stack)
					.text(" x", DARK_GRAY)
					.number(inputCount * stack.getCount() * chance, GOLD)
					.component();
				CCGLang.of().itemEntry(stack.copyWithCount(1), line).forGoggles(tooltip);
			}), () -> CCGLang.of().itemName(ItemStack.EMPTY).forGoggles(tooltip, 2)
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
		CCGLang.of()
			.translate("tooltip.crushingController")
			.add(CCGLang.of().fraction(be.getProcessingSpeed() * 16, AllConfigs.server().kinetics.maxRotationSpeed.get()))
			.forGoggles(tooltip);
		int processingSpeed = Math.max(1, be.getProcessingSpeed());
		var leftTick = (int) Math.ceil(be.timer / (double) processingSpeed);
		if (leftTick == 0) return false;
		CCGLang.of().translate("tooltip.leftTime", GRAY).number(leftTick / 20, GOLD).space().seconds(GRAY).forGoggles(tooltip);
		CCGLang.of().translate("tooltip.expectedOutputs", GRAY).forGoggles(tooltip);
		int inputCount = Math.max(1, be.inputInv.getStackInSlot(0).getCount());
		if (isPlayerSneaking) lastRecipe.getRollableResults().forEach(result -> {
			ItemStack stack = result.getStack();
			float chance = result.getChance();
			MutableComponent label = CCGLang.of()
				.itemName(stack)
				.text(" x", DARK_GRAY)
				.number(chance * 100, AQUA)
				.text("%", DARK_GRAY)
				.component();
			CCGLang.of().itemEntry(stack.copyWithCount(inputCount * stack.getCount()), label).forGoggles(tooltip);
		});
		else lastRecipe.getRollableResults().forEach(result -> {
			ItemStack stack = result.getStack();
			float chance = result.getChance();
			MutableComponent line = CCGLang.of()
				.itemName(stack)
				.text(" x", DARK_GRAY)
				.number(inputCount * stack.getCount() * chance, GOLD)
				.component();
			CCGLang.of().itemEntry(stack.copyWithCount(1), line).forGoggles(tooltip);
		});
		return true;
	}
	public static boolean fluidTransport(List<Component> tooltip, SmartBlockEntity be) {
		if (!CCG.config.goggles.fluidTransport) return false;
		FluidTransportBehaviour behaviour = be.getBehaviour(FluidTransportBehaviour.TYPE);
		if (behaviour == null) return false;
		BlockState state = be.getBlockState();
		var added = false;
		for (Direction direction : Direction.values()) {
			if (!behaviour.canHaveFlowToward(state, direction)) continue;
			if (!added) {
				CCGLang.of().translate("tooltip.fluidTransport").forGoggles(tooltip);
				added = true;
			}
			CCGLangBuilder line = CCGLang.of().translate("tooltip.direction." + direction.getName(), GRAY).space();
			Flow flow = behaviour.getFlow(direction);
			PipeConnection connection = behaviour.getConnection(direction);
			if (flow == null || connection == null) line.text("—", DARK_GRAY).space().number(0, DARK_GRAY).space();
			else {
				Couple<Float> pressure = connection.getPressure();
				float maxPressure = Math.max(pressure.get(true), pressure.get(false));
				int rate = maxPressure <= 0 ? 0 : Math.max(1, (int) (maxPressure / 2));
				int color = Outliner.getColor(!flow.inbound);
				if (flow.inbound) line.add(CCGLang.translateDirect("tooltip.fluidFlow.in").withStyle(s -> s.withColor(color)));
				else line.add(CCGLang.translateDirect("tooltip.fluidFlow.out").withStyle(s -> s.withColor(color)));
				line.space().add(flow.fluid.getHoverName()).space().number(rate, AQUA).space();
			}
			line.add(CCGCreateLang.translateDirect("generic.unit.millibuckets").withStyle(DARK_GRAY)).text("/t", DARK_GRAY);
			line.forGoggles(tooltip, 1);
		}
		return added;
	}
	public static boolean fluidTank(List<Component> tooltip, FluidTankBlockEntity be) {
		if (!CCG.config.goggles.fluidTankThroughput) return false;
		FluidTankBlockEntity controller = be.getControllerBE();
		if (controller == null) return false;
		double rate = ((FluidTankThroughputData) controller).ccg$getRate();
		if (rate < 0.5) return false;
		CCGLang.of().translate("tooltip.fluidThroughput").forGoggles(tooltip);
		CCGLang.of()
			.number(Math.round(rate), AQUA)
			.space()
			.add(CCGCreateLang.translateDirect("generic.unit.millibuckets").withStyle(DARK_GRAY))
			.text("/t", DARK_GRAY)
			.forGoggles(tooltip, 1);
		return true;
	}
}
