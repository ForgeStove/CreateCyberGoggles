package io.github.forgestove.create_cyber_goggles.api;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.GoggleTooltipRegistry;
import io.github.forgestove.create_cyber_goggles.core.factory.CCGMods;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.*;
/**
 * 护目镜悬浮内容（goggle tooltip）条目注册表。
 * <p>
 * 条目以「目标方块实体类型 + 条目 id」登记，由 {@link #dispatch} 按注册顺序执行；
 * {@link #NATIVE} 占位标记目标类型原生内容的执行位置。
 * </p>
 * <p>
 * 条目只能向 tooltip 追加内容，不得清空或替换已有元素；CCG 配置开关仅控制内建条目。
 * {@code create_cyber_goggles} 命名空间视为内建条目，异常直接向调用方传播；其余条目捕获后跳过，
 * 同一 id 仅记录一次 WARN。
 * </p>
 */
@SuppressWarnings("unused")
public final class GoggleTooltip {
	/** 占位条目 id：标记目标类型原生内容的执行位置。 */
	public static final ResourceLocation NATIVE = CCGMods.create.rl("native");
	private static final GoggleTooltipRegistry<Context> REGISTRY = new GoggleTooltipRegistry<>(
		NATIVE.toString(), CCG.ID, (message, t) -> {
		if (t == null) CCG.LOGGER.warn(message);
		else CCG.LOGGER.warn(message, t);
	}
	);
	/** 在目标类型的条目列表末尾追加条目。 */
	public static <T extends BlockEntity> void register(Class<T> target, ResourceLocation id, Provider<? super T> provider) {
		REGISTRY.register(target, check(id), wrap(target, provider));
	}
	/** 在目标类型的条目列表末尾追加不关心 Shift 状态的条目。 */
	public static <T extends BlockEntity> void register(Class<T> target, ResourceLocation id, SimpleProvider<? super T> provider) {
		Objects.requireNonNull(provider, "provider");
		register(target, id, (tooltip, be, sneaking) -> provider.add(tooltip, be));
	}
	private static String check(ResourceLocation id) {
		if (id.equals(NATIVE)) throw new IllegalArgumentException(NATIVE + " is a reserved placeholder id");
		return id.toString();
	}
	private static <T extends BlockEntity> Function<Context, Boolean> wrap(Class<T> target, Provider<? super T> provider) {
		Objects.requireNonNull(provider, "provider");
		return context -> provider.add(context.tooltip(), target.cast(context.be()), context.isPlayerSneaking());
	}
	/** 在指定锚点条目之前插入；锚点不存在时追加到末尾并记录 WARN。 */
	public static <T extends BlockEntity> void registerBefore(
		Class<T> target,
		ResourceLocation anchorId,
		ResourceLocation id,
		Provider<? super T> provider
	) {
		REGISTRY.registerBefore(target, anchorId.toString(), check(id), wrap(target, provider));
	}
	/** 在指定锚点条目之后插入；锚点不存在时追加到末尾并记录 WARN。 */
	public static <T extends BlockEntity> void registerAfter(
		Class<T> target,
		ResourceLocation anchorId,
		ResourceLocation id,
		Provider<? super T> provider
	) {
		REGISTRY.registerAfter(target, anchorId.toString(), check(id), wrap(target, provider));
	}
	/** 内部用：为指定类型注册 {@link #NATIVE} 占位。 */
	@Internal
	public static void registerNative(Class<? extends BlockEntity> target) {
		REGISTRY.register(target, NATIVE.toString(), null);
	}
	/** 内部用：mixin 覆写体内的分发入口，仅限客户端调用；{@link #NATIVE} 处调用 {@code nativeFallback}。 */
	@Internal
	public static boolean dispatch(
		List<Component> tooltip,
		BlockEntity be,
		boolean isPlayerSneaking,
		@Nullable BooleanSupplier nativeFallback
	) {
		return REGISTRY.dispatch(new Context(tooltip, be, isPlayerSneaking), be.getClass(), nativeFallback);
	}
	@FunctionalInterface
	public interface Provider<T extends BlockEntity> {
		/** 向 tooltip 追加内容；返回 {@code true} 表示添加了内容。 */
		boolean add(List<Component> tooltip, T be, boolean isPlayerSneaking);
	}
	@FunctionalInterface
	public interface SimpleProvider<T extends BlockEntity> {
		/** 向 tooltip 追加内容；返回 {@code true} 表示添加了内容。 */
		boolean add(List<Component> tooltip, T be);
	}
	private record Context(List<Component> tooltip, BlockEntity be, boolean isPlayerSneaking) {}
}
