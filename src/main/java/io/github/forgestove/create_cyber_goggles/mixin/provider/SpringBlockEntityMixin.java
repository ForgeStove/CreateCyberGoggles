package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import dev.simulated_team.simulated.content.blocks.spring.*;
import dev.simulated_team.simulated.content.items.spring.SpringItemHandler;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.util.CCGLang;
import io.github.forgestove.create_cyber_goggles.core.util.contract.Self;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;

import java.util.List;
@Mixin(SpringBlockEntity.class)
public abstract class SpringBlockEntityMixin extends SmartBlockEntity implements IHaveGoggleInformation, Self<SpringBlockEntity> {
	@Shadow protected LerpedFloat renderLength;
	public SpringBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}
	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		if (!CCG.config.goggles.enhancedInfo) return false;
		CCGLang.add(Component.translatable("create_cyber_goggles.tooltip.spring")).forGoggles(tooltip);
		var size = switch (getBlockState().getValue(SpringBlock.SIZE).getSerializedName()) {
			case "small" -> Component.translatable("create_cyber_goggles.tooltip.size.small");
			case "medium" -> Component.translatable("create_cyber_goggles.tooltip.size.medium");
			case "large" -> Component.translatable("create_cyber_goggles.tooltip.size.large");
			default -> Component.translatable("multiplayer.status.unknown");
		};
		CCGLang.add(Component.translatable("create_cyber_goggles.tooltip.size").withStyle(ChatFormatting.GRAY))
			.add(size)
			.forGoggles(tooltip);
		CCGLang.add(Component.translatable("create_cyber_goggles.tooltip.spring.isController").withStyle(ChatFormatting.GRAY))
			.is(thiz().isController())
			.forGoggles(tooltip);
		CCGLang.add(Component.translatable("create_cyber_goggles.tooltip.spring.currentLength").withStyle(ChatFormatting.GRAY))
			.fraction(renderLength.getValue(), SpringItemHandler.MAX_LENGTH)
			.forGoggles(tooltip);
		return true;
	}
}
