package io.github.forgestove.create_cyber_goggles.mixin.accessor;
import com.simibubi.create.content.logistics.AddressEditBox;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
@Mixin(RedstoneRequesterScreen.class)
public interface RedstoneRequesterScreenAccessor {
	/** 红石请求器界面每格的请求数量 */
	@Accessor
	List<Integer> getAmounts();
	/** 请求地址输入框 */
	@Accessor
	AddressEditBox getAddressBox();
	/** 「允许部分请求」开关按钮 */
	@Accessor
	IconButton getAllowPartial();
	/** 「禁止部分请求」开关按钮 */
	@Accessor
	IconButton getDontAllowPartial();
}
