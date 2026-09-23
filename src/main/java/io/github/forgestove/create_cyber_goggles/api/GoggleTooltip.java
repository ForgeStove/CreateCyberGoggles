package io.github.forgestove.create_cyber_goggles.api;
import io.github.forgestove.create_cyber_goggles.CCG;
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
 * CCG 对 {@code addToGoggleTooltip} 的覆写不再独占内容，改为调度此处注册的条目：
 * 第三方模组可按「目标方块实体 + 命名锚点」插入自己的整段内容，与 CCG 内建条目共存。
 * </p>
 * 注册请在 common 端初始化时完成，且 provider 需保持双端安全（Jade 等服务端收集路径也会走到）。
 * 条目只允许追加内容，不要清空或截断 tooltip。CCG 的配置开关只门控 CCG 自己的条目，不门控第三方条目。
 * 命名空间为 {@code create_cyber_goggles} 的条目视为 CCG 内建，不做异常隔离；其余条目逐个 try/catch，
 * 抛异常时 WARN 一次并继续，不会拖垮其他条目。
 */
@SuppressWarnings("unused")
public final class GoggleTooltip {
	/** 保留占位条目 id：仅代表「Create 原生内容在此位置」，dispatch 走到此处时调用 mixin 传入的原生回调。 */
	public static final ResourceLocation NATIVE = CCGMods.create.rl("native");
	private static final GoggleTooltipRegistry<Context> REGISTRY = new GoggleTooltipRegistry<>(
		NATIVE.toString(), CCG.ID, (message, t) -> {
		if (t == null) CCG.LOGGER.warn(message);
		else CCG.LOGGER.warn(message, t);
	}
	);
	/** 把内容条目追加到目标悬浮窗末尾（最常用）。 */
	public static void register(Class<? extends BlockEntity> target, ResourceLocation id, Provider provider) {
		REGISTRY.register(target, check(id), wrap(provider));
	}
	private static String check(ResourceLocation id) {
		if (id.equals(NATIVE)) throw new IllegalArgumentException(NATIVE + " is a reserved placeholder id");
		return id.toString();
	}
	private static Function<Context, Boolean> wrap(Provider provider) {
		Objects.requireNonNull(provider, "provider");
		return context -> provider.add(context.be(), context.tooltip(), context.isPlayerSneaking());
	}
	/** 把内容条目插入到指定锚点条目之前（锚点不存在则退化为末尾追加并 WARN）。 */
	public static void registerBefore(
		Class<? extends BlockEntity> target,
		ResourceLocation anchorId,
		ResourceLocation id,
		Provider provider
	) {
		REGISTRY.registerBefore(target, anchorId.toString(), check(id), wrap(provider));
	}
	/** 把内容条目插入到指定锚点条目之后（锚点不存在则退化为末尾追加并 WARN）。 */
	public static void registerAfter(
		Class<? extends BlockEntity> target,
		ResourceLocation anchorId,
		ResourceLocation id,
		Provider provider
	) {
		REGISTRY.registerAfter(target, anchorId.toString(), check(id), wrap(provider));
	}
	/** 内部用：在目标条目列表末尾注册 {@link #NATIVE} 占位，仅供 CCG 自注册引导调用。 */
	@Internal
	public static void registerNative(Class<? extends BlockEntity> target) {
		REGISTRY.register(target, NATIVE.toString(), null);
	}
	/**
	 * 内部用：mixin 覆写体内的调度入口。按注册顺序执行条目，{@link #NATIVE} 占位处调用
	 * nativeFallback（可为 null）；返回值为 nativeFallback 结果与任一条目返回值的或。
	 */
	@Internal
	public static boolean dispatch(
		BlockEntity be,
		List<Component> tooltip,
		boolean isPlayerSneaking,
		@Nullable BooleanSupplier nativeFallback
	) {
		return REGISTRY.dispatch(new Context(be, tooltip, isPlayerSneaking), be.getClass(), nativeFallback);
	}
	@FunctionalInterface
	public interface Provider {
		/** 往悬浮窗追加内容；返回 true 表示添加了内容。 */
		boolean add(BlockEntity be, List<Component> tooltip, boolean isPlayerSneaking);
	}
	private record Context(BlockEntity be, List<Component> tooltip, boolean isPlayerSneaking) {}
}
