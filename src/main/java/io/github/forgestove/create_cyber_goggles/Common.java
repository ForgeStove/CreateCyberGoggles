package io.github.forgestove.create_cyber_goggles;
import com.simibubi.create.content.equipment.armor.CardboardArmorItem;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity.FuelType;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.infrastructure.config.AllConfigs;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;
public class Common {
	public static StockTickerBlockEntity lastSTBE;
	public static int index = 1, scrollDeltaY;
	/**
	 * 测试玩家是否穿着全套纸板盔甲并且不在飞行状态。
	 *
	 * @param player 本地玩家实体
	 */
	public static boolean testForStealth(LocalPlayer player) {
		var allMatch = Stream.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)
			.allMatch(slot -> player.getItemBySlot(slot).getItem() instanceof CardboardArmorItem);
		return CCG.CONFIG.chainConveyor.cardBoardedYourself && !player.getAbilities().flying && allMatch;
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
		var x = guiGraphics.guiWidth() / 2 + AllConfigs.client().overlayOffsetX.get();
		var y = guiGraphics.guiHeight() / 2 + AllConfigs.client().overlayOffsetY.get();
		guiGraphics.renderItem(itemStack, x + 10, y - 16);
		guiGraphics.renderItemDecorations(font, itemStack, x + 10, y - 16);
		guiGraphics.renderTooltip(font, itemStack, x + 22, y - height);
	}
	/**
	 * 获取当前玩家选中的方块实体，并将其转换为 {@link KineticBlockEntity} 类型。
	 * 如果选中的方块实体不是 {@link KineticBlockEntity} 类型，则返回 null。
	 *
	 * @return 当前选中的 {@link KineticBlockEntity} 实例，如果没有选中或类型不匹配则返回 null
	 */
	public static @Nullable KineticBlockEntity getKBE() {
		if (!(getBE() instanceof KineticBlockEntity kbe)) return null;
		return kbe;
	}
	/**
	 * 获取当前玩家选中的方块实体。
	 * 如果没有选中方块或选中的方块不是 {@link BlockEntity} 类型，则返回 null。
	 *
	 * @return 当前选中的 {@link BlockEntity} 实例，如果没有选中或类型不匹配则返回 null
	 */
	public static @Nullable BlockEntity getBE() {
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
	public static @Nullable Block getB() {
		var mc = Minecraft.getInstance();
		if (mc.level == null) return null;
		if (!(mc.hitResult instanceof BlockHitResult blockHitResult)) return null;
		if (!(blockHitResult.getType() == Type.BLOCK)) return null;
		return mc.level.getBlockState(blockHitResult.getBlockPos()).getBlock();
	}
	/**
	 * 为风扇组件添加悬浮提示信息。
	 * 此方法根据风扇的推/拉状态和作用范围，格式化并添加相应的提示文本。
	 *
	 * @param tooltip 需要添加提示信息的组件列表
	 * @param pushing 风扇是否处于推动模式（true为推动，false为拉动）
	 * @param range   风扇的作用范围（原始值）
	 * @param divide  范围除数，用于计算显示的实际范围值
	 */
	public static boolean addFanTooltip(List<Component> tooltip, boolean pushing, float range, int divide) {
		if (range == 0) return false;
		CCGLang.translate("tooltip.windState").forGoggles(tooltip);
		CCGLang.number(range / divide)
			.space()
			.translate(pushing ? "tooltip.pushRange" : "tooltip.pullRange")
			.color(pushing ? CCG.CONFIG.delayRender.windPushColor : CCG.CONFIG.delayRender.windPullColor)
			.forGoggles(tooltip);
		return true;
	}
	/**
	 * 为燃烧室添加悬浮提示信息，显示燃烧状态、剩余燃烧时间和燃料类型颜色标识
	 *
	 * @param tooltip           用于显示提示信息的组件列表
	 * @param remainingBurnTime 剩余燃烧时间（单位：tick）
	 * @param isCreative        是否为创造模式燃烧室
	 * @param activeFuel        当前激活的燃料类型
	 */
	public static boolean addBurnerTooltip(List<Component> tooltip, int remainingBurnTime, boolean isCreative, FuelType activeFuel) {
		CCGLang.translate("tooltip.burnerState").forGoggles(tooltip);
		CCGLang.text(isCreative ? "∞" : String.format("%.2f", remainingBurnTime / 20f))
			.text(String.format(" / %d ", BlazeBurnerBlockEntity.MAX_HEAT_CAPACITY / 20))
			.translate("tooltip.seconds")
			.style(switch (activeFuel) {
				case SPECIAL -> ChatFormatting.AQUA;
				case NORMAL -> ChatFormatting.YELLOW;
				default -> ChatFormatting.GRAY;
			})
			.forGoggles(tooltip);
		return true;
	}
	/**
	 * 获取当前上下文中的相关过滤器物品。
	 * 此方法有两种工作模式：
	 * <p>
	 * 1. 如果玩家正在查看GUI界面，则返回鼠标悬停位置的物品
	 * <p>
	 * 2. 如果玩家在游戏世界中，则尝试从目标方块实体获取过滤器物品
	 *
	 * @return 相关的过滤器物品，如果无法获取则返回null
	 */
	public static @Nullable ItemStack getRelevantFilterItem() {
		var mc = Minecraft.getInstance();
		if (mc.screen != null) {
			if (!(mc.screen instanceof AbstractContainerScreen<?> screen)) return null;
			var slot = screen.getSlotUnderMouse();
			return slot == null ? null : slot.getItem();
		}
		if (!(getBE() instanceof SmartBlockEntity sbe) || !(mc.hitResult instanceof BlockHitResult blockHitResult)) return null;
		var behaviour = sbe.getBehaviour(FilteringBehaviour.TYPE);
		return behaviour == null ? null : behaviour.getFilter(blockHitResult.getDirection());
	}
	/**
	 * 向本地玩家显示客户端消息。
	 * 此方法将 LangBuilder 构建的组件显示为覆盖游戏界面的状态栏消息。
	 *
	 * @param builder 包含要显示消息内容的语言构建器
	 * @param player  接收消息的本地玩家实例
	 */
	public static void displayClientMessage(LangBuilder builder, LocalPlayer player) {
		player.displayClientMessage(builder.component(), true);
	}
}
