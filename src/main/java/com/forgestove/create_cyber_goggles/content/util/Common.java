package com.forgestove.create_cyber_goggles.content.util;
import com.forgestove.create_cyber_goggles.content.config.CCGConfig;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
public class Common {
	/**
	 * 测试玩家是否穿着全套纸板盔甲并且不在飞行状态。
	 *
	 * @param player 本地玩家实体
	 */
	public static boolean testForStealth(LocalPlayer player) {
		return CCGConfig.get().chainConveyor.cardBoardedYourself
			&& !player.getAbilities().flying
			&& AllItems.CARDBOARD_HELMET.isIn(player.getItemBySlot(EquipmentSlot.HEAD))
			&& AllItems.CARDBOARD_CHESTPLATE.isIn(player.getItemBySlot(EquipmentSlot.CHEST))
			&& AllItems.CARDBOARD_LEGGINGS.isIn(player.getItemBySlot(EquipmentSlot.LEGS))
			&& AllItems.CARDBOARD_BOOTS.isIn(player.getItemBySlot(EquipmentSlot.FEET));
	}
	/**
	 * 在屏幕中央区域渲染指定物品堆的图标及关联的悬浮提示信息。
	 *
	 * @param guiGraphics GUI渲染上下文对象，用于执行图形绘制操作
	 * @param itemStack   需要渲染的物品堆实例。若值为null或空物品堆叠时方法立即返回
	 */
	public static void renderItemStack(GuiGraphics guiGraphics, ItemStack itemStack) {
		if (itemStack == null || itemStack.isEmpty()) return;
		var mc = Minecraft.getInstance();
		var font = mc.font;
		var tooltipFlag = mc.options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL;
		var tooltipLines = itemStack.getTooltipLines(mc.player, tooltipFlag);
		var height = Math.max(10, tooltipLines.size() * font.lineHeight - 60);
		var x = guiGraphics.guiWidth() / 2;
		var y = guiGraphics.guiHeight() / 2;
		guiGraphics.renderItem(itemStack, x + 10, y - 15);
		guiGraphics.renderItemDecorations(font, itemStack, x + 10, y - 15);
		guiGraphics.renderTooltip(font, itemStack, x + 22, y - height);
	}
	/**
	 * 获取当前玩家选中的方块实体，并将其转换为 {@link KineticBlockEntity} 类型。
	 * 如果选中的方块实体不是 {@link KineticBlockEntity} 类型，则返回 null。
	 *
	 * @return 当前选中的 {@link KineticBlockEntity} 实例，如果没有选中或类型不匹配则返回 null
	 */
	public static @Nullable KineticBlockEntity getSelectedKBE() {
		if (!(getSelectedBE() instanceof KineticBlockEntity kbe)) return null;
		return kbe;
	}
	/**
	 * 获取当前玩家选中的方块实体。
	 * 如果没有选中方块或选中的方块不是 {@link BlockEntity} 类型，则返回 null。
	 *
	 * @return 当前选中的 {@link BlockEntity} 实例，如果没有选中或类型不匹配则返回 null
	 */
	public static @Nullable BlockEntity getSelectedBE() {
		var mc = Minecraft.getInstance();
		var hitResult = mc.hitResult;
		var level = mc.level;
		if (hitResult == null) return null;
		if (level == null) return null;
		if (!(hitResult instanceof BlockHitResult blockHitResult)) return null;
		return level.getBlockEntity(blockHitResult.getBlockPos());
	}
}
