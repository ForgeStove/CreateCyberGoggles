package io.github.forgestove.create_cyber_goggles.mixin.accessor;
import com.simibubi.create.content.logistics.AddressEditBox;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
@Mixin(RedstoneRequesterScreen.class)
public interface RedstoneRequesterScreenAccessor {
	@Accessor
	List<Integer> getAmounts();
	@Accessor
	AddressEditBox getAddressBox();
	@Accessor
	IconButton getAllowPartial();
	@Accessor
	IconButton getDontAllowPartial();
}
