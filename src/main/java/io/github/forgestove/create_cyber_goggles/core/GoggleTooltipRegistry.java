package io.github.forgestove.create_cyber_goggles.core;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
/**
 * 护目镜悬浮内容条目注册表核心。
 * <p>由 {@code GoggleTooltip} 持有实例并做类型适配。</p>
 */
@Internal
public final class GoggleTooltipRegistry<C> {
	private final String nativeId;
	private final String builtinPrefix;
	private final BiConsumer<String, @Nullable Throwable> warner;
	private final Map<Class<?>, List<Entry<C>>> entries = new ConcurrentHashMap<>();
	private final Set<String> warned = ConcurrentHashMap.newKeySet();
	public GoggleTooltipRegistry(String nativeId, String builtinNamespace, BiConsumer<String, @Nullable Throwable> warner) {
		this.nativeId = nativeId;
		builtinPrefix = builtinNamespace + ":";
		this.warner = warner;
	}
	private static <C> int indexOf(List<Entry<C>> list, String id) {
		for (var i = 0; i < list.size(); i++)
			if (list.get(i).id().equals(id)) return i;
		return -1;
	}
	public void register(Class<?> target, String id, @Nullable Function<C, Boolean> action) {
		List<Entry<C>> list = entries.computeIfAbsent(target, key -> new CopyOnWriteArrayList<>());
		if (indexOf(list, id) >= 0) {
			warner.accept("Duplicate goggle tooltip entry " + id + " for " + target.getName(), null);
			return;
		}
		list.add(new Entry<>(id, action));
	}
	public void registerBefore(Class<?> target, String anchorId, String id, Function<C, Boolean> action) {
		insert(target, anchorId, id, action, false);
	}
	private void insert(Class<?> target, String anchorId, String id, Function<C, Boolean> action, boolean after) {
		List<Entry<C>> list = entries.computeIfAbsent(target, key -> new CopyOnWriteArrayList<>());
		if (indexOf(list, id) >= 0) {
			warner.accept("Duplicate goggle tooltip entry " + id + " for " + target.getName(), null);
			return;
		}
		int anchor = indexOf(list, anchorId);
		if (anchor < 0) {
			warner.accept("Goggle tooltip anchor " + anchorId + " not found for " + target.getName() + ", appending " + id, null);
			list.add(new Entry<>(id, action));
			return;
		}
		list.add(after ? anchor + 1 : anchor, new Entry<>(id, action));
	}
	public void registerAfter(Class<?> target, String anchorId, String id, Function<C, Boolean> action) {
		insert(target, anchorId, id, action, true);
	}
	public boolean dispatch(C context, Class<?> actualClass, @Nullable BooleanSupplier nativeFallback) {
		List<Entry<C>> list = lookup(actualClass);
		if (list == null) return nativeFallback != null && nativeFallback.getAsBoolean();
		var added = false;
		for (var entry : list) {
			if (entry.id().equals(nativeId)) {
				if (nativeFallback != null) added |= nativeFallback.getAsBoolean();
				continue;
			}
			assert entry.action() != null;
			if (entry.id().startsWith(builtinPrefix)) added |= entry.action().apply(context);
			else try {
				added |= entry.action().apply(context);
			} catch (Throwable t) {
				if (warned.add(entry.id())) warner.accept("Goggle tooltip entry " + entry.id() + " threw", t);
			}
		}
		return added;
	}
	private @Nullable List<Entry<C>> lookup(Class<?> actualClass) {
		for (Class<?> type = actualClass; type != null; type = type.getSuperclass()) {
			List<Entry<C>> list = entries.get(type);
			if (list != null && !list.isEmpty()) return list;
		}
		return null;
	}
	record Entry<C>(String id, @Nullable Function<C, Boolean> action) {}
}
