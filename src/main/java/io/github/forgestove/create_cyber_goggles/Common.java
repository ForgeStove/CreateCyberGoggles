package io.github.forgestove.create_cyber_goggles;
import com.zurrtum.create.AllSoundEvents.SoundEntry;
import com.zurrtum.create.client.catnip.lang.LangBuilder;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.zurrtum.create.content.equipment.armor.CardboardArmorItem;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlockEntity.FuelType;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import io.github.forgestove.create_cyber_goggles.mixin.accessor.AbstractContainerScreenAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.*;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.Shapes;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;
public class Common {
	public static StockTickerBlockEntity lastSTBE;
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
		var x = guiGraphics.guiWidth() / 2 + 20;
		var y = guiGraphics.guiHeight() / 2;
		guiGraphics.renderItem(itemStack, x + 10, y - 16);
		guiGraphics.renderItemDecorations(font, itemStack, x + 10, y - 16);
		guiGraphics.renderTooltip(
			mc.font,
			tooltipLines.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).toList(),
			x + 22,
			y - height,
			DefaultTooltipPositioner.INSTANCE,
			null
		);
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
			var accessor = (AbstractContainerScreenAccessor) screen;
			var slot = accessor.getHoveredSlot();
			return slot == null ? null : slot.getItem();
		}
		if (!(getBE() instanceof SmartBlockEntity sbe) || !(mc.hitResult instanceof BlockHitResult blockHitResult)) return null;
		var behaviour = sbe.getBehaviour(FilteringBehaviour.TYPE);
		return behaviour == null ? ItemStack.EMPTY : behaviour.getFilter(blockHitResult.getDirection());
	}
	/**
	 * 向本地玩家显示客户端消息。
	 * 此方法将 LangBuilder 构建的组件显示为覆盖游戏界面的状态栏消息。
	 *
	 * @param builder 包含要显示消息内容的语言构建器
	 */
	public static void displayMessage(LangBuilder builder) {
		var player = Minecraft.getInstance().player;
		if (player != null) player.displayClientMessage(builder.component(), true);
	}
	/**
	 * 播放指定的音效，可自定义音调和音量。
	 *
	 * @param sound  要播放的音效事件
	 * @param pitch  音调值，影响播放速度和音高
	 * @param volume 音量大小，1.0f为正常音量
	 */
	public static void playSound(SoundEvent sound, float pitch, float volume) {
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume));
	}
	/**
	 * 以预设的音调和音量播放Create模组的音效条目。
	 * 默认使用较低的音调(0.25f)和正常音量(1.0f)。
	 *
	 * @param entry Create模组的音效条目
	 */
	public static void playSound(SoundEntry entry) {
		playSound(entry.getMainEvent(), .25f, 1f);
	}
	/**
	 * 以默认音调和音量播放指定的音效。
	 * 使用正常音调(1.0f)和正常音量(1.0f)。
	 *
	 * @param sound 要播放的音效事件
	 */
	public static void playSound(SoundEvent sound) {
		playSound(sound, 1f, 1f);
	}
	public static @Nullable AABB getBounds(BlockPos blockPos) {
		var level = Minecraft.getInstance().level;
		if (level == null) return null;
		var shape = level.getBlockState(blockPos).getShape(level, blockPos);
		return (shape.isEmpty() ? Shapes.block() : shape).bounds().move(blockPos);
	}
}
