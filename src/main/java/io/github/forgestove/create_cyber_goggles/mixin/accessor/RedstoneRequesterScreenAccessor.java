package io.github.forgestove.create_cyber_goggles.mixin.accessor;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
@Mixin(RedstoneRequesterScreen.class)
public interface RedstoneRequesterScreenAccessor {
	/** 红石请求器界面每格的请求数量 */
	@Accessor("amounts")
	List<Integer> getAmounts();
}
