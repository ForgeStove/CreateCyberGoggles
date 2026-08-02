package io.github.forgestove.create_cyber_goggles.api;
/** 红石请求器分页当前页码，供 Screen 渲染与 JEI 拖入目标共享 */
public interface RequestPageProvider {
	int ccg$getRequestPage();
	void ccg$setRequestPage(int page);
}
