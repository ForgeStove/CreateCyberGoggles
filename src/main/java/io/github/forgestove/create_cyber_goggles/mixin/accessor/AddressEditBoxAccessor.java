package io.github.forgestove.create_cyber_goggles.mixin.accessor;
import com.simibubi.create.content.logistics.AddressEditBox;
import com.simibubi.create.content.trains.schedule.DestinationSuggestions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(AddressEditBox.class)
public interface AddressEditBoxAccessor {
	@Accessor
	DestinationSuggestions getDestinationSuggestions();
}
