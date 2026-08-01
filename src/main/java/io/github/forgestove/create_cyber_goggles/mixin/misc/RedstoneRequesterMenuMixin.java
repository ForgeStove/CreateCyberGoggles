package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterMenu;
import io.github.forgestove.create_cyber_goggles.api.ScreenReferenced;
import org.spongepowered.asm.mixin.*;
/** 给红石请求器菜单添加 screenReference，使 JEI 转移时能拿到对应 Screen 更新请求数量 */
@Mixin(RedstoneRequesterMenu.class)
public abstract class RedstoneRequesterMenuMixin implements ScreenReferenced {
	@Unique private Object ccg$screenReference;
	@Override
	public Object ccg$getScreenReference() {
		return ccg$screenReference;
	}
	@Override
	public void ccg$setScreenReference(Object screen) {
		ccg$screenReference = screen;
	}
}
