package io.github.forgestove.create_cyber_goggles.core.util;
import com.simibubi.create.AllSoundEvents.SoundEntry;
import com.simibubi.create.Create;
import com.simibubi.create.content.equipment.armor.CardboardArmorItem;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.Shapes;
import org.jetbrains.annotations.*;

import java.awt.Color;
import java.util.stream.Stream;
public class CCGUtil {
	@Contract(pure = true)
	public static boolean isInGUI() {
		return Minecraft.getInstance().screen != null;
	}
	@Contract(pure = true)
	public static boolean isInGame() {
		return !isInGUI();
	}
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
	 * 获取当前玩家选中的方块实体。
	 * 如果没有选中方块或选中的方块不是{@link BlockEntity}类型，则返回{@code null}。
	 *
	 * @return 当前选中的{@link BlockEntity}实例，如果没有选中或类型不匹配则返回{@code null}
	 */
	public static @Nullable BlockEntity getBE() {
		var mc = Minecraft.getInstance();
		if (mc.level == null) return null;
		if (!(mc.hitResult instanceof BlockHitResult bhr)) return null;
		return bhr.getType() == Type.BLOCK ? mc.level.getBlockEntity(bhr.getBlockPos()) : null;
	}
	public static <T> @Nullable T getAs(@NotNull Class<T> clazz, Object object) {
		return clazz.isInstance(object) ? clazz.cast(object) : null;
	}
	public static <T> T getBE(Class<T> clazz) {
		return getAs(clazz, getBE());
	}
	/**
	 * 获取当前玩家选中的实体。
	 * 如果没有选中实体或选中的不是实体类型，则返回{@code null}。
	 *
	 * @return 当前选中的{@link Entity}实例，如果没有选中或类型不匹配则返回{@code null}
	 */
	public static @Nullable Entity getE() {
		var mc = Minecraft.getInstance();
		if (!(mc.hitResult instanceof EntityHitResult ehr)) return null;
		return ehr.getType() == Type.ENTITY ? ehr.getEntity() : null;
	}
	/**
	 * 获取当前玩家选中的方块。
	 * 如果没有选中方块或选中的方块不是{@link Block}类型，则返回{@code null}。
	 *
	 * @return 当前选中的{@link Block}实例，如果没有选中或类型不匹配则返回{@code null}
	 */
	public static @Nullable Block getB() {
		var mc = Minecraft.getInstance();
		if (mc.level == null) return null;
		if (!(mc.hitResult instanceof BlockHitResult blockHitResult)) return null;
		if (!(blockHitResult.getType() == Type.BLOCK)) return null;
		return mc.level.getBlockState(blockHitResult.getBlockPos()).getBlock();
	}
	/**
	 * 获取当前上下文中的相关过滤器物品。
	 * 此方法有两种工作模式：
	 * <p>
	 * 1. 如果玩家正在查看GUI界面，则返回鼠标悬停位置的物品
	 * <p>
	 * 2. 如果玩家在游戏世界中，则尝试从目标方块实体获取过滤器物品
	 *
	 * @return 相关的过滤器物品，如果无法获取则返回{@code null}
	 */
	public static @Nullable ItemStack getRelevantFilterItem() {
		var mc = Minecraft.getInstance();
		if (isInGUI()) {
			if (!(mc.screen instanceof AbstractContainerScreen<?> screen)) return null;
			var slot = screen.getSlotUnderMouse();
			return slot == null ? null : slot.getItem();
		}
		if (!(getBE() instanceof SmartBlockEntity sbe) || !(mc.hitResult instanceof BlockHitResult blockHitResult)) return null;
		var behaviour = sbe.getBehaviour(FilteringBehaviour.TYPE);
		return behaviour == null ? ItemStack.EMPTY : behaviour.getFilter(blockHitResult.getDirection());
	}
	/**
	 * 向本地玩家显示客户端消息。
	 * <p>
	 * 此方法将{@link LangBuilder}构建的组件显示为覆盖游戏界面的状态栏消息。
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
	 * 以预设的音调和音量播放{@link Create}模组的音效条目。
	 * 默认使用较低的音调{@code 0.25f}和正常音量{@code 1.0f}。
	 *
	 * @param entry {@link Create}模组的音效条目
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
	/**
	 * 获取指定方块位置的包围盒{@link AABB}。
	 * <p>
	 * 如果{@code ClientLevel}为{@code null}，则返回{@code null}。
	 * <p>
	 * 若方块形状为空，则使用完整方块形状{@code Shapes.block()}。
	 *
	 * @param blockPos 方块位置
	 * @return 该方块的{@link AABB}包围盒，若无法获取则返回{@code null}
	 */
	public static @Nullable AABB getBounds(BlockPos blockPos) {
		var level = Minecraft.getInstance().level;
		if (level == null) return null;
		var shape = level.getBlockState(blockPos).getShape(level, blockPos);
		return (shape.isEmpty() ? Shapes.block() : shape).bounds().move(blockPos);
	}
	/**
	 * 根据进度值生成渐变色。
	 * <p>
	 * 使用HSB色彩空间，色相随进度变化，饱和度和亮度固定为1.0。
	 *
	 * @param progress 渐变进度，范围通常为0.0~1.0
	 * @return 代表渐变色的RGB整数值
	 */
	@Contract(pure = true)
	public static int getGradientColor(float progress) {
		return Color.HSBtoRGB(progress * 0.33f, 1.0f, 1.0f);
	}
}
