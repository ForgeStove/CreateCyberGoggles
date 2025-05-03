package com.forgestove.create_cyber_goggles;
import com.simibubi.create.AllItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
public class Util {
	/**
	 * 测试玩家是否穿着全套纸板盔甲并且不在飞行状态。
	 *
	 * @param player 本地玩家实体
	 */
	public static boolean testForStealth(LocalPlayer player) {
		return CreateCyberGoggles.config.chainConveyor.cardBoardedYourself
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
	 * @implNote 渲染位置逻辑：
	 * 	<p>物品图标绘制在屏幕水平中央偏右10像素、垂直中央偏上15像素的位置
	 * 	<p>物品装饰层（如数量文本）叠加在图标相同位置
	 * 	<p>悬浮提示框根据提示行数自适应高度，水平位置位于图标右侧12像素
	 * 	<p>垂直位置根据提示行数动态计算以避免溢出屏幕
	 */
	public static void renderItemStack(GuiGraphics guiGraphics, ItemStack itemStack) {
		if (itemStack == null || itemStack.isEmpty()) return;
		var mc = Minecraft.getInstance();
		var font = mc.font;
		var tooltipFlag = mc.options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL;
		var tooltipLines = itemStack.getTooltipLines(Item.TooltipContext.of(mc.level), mc.player, tooltipFlag);
		var height = Math.max(10, tooltipLines.size() * font.lineHeight - 60);
		var x = guiGraphics.guiWidth() / 2;
		var y = guiGraphics.guiHeight() / 2;
		guiGraphics.renderItem(itemStack, x + 10, y - 15);
		guiGraphics.renderItemDecorations(font, itemStack, x + 10, y - 15);
		guiGraphics.renderTooltip(font, itemStack, x + 22, y - height);
	}
}
