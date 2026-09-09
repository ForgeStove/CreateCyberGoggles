package io.github.forgestove.create_cyber_goggles.mixin.accessor;
import com.simibubi.create.content.logistics.AddressEditBox;
import com.simibubi.create.content.trains.schedule.DestinationSuggestions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(AddressEditBox.class)
public interface AddressEditBoxAccessor {
	/** 地址输入框右挂的剪贴板/站点下拉建议 */
	@Accessor
	DestinationSuggestions getDestinationSuggestions();
}
