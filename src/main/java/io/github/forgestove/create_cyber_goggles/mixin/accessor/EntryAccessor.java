package io.github.forgestove.create_cyber_goggles.mixin.accessor;
import net.createmod.catnip.config.ui.ConfigScreenList.Entry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
@Mixin(Entry.class)
public interface EntryAccessor {
	@Accessor
	Map<String, String> getAnnotations();
}
