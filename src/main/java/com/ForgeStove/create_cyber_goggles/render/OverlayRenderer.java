package com.ForgeStove.create_cyber_goggles.render;
import com.ForgeStove.create_cyber_goggles.config.*;
import com.simibubi.create.content.logistics.depot.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.LayeredDraw.Layer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.*;
import net.minecraft.world.item.TooltipFlag.Default;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;
public class OverlayRenderer {
	public static final Layer OVERLAY = OverlayRenderer::renderOverlay;
	public static void renderOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
		if (!Config.enhancedInfo.get()) return;
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player != null && player.hasContainerOpen() || mc.isPaused()) return;
		ItemStack itemStack = ItemStack.EMPTY;
		ClientLevel level = mc.level;
		if (level != null && mc.hitResult instanceof BlockHitResult blockHitResult) {
			BlockPos blockPos = blockHitResult.getBlockPos();
			if (blockHitResult.getType() == HitResult.Type.MISS) return;
			Block block = level.getBlockState(blockPos).getBlock();
			if (block instanceof DepotBlock depotBlock) {
				DepotBlockEntity blockEntity = depotBlock.getBlockEntity(level, blockPos);
				if (blockEntity != null) itemStack = blockEntity.getHeldItem();
			}
		}
		renderItemStack(guiGraphics, itemStack, mc);
	}
	public static void renderItemStack(GuiGraphics guiGraphics, @NotNull ItemStack itemStack, Minecraft mc) {
		if (itemStack.isEmpty()) return;
		Font font = mc.font;
		Default tooltipFlag = mc.options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL;
		List<Component> tooltipLines = itemStack.getTooltipLines(TooltipContext.of(mc.level), mc.player, tooltipFlag);
		int tooltipHeight = tooltipLines.size() * font.lineHeight + 8;
		int x = guiGraphics.guiWidth() / 2;
		int y = guiGraphics.guiHeight() / 2;
		guiGraphics.renderItem(itemStack, x + 10, y - 15);
		guiGraphics.renderItemDecorations(font, itemStack, x + 10, y - 15);
		guiGraphics.renderComponentTooltip(font, tooltipLines, x + 20, y - Math.max(0, tooltipHeight - 80));
	}
}
