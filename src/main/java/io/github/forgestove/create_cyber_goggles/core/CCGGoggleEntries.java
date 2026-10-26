package io.github.forgestove.create_cyber_goggles.core;
import com.simibubi.create.content.equipment.armor.BacktankBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelControllerBlockEntity;
import com.simibubi.create.content.kinetics.fan.*;
import com.simibubi.create.content.kinetics.millstone.MillstoneBlockEntity;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.content.redstone.diodes.BrassDiodeBlockEntity;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity;
import io.github.forgestove.create_cyber_goggles.api.GoggleTooltip;
import io.github.forgestove.create_cyber_goggles.core.util.GoggleTooltipUtil;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.getCCGRes;
public final class CCGGoggleEntries {
	public static void register() {
		GoggleTooltip.register(BlazeBurnerBlockEntity.class, getCCGRes("burner"), GoggleTooltipUtil::burner);
		GoggleTooltip.register(RedstoneRequesterBlockEntity.class, getCCGRes("redstone_requester"), GoggleTooltipUtil::redstoneRequester);
		GoggleTooltip.register(NozzleBlockEntity.class, getCCGRes("nozzle"), GoggleTooltipUtil::nozzle);
		GoggleTooltip.register(BrassDiodeBlockEntity.class, getCCGRes("pulse"), GoggleTooltipUtil::pulse);
		GoggleTooltip.register(
			CrushingWheelControllerBlockEntity.class,
			getCCGRes("crushing_controller"),
			GoggleTooltipUtil::crushingController
		);
		GoggleTooltip.register(DepotBlockEntity.class, getCCGRes("depot"), GoggleTooltipUtil::depot);
		GoggleTooltip.register(SchematicannonBlockEntity.class, getCCGRes("cannon"), GoggleTooltipUtil::cannon);
		GoggleTooltip.register(BacktankBlockEntity.class, getCCGRes("backtank"), GoggleTooltipUtil::backtank);
		GoggleTooltip.registerNative(BacktankBlockEntity.class);
		GoggleTooltip.register(EncasedFanBlockEntity.class, getCCGRes("fan"), GoggleTooltipUtil::fan);
		GoggleTooltip.registerNative(EncasedFanBlockEntity.class);
		GoggleTooltip.registerNative(BeltBlockEntity.class);
		GoggleTooltip.register(BeltBlockEntity.class, getCCGRes("belt"), GoggleTooltipUtil::belt);
		GoggleTooltip.registerNative(MillstoneBlockEntity.class);
		GoggleTooltip.register(MillstoneBlockEntity.class, getCCGRes("millstone"), GoggleTooltipUtil::millstone);
	}
}
