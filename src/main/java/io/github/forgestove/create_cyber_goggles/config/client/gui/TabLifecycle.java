package io.github.forgestove.create_cyber_goggles.config.client.gui;
/**
 * 入口在被添加到 ConfigCategoryTab 后接收初始化回调的接口。
 * 实现类可在此完成回调注册等初始化逻辑。
 */
public interface TabLifecycle {
	void onAttachedToTab(ConfigCategoryTab<?> tab);
}
