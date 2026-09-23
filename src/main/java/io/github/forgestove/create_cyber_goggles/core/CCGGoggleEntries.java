package io.github.forgestove.create_cyber_goggles.core;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.equipment.armor.BacktankBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelControllerBlockEntity;
import com.simibubi.create.content.kinetics.fan.*;
import com.simibubi.create.content.kinetics.millstone.*;
import com.simibubi.create.content.logistics.depot.*;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.content.redstone.diodes.BrassDiodeBlockEntity;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.api.GoggleTooltip;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import io.github.forgestove.create_cyber_goggles.core.util.contract.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;

import java.util.Optional;
/**
 * CCG 内建护目镜悬浮内容条目的自注册引导。
 * <p>在 common 入口 {@link CCG} 构造时调用；provider 均双端安全（服务端 Jade 收集路径会走到）。</p>
 */
public final class CCGGoggleEntries {
	public static void register() {
		GoggleTooltip.register(BlazeBurnerBlockEntity.class, id("burner"), (be, tooltip, sneaking) -> {
			var data = (BurnerTooltipData) be;
			return GoggleTooltipUtil.burner(tooltip, data.ccg$getRemainingBurnTime(), ((BlazeBurnerBlockEntity) be).isCreative, data.ccg$getActiveFuel());
		});
		GoggleTooltip.register(
			RedstoneRequesterBlockEntity.class,
			id("redstone_requester"),
			(be, tooltip, sneaking) -> GoggleTooltipUtil.redstoneRequester(tooltip, ((RedstoneRequesterBlockEntity) be).encodedRequest.stacks())
		);
		GoggleTooltip.register(NozzleBlockEntity.class, id("nozzle"), (be, tooltip, sneaking) -> {
			var data = (NozzleTooltipData) be;
			return GoggleTooltipUtil.fan(tooltip, data.ccg$getPushing(), data.ccg$getRange() / 2F);
		});
		GoggleTooltip.register(BrassDiodeBlockEntity.class, id("pulse"), (be, tooltip, sneaking) -> {
			var data = (PulseTooltipData) be;
			return GoggleTooltipUtil.pulse(tooltip, data.ccg$getState(), data.ccg$getMaxState());
		});
		GoggleTooltip.register(
			CrushingWheelControllerBlockEntity.class,
			id("crushing_controller"),
			(be, tooltip, sneaking) -> GoggleTooltipUtil.crushingController(tooltip, (CrushingWheelControllerBlockEntity) be)
		);
		GoggleTooltip.register(
			DepotBlockEntity.class,
			id("depot"),
			(be, tooltip, sneaking) -> GoggleTooltipUtil.depot(tooltip, ((DepotBlockEntity) be).getBehaviour(DepotBehaviour.TYPE).itemHandler)
		);
		GoggleTooltip.register(
			SchematicannonBlockEntity.class,
			id("cannon"),
			(be, tooltip, sneaking) -> GoggleTooltipUtil.cannon(tooltip, (SchematicannonBlockEntity) be)
		);
		GoggleTooltip.register(BacktankBlockEntity.class, id("backtank"), (be, tooltip, sneaking) -> {
			var data = (BacktankTooltipData) be;
			return GoggleTooltipUtil.backtank(tooltip, (BacktankBlockEntity) be, data.ccg$getCapacityEnchantLevel(), data.ccg$getLeftTick());
		});
		GoggleTooltip.registerNative(BacktankBlockEntity.class);
		GoggleTooltip.register(EncasedFanBlockEntity.class, id("fan"), (be, tooltip, sneaking) -> {
			var airCurrent = ((EncasedFanBlockEntity) be).getAirCurrent();
			return GoggleTooltipUtil.fan(tooltip, airCurrent.pushing, airCurrent.maxDistance);
		});
		GoggleTooltip.registerNative(EncasedFanBlockEntity.class);
		GoggleTooltip.registerNative(BeltBlockEntity.class);
		GoggleTooltip.register(BeltBlockEntity.class, id("belt"), (be, tooltip, sneaking) -> {
			if (!CCG.config.goggles.enhancedInfo) return false;
			var belt = (BeltBlockEntity) be;
			CCGLang.add(Component.translatable("create_cyber_goggles.tooltip.isController").withStyle(ChatFormatting.GRAY))
				.is(belt.isController())
				.forGoggles(tooltip);
			if (belt.getSpeed() == 0) return false;
			var controllerBE = belt.getControllerBE();
			if (controllerBE != null) GoggleTooltipUtil.belt(tooltip, ((BeltTooltipData) controllerBE).ccg$getRate());
			return false;
		});
		GoggleTooltip.registerNative(MillstoneBlockEntity.class);
		GoggleTooltip.register(MillstoneBlockEntity.class, id("millstone"), (be, tooltip, sneaking) -> {
			var mbe = (MillstoneBlockEntity) be;
			var level = mbe.getLevel();
			if (level == null) return false;
			Optional<RecipeHolder<MillingRecipe>> recipe = AllRecipeTypes.MILLING.find(new RecipeWrapper(mbe.inputInv), level);
			if (recipe.isEmpty()) return false;
			return GoggleTooltipUtil.millstone(tooltip, mbe, recipe.get().value());
		});
	}
	private static ResourceLocation id(String name) {
		return ResourceLocation.fromNamespaceAndPath(CCG.ID, name);
	}
}
