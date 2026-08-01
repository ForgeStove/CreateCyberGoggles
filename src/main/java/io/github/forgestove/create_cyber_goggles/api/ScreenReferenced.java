package io.github.forgestove.create_cyber_goggles.api;
/** 让菜单能反向引用它关联的 Screen */
public interface ScreenReferenced {
	Object ccg$getScreenReference();
	void ccg$setScreenReference(Object screen);
}
