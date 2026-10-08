package io.github.forgestove.flexconfig.tree;
import io.github.forgestove.flexconfig.api.RestartType;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
public interface ConfigNode<C> {
	@Nullable Component getTooltip();
	void resetToDefault();
	void resetToActive(C config);
	/**
	 * 该节点相对当前生效值是否还需要重启才能完全生效。
	 *
	 * @return 不需要则返回 {@code null}，否则返回所需的重启级别（树聚合时取最重的一档）
	 */
	@Nullable RestartType restartType(C config);
	boolean isDefaultValue(C config);
	boolean isActiveValue(C config);
	@Nullable Component validate(C config);
	void copy(C from, C to);
	/** 把 from 复制到运行时对象 to，跳过「已改动但还未生效」的延迟项——它们的运行时值要等重启才更新。 */
	void copyForRuntime(C from, C to);
	/** 把「已改动但还未生效」的新值写入 to（文件模型）；没有待生效改动时是空操作。 */
	void flushPending(C to);
	/** 让「重启级别等于 {@code restartType}」的延迟项生效：把文件里的新值写入运行时对象 active。 */
	void applyRestart(C saved, C active, RestartType restartType);
	void writeEditingToConfig(C config);
	default String getPath() {
		return "";
	}
}
