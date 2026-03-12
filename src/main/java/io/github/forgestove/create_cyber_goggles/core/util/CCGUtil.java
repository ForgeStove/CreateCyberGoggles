package io.github.forgestove.create_cyber_goggles.core.util;
import com.simibubi.create.*;
import com.simibubi.create.content.equipment.armor.CardboardArmorItem;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBox;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.networking.SimplePacketBase;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
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
import net.minecraftforge.fml.util.thread.EffectiveSide;
import org.jetbrains.annotations.*;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;
public class CCGUtil {
	public static final Minecraft mc = Minecraft.getInstance();
	public static final Outliner outliner = Outliner.getInstance();
	private static HitResult cachedHitResult;
	private static float lastRealtimeTick;
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
	public static float getRealtimeDeltaTicks() {
		return mc.getFrameTime();
	}
	public static <T extends U, U> @Nullable T getAs(@NotNull Class<T> clazz, U object) {
		return clazz.isInstance(object) ? clazz.cast(object) : null;
	}
	/** @return 当前帧的{@link HitResult} */
	private static HitResult getCurrentHitResult() {
		var currentTick = mc.level != null ? getRealtimeDeltaTicks() : 0;
		if (lastRealtimeTick == currentTick && cachedHitResult != null) return cachedHitResult;
		cachedHitResult = mc.hitResult;
		lastRealtimeTick = currentTick;
		return cachedHitResult;
	}
	/** @return 当前的{@link BlockHitResult}，如果不是方块命中则返回 {@code null} */
	@Contract(pure = true)
	public static @Nullable BlockHitResult getBlockHitResult() {
		return getCurrentHitResult() instanceof BlockHitResult result && result.getType() != Type.MISS ? result : null;
	}
	/** @return 当前的{@link EntityHitResult}，如果不是实体命中则返回 {@code null} */
	@Contract(pure = true)
	public static @Nullable EntityHitResult getEntityHitResult() {
		var hitResult = getCurrentHitResult();
		if (hitResult instanceof EntityHitResult result && result.getType() != Type.MISS) return result;
		return raycastEntityHitResult(hitResult);
	}
	private static @Nullable EntityHitResult raycastEntityHitResult(@Nullable HitResult hitResult) {
		if (mc.level == null) return null;
		var camera = mc.getCameraEntity();
		if (camera == null) return null;
		var partialTick = getRealtimeDeltaTicks();
		var start = camera.getEyePosition(partialTick);
		var view = camera.getViewVector(partialTick);
		if (mc.player == null) return null;
		var reach = mc.player.getEntityReach();
		if (hitResult != null && hitResult.getType() != Type.MISS) reach = Math.min(reach, start.distanceTo(hitResult.getLocation()));
		var end = start.add(view.scale(reach));
		var searchBox = camera.getBoundingBox().expandTowards(view.scale(reach)).inflate(1.0D);
		EntityHitResult selectedHitResult = null;
		var minDistanceSqr = reach * reach;
		for (var entity : mc.level.getEntities(camera, searchBox, CCGUtil::canOverlayPickEntity)) {
			var bounds = entity.getBoundingBox().inflate(entity.getPickRadius());
			var hitPos = bounds.clip(start, end);
			if (hitPos.isEmpty()) continue;
			var distanceSqr = start.distanceToSqr(hitPos.get());
			if (distanceSqr >= minDistanceSqr) continue;
			selectedHitResult = new EntityHitResult(entity, hitPos.get());
			minDistanceSqr = distanceSqr;
		}
		return selectedHitResult;
	}
	private static boolean canOverlayPickEntity(@NotNull Entity entity) {
		if (!entity.isAlive() || entity.isSpectator()) return false;
		return entity instanceof ItemRenderable || entity.isPickable();
	}
	/** @return 当前选中的{@link BlockEntity}实例，如果没有选中或类型不匹配则返回{@code null} */
	public static @Nullable BlockEntity getBlockEntity() {
		if (mc.level == null) return null;
		var result = getBlockHitResult();
		if (result == null || result.getType() == Type.MISS) return null;
		return mc.level.getBlockEntity(result.getBlockPos());
	}
	/** @return 如果类型匹配{@link T}则返回对应实例，否则返回{@code null} */
	public static <T extends BlockEntity> @Nullable T getBlockEntity(Class<T> clazz) {
		return getAs(clazz, getBlockEntity());
	}
	/** @return 当前选中的{@link Block}实例，如果没有选中或类型不匹配则返回{@code null} */
	public static @Nullable Block getBlock() {
		if (mc.level == null) return null;
		var result = getBlockHitResult();
		if (result == null || result.getType() == Type.MISS) return null;
		return mc.level.getBlockState(result.getBlockPos()).getBlock();
	}
	/** @return 如果类型匹配{@link T}则返回对应实例，否则返回{@code null} */
	public static <T extends Block> @Nullable T getBlock(Class<T> clazz) {
		return getAs(clazz, getBlock());
	}
	/** @return 选中的{@link Entity}实例，如果没有选中或类型不匹配则返回{@code null} */
	public static @Nullable Entity getEntity() {
		var result = getEntityHitResult();
		return result != null ? result.getEntity() : null;
	}
	/** @return 如果输入不为{@code null}则返回其本身，否则返回{@link ItemStack#EMPTY} */
	@Contract(value = "!null -> param1", pure = true)
	public static @NotNull ItemStack orEmpty(@Nullable ItemStack itemStack) {
		return Objects.requireNonNullElse(itemStack, ItemStack.EMPTY);
	}
	/** @return 选中的过滤器物品，如果未选中则返回{@code null} */
	public static @Nullable ItemStack getSelectedFilter() {
		if (isInGUI()) {
			if (!(mc.screen instanceof AbstractContainerScreen<?> screen)) return null;
			var slot = screen.getSlotUnderMouse();
			return slot == null ? null : slot.getItem();
		}
		var result = getBlockHitResult();
		var sbe = getBlockEntity(SmartBlockEntity.class);
		if (sbe == null || result == null) return null;
		var behaviour = sbe.getBehaviour(FilteringBehaviour.TYPE);
		return behaviour == null ? null : behaviour.getFilter(result.getDirection());
	}
	/** @return 该位置方块的{@link AABB}包围盒，若无法获取则返回{@link Shapes#block()}的包围盒 */
	public static @NotNull AABB getBounds(BlockPos blockPos) {
		if (mc.level == null) return Shapes.block().bounds();
		var shape = mc.level.getBlockState(blockPos).getShape(mc.level, blockPos);
		return (shape.isEmpty() ? Shapes.block() : shape).bounds().move(blockPos);
	}
	/** @return 如果存在激活的{@link ValueBox}则返回{@code true}，否则返回{@code false} */
	public static boolean hasActivedValueBox() {
		for (var entry : outliner.getOutlines().values()) {
			if (!entry.isAlive()) continue;
			var outline = entry.getOutline();
			if (outline instanceof ValueBox valueBox && !valueBox.isPassive) return true;
		}
		return false;
	}
	/** 检测本地玩家是否穿着全套纸板盔甲并且不在飞行状态 */
	public static boolean testForStealth() {
		if (mc.player == null) return false;
		var allMatch = Stream.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)
			.allMatch(slot -> mc.player.getItemBySlot(slot).getItem() instanceof CardboardArmorItem);
		return CCG.config.chainConveyor.cardBoardedYourself && !mc.player.getAbilities().flying && allMatch;
	}
	/** @return 如果玩家主手或副手中有物品则返回{@code true}，否则返回{@code false} */
	public static boolean hasItemInHand() {
		return mc.player != null && !Stream.of(mc.player.getMainHandItem(), mc.player.getOffhandItem()).allMatch(ItemStack::isEmpty);
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
		CCGLang.translate(messageKey).space().add(CCGLang.enabled(newEnabled)).sendStatus(mc.player);
	}
	/** 向服务器发送玩家动作指令 */
	public static void sendAction(Action action) {
		if (mc.player == null) return;
		mc.player.connection.send(new ServerboundPlayerCommandPacket(mc.player, action));
	}
	/** 使用{@link Create}模组的网络通道系统将数据包发送到服务器 */
	public static void sendToServer(SimplePacketBase packet) {
		AllPackets.getChannel().sendToServer(packet);
	}
}
