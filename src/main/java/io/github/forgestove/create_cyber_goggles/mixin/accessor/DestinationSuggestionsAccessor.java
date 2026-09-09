package io.github.forgestove.create_cyber_goggles.mixin.accessor;
import com.mojang.brigadier.suggestion.Suggestion;
import com.simibubi.create.content.trains.schedule.DestinationSuggestions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
@Mixin(DestinationSuggestions.class)
public interface DestinationSuggestionsAccessor {
	/** 当前匹配到的地址建议（用于按实际条数计算下拉高度，决定朝上还是朝下展开） */
	@Accessor
	List<Suggestion> getCurrentSuggestions();
	/** 下拉锚点偏移：位置按 {@code 72 + yOffset} 计算，anchorToBottom 时列表自该点向上排 */
	@Accessor
	int getYOffset();
	@Accessor
	void setYOffset(int yOffset);
}
