package io.github.forgestove.create_cyber_goggles.core.util;
import com.simibubi.create.Create;
import com.simibubi.create.content.equipment.armor.CardboardArmorItem;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.fml.util.thread.EffectiveSide;
import org.jetbrains.annotations.*;

import java.awt.Color;
import java.util.function.Consumer;
import java.util.stream.Stream;
public class CCGUtil {
	/** 获取当前的 {@link Minecraft} 客户端实例。 */
	public static final Minecraft mc = Minecraft.getInstance();
	public static final Outliner outliner = Outliner.getInstance();
	@Contract(pure = true)
	public static boolean isInGUI() {
		return mc.screen != null;
	}
	@Contract(pure = true)
	public static boolean isInGame() {
		return !isInGUI();
	}
	public static boolean isClient() {
		return EffectiveSide.get().isClient();
	}
	public static boolean isServer() {
		return !isClient();
	}
	/**
	 * 尝试将给定对象转换为指定类型。
	 * <p>
	 * 该方法等价于：{@code object instanceof T ? (T) object : null}
	 *
	 * @param clazz  目标类型的Class对象
	 * @param object 待转换的对象
	 * @param <T>    目标类型
	 * @return 转换后的对象或{@code null}
	 */
	public static <T extends U, U> @Nullable T getAs(@NotNull Class<T> clazz, U object) {
		return clazz.isInstance(object) ? clazz.cast(object) : null;
	}
	/**
	 * 获取当前玩家的方块命中结果。
	 *
	 * @return 当前的 {@link BlockHitResult}，如果不是方块命中则返回 {@code null}
	 */
	@Contract(pure = true)
	public static @Nullable BlockHitResult getBlockHitResult() {
		return mc.hitResult instanceof BlockHitResult result ? result.getType() != Type.MISS ? result : null : null;
	}
	/**
	 * 获取当前玩家的实体命中结果。
	 *
	 * @return 当前的 {@link EntityHitResult}，如果不是实体命中则返回 {@code null}
	 */
	@Contract(pure = true)
	public static @Nullable EntityHitResult getEntityHitResult() {
		return mc.hitResult instanceof EntityHitResult result ? result : null;
	}
	/**
	 * 获取当前玩家选中的方块实体。
	 * 如果没有选中方块或选中的方块不是{@link BlockEntity}类型，则返回{@code null}。
	 *
	 * @return 当前选中的{@link BlockEntity}实例，如果没有选中或类型不匹配则返回{@code null}
	 */
	public static @Nullable BlockEntity getBlockEntity() {
		if (mc.level == null) return null;
		var result = getBlockHitResult();
		if (result == null || result.getType() == Type.MISS) return null;
		return mc.level.getBlockEntity(result.getBlockPos());
	}
	/**
	 * 获取指定类型的方块实体实例。
	 *
	 * @param clazz 目标方块实体的类型
	 * @param <T>   方块实体类型
	 * @return 如果类型匹配则返回对应实例，否则返回{@code null}
	 */
	public static <T extends BlockEntity> @Nullable T getBlockEntity(Class<T> clazz) {
		return getAs(clazz, getBlockEntity());
	}
	/**
	 * 获取当前玩家选中的方块。
	 *
	 * @return 当前选中的{@link Block}实例，如果没有选中或类型不匹配则返回{@code null}
	 */
	public static @Nullable Block getBlock() {
		if (mc.level == null) return null;
		var result = getBlockHitResult();
		if (result == null || result.getType() == Type.MISS) return null;
		return mc.level.getBlockState(result.getBlockPos()).getBlock();
	}
	/**
	 * 获取指定类型的方块实例。
	 *
	 * @param clazz 目标方块的类型
	 * @param <T>   方块类型
	 * @return 如果类型匹配则返回对应实例，否则返回{@code null}
	 */
	public static <T extends Block> @Nullable T getBlock(Class<T> clazz) {
		return getAs(clazz, getBlock());
	}
	/**
	 * 获取当前玩家选中的实体。
	 *
	 * @return 当前选中的{@link Entity}实例，如果没有选中或类型不匹配则返回{@code null}
	 */
	public static @Nullable Entity getEntity() {
		var result = getEntityHitResult();
		return result != null ? result.getEntity() : null;
	}
	/**
	 * 获取当前上下文中的相关过滤器物品。
	 *
	 * @return 相关的过滤器物品，如果无法获取则返回{@code null}
	 */
	public static @Nullable ItemStack getRelevantFilterItem() {
		if (isInGUI()) {
			if (!(mc.screen instanceof AbstractContainerScreen<?> screen)) return null;
			var slot = screen.getSlotUnderMouse();
			return slot == null ? null : slot.getItem();
		}
		var result = getBlockHitResult();
		var sbe = getBlockEntity(SmartBlockEntity.class);
		if (sbe == null || result == null) return null;
		var behaviour = sbe.getBehaviour(FilteringBehaviour.TYPE);
		return behaviour == null ? ItemStack.EMPTY : behaviour.getFilter(result.getDirection());
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
	public static @NotNull AABB getBounds(BlockPos blockPos) {
		if (mc.level == null) return Shapes.block().bounds();
		var shape = mc.level.getBlockState(blockPos).getShape(mc.level, blockPos);
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
	/**
	 * 检测本地玩家是否穿着全套纸板盔甲并且不在飞行状态。
	 */
	public static boolean testForStealth() {
		if (mc.player == null) return false;
		var allMatch = Stream.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)
			.allMatch(slot -> mc.player.getItemBySlot(slot).getItem() instanceof CardboardArmorItem);
		return CCG.CONFIG.chainConveyor.cardBoardedYourself && !mc.player.getAbilities().flying && allMatch;
	}
	/**
	 * 播放指定的音效
	 *
	 * @param sound  音效事件
	 * @param pitch  音高
	 * @param volume 音量
	 */
	public static void playSound(SoundEvent sound, float pitch, float volume) {
		mc.getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch, volume));
	}
	/**
	 * 切换配置项的启用状态，并显示提示消息与播放音效。
	 * <p>
	 * 仅在按键按下且玩家未处于GUI界面时生效。
	 * <p>
	 * 切换后通过{@code setter}设置新状态，显示对应启用/禁用消息，并播放确认或拒绝音效。
	 *
	 * @param keyDown    是否按下相关按键
	 * @param enabled    当前配置项是否启用
	 * @param setter     用于设置配置项状态的回调
	 * @param messageKey 状态切换时显示消息的语言键
	 */
	public static void toggleConfig(boolean keyDown, boolean enabled, Consumer<Boolean> setter, String messageKey) {
		if (!keyDown) return;
		if (isInGUI()) return;
		var newEnabled = !enabled;
		setter.accept(newEnabled);
		if (mc.player == null) return;
		CCGLang.translate(messageKey)
			.space()
			.add(CCGLang.enabled(newEnabled))
			.style(enabled ? ChatFormatting.RED : ChatFormatting.GREEN)
			.sendStatus(mc.player);
	}
	/**
	 * 向服务器发送玩家动作指令。
	 *
	 * @param action 要发送的玩家动作类型
	 */
	public static void sendAction(Action action) {
		if (mc.player == null) return;
		mc.player.connection.send(new ServerboundPlayerCommandPacket(mc.player, action));
	}
	/**
	 * 向服务器发送网络数据包。
	 * <p>
	 * 使用{@link Create}模组的网络通道系统将数据包发送到服务器端。
	 */
	public static void sendToServer(CustomPacketPayload packet) {
		CatnipServices.NETWORK.sendToServer(packet);
	}
}
