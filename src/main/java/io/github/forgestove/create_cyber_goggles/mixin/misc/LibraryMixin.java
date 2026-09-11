package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.mojang.blaze3d.audio.Library;
import io.github.forgestove.create_cyber_goggles.CCG;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.ALC11;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

import java.nio.IntBuffer;
@Mixin(Library.class)
public class LibraryMixin {
	@Unique private static final int VANILLA_SOURCES = 255;
	@WrapOperation(
		method = "init", at = @At(
		value = "INVOKE", target = "Lorg/lwjgl/openal/ALC10;alcCreateContext(JLjava/nio/IntBuffer;)J"
	)
	)
	public long requestMoreSources(long device, IntBuffer attributes, Operation<Long> original) {
		int want = CCG.config.misc.soundPoolLimit;
		if (want == VANILLA_SOURCES) return original.call(device, attributes);
		int size = attributes.remaining();
		IntBuffer extended = BufferUtils.createIntBuffer(size + 2);
		for (var i = 0; i < size - 1; i++) extended.put(attributes.get(attributes.position() + i));
		extended.put(ALC11.ALC_MONO_SOURCES).put(want).put(0).flip();
		long context = original.call(device, extended);
		if (context != 0L) return context;
		CCG.LOGGER.warn("Failed to allocate {} sound sources, falling back to vanilla", want);
		return original.call(device, attributes);
	}
	@ModifyArg(
		method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(III)I", ordinal = 1), index = 2
	)
	public int raiseChannelLimit(int max) {
		int want = CCG.config.misc.soundPoolLimit;
		return want != VANILLA_SOURCES ? want : max;
	}
}
