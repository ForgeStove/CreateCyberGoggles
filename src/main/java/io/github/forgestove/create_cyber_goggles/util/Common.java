package io.github.forgestove.create_cyber_goggles.util;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.logistics.filter.*;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.createmod.catnip.gui.ScreenOpener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import org.jetbrains.annotations.*;
public class Common {
	public static StockTickerBlockEntity laststbe;
	public static int index = 1, scrollDeltaY = 0;
	/**
	 * 测试玩家是否穿着全套纸板盔甲并且不在飞行状态。
	 *
	 * @param player 本地玩家实体
	 */
	public static boolean testForStealth(LocalPlayer player) {
		return CCG.CONFIG.chainConveyor.cardBoardedYourself
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
		var tooltipLines = itemStack.getTooltipLines(TooltipContext.of(mc.level), mc.player, tooltipFlag);
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
		if (mc.level == null) return null;
		if (!(mc.hitResult instanceof BlockHitResult blockHitResult)) return null;
		if (!(blockHitResult.getType() == Type.BLOCK)) return null;
		return mc.level.getBlockEntity(blockHitResult.getBlockPos());
	}
	/**
	 * 获取当前玩家选中的方块。
	 * 如果没有选中方块或选中的方块不是 {@link Block} 类型，则返回 null。
	 *
	 * @return 当前选中的 {@link Block} 实例，如果没有选中或类型不匹配则返回 null
	 */
	public static @Nullable Block getSelectedB() {
		var mc = Minecraft.getInstance();
		if (mc.level == null) return null;
		if (!(mc.hitResult instanceof BlockHitResult blockHitResult)) return null;
		if (!(blockHitResult.getType() == Type.BLOCK)) return null;
		return mc.level.getBlockState(blockHitResult.getBlockPos()).getBlock();
	}
	/**
	 * 显示一条格式化的客户端消息。
	 *
	 * @param currentValue 当前值，用于确定消息的启用或禁用状态
	 */
	public static void displayClientMessage(boolean currentValue) {
		var mc = Minecraft.getInstance();
		var player = mc.player;
		if (player == null || mc.screen != null) return;
		player.displayClientMessage(
			currentValue
				? Component.translatable("message.create_cyber_goggles.enableDivingAffect")
				: Component.translatable("message.create_cyber_goggles.disableDivingAffect"), true
		);
	}
	/**
	 * 打开与指定过滤器物品相关的筛选器界面。
	 *
	 * @param filter 需要打开筛选器界面的物品堆
	 */
	public static void openFilterScreen(@NotNull ItemStack filter) {
		if (!(filter.getItem() instanceof FilterItem filterItem)) return;
		var mc = Minecraft.getInstance();
		if (mc.player == null) return;
		var inv = mc.player.getInventory();
		var name = filter.getHoverName();
		ScreenOpener.open(switch (filterItem.type) {
			case REGULAR -> new FilterScreen(FilterMenu.create(-1, inv, filter), inv, name);
			case ATTRIBUTE -> new AttributeFilterScreen(AttributeFilterMenu.create(-1, inv, filter), inv, name);
			case PACKAGE -> new PackageFilterScreen(PackageFilterMenu.create(-1, inv, filter), inv, name);
		});
	}
}
