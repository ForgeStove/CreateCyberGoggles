package io.github.forgestove.create_cyber_goggles.mixin.accessor;
import com.mojang.brigadier.suggestion.Suggestion;
import com.simibubi.create.content.trains.schedule.DestinationSuggestions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
@Mixin(DestinationSuggestions.class)
public interface DestinationSuggestionsAccessor {
	@Accessor
	List<Suggestion> getCurrentSuggestions();
	@Accessor
	void setYOffset(int yOffset);
}
