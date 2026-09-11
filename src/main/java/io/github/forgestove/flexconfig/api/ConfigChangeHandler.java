package io.github.forgestove.flexconfig.api;
/**
 * 配置值变化后的副作用处理器（配合 {@link OnChange} 使用）。
 * <p>
 * 契约：
 * <ul>
 * <li>需有<b>无参构造</b>，实例由框架缓存并复用（不要在里面保存每次调用的状态）；</li>
 * <li>同一次保存内按<b>类</b>去重，至多调用一次（多个字段共用同一 handler 时只传第一个变化字段的值）；</li>
 * <li>调用时配置对象已写入新值，但配置文件的落盘发生在其后；</li>
 * <li>抛出任何异常都会被吞掉并记日志，不影响保存流程。</li>
 * </ul>
 */
@FunctionalInterface
public interface ConfigChangeHandler {
	/**
	 * @param oldValue 变更前的值
	 * @param newValue 变更后的值
	 */
	void onChange(Object oldValue, Object newValue);
}
