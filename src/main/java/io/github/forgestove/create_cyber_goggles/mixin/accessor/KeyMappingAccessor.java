package io.github.forgestove.create_cyber_goggles.mixin.accessor;
import com.mojang.blaze3d.platform.InputConstants.Key;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(KeyMapping.class)
public interface KeyMappingAccessor {
	@Accessor
	Key getKey();
}
