package io.github.forgestove.create_cyber_goggles.mixin.compact.simulated.provider;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import dev.simulated_team.simulated.content.blocks.spring.*;
import dev.simulated_team.simulated.content.items.spring.SpringItemHandler;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.CCGLang;
import io.github.forgestove.create_cyber_goggles.core.util.contract.Self;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;

import java.util.List;
@Pseudo
@Mixin(SpringBlockEntity.class)
public abstract class SpringBlockEntityMixin extends SmartBlockEntity implements IHaveGoggleInformation, Self<SpringBlockEntity> {
	public SpringBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		if (!CCG.config.goggles.enhancedInfo) return false;
		CCGLang.translate("tooltip.spring").forGoggles(tooltip);
		MutableComponent size = switch (getBlockState().getValue(SpringBlock.SIZE).getSerializedName()) {
			case "small" -> CCGLang.translateDirect("tooltip.size.small");
			case "medium" -> CCGLang.translateDirect("tooltip.size.medium");
			case "large" -> CCGLang.translateDirect("tooltip.size.large");
			default -> Component.translatable("multiplayer.status.unknown");
		};
		CCGLang.translate("tooltip.size", ChatFormatting.GRAY).add(size).forGoggles(tooltip);
		CCGLang.translate("tooltip.isController", ChatFormatting.GRAY).is(thiz().isController()).forGoggles(tooltip);
		CCGLang.translate("tooltip.spring.currentLength", ChatFormatting.GRAY)
			.fraction(thiz().getRenderLength(1), SpringItemHandler.MAX_LENGTH)
			.forGoggles(tooltip);
		return true;
	}
}
