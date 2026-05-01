package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import dev.simulated_team.simulated.content.blocks.spring.*;
import dev.simulated_team.simulated.content.items.spring.SpringItemHandler;
import io.github.forgestove.create_cyber_goggles.core.api.Self;
import io.github.forgestove.create_cyber_goggles.core.util.CCGLang;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;

import java.util.List;
@Mixin(value = SpringBlockEntity.class, remap = false)
public abstract class SpringBlockEntityMixin extends SmartBlockEntity implements IHaveGoggleInformation, Self<SpringBlockEntity> {
	@Shadow protected double desiredLength;
	@Shadow protected LerpedFloat renderLength;
	public SpringBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		CCGLang.translate("tooltip.spring").forGoggles(tooltip);
		CCGLang.translate("tooltip.size", ChatFormatting.GRAY)
			.translate("tooltip.size." + getBlockState().getValue(SpringBlock.SIZE).getSerializedName())
			.forGoggles(tooltip);
		CCGLang.translate("tooltip.spring.isController", ChatFormatting.GRAY).is(thiz().isController()).forGoggles(tooltip);
		CCGLang.translate("tooltip.spring.desiredLength", ChatFormatting.GRAY)
			.fraction(desiredLength, SpringItemHandler.MAX_LENGTH)
			.forGoggles(tooltip);
		CCGLang.translate("tooltip.spring.currentLength", ChatFormatting.GRAY)
			.number(renderLength.getValue())
			.forGoggles(tooltip);
		return true;
	}
}
