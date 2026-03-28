package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.simibubi.create.content.schematics.SchematicItem;
import com.simibubi.create.foundation.utility.CreatePaths;
import io.github.forgestove.create_cyber_goggles.core.util.SchematicFolderUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.io.InputStream;
import java.nio.file.*;
@Mixin(value = SchematicItem.class, remap = false)
public abstract class SchematicItemMixin {
	@WrapOperation(
		method = "loadSchematic", at = @At(
		value = "INVOKE",
		target = "Ljava/nio/file/Files;newInputStream(Ljava/nio/file/Path;[Ljava/nio/file/OpenOption;)Ljava/io/InputStream;"
	)
	)
	private static InputStream openInputWithFallback(Path path, OpenOption[] options, Operation<InputStream> original) {
		if (Files.exists(path)) return original.call(path, options);
		if (path.startsWith(CreatePaths.SCHEMATICS_DIR)) {
			var uploadName = path.getFileName().toString();
			var fallback = SchematicFolderUtil.resolveLocalSchematicByFileName(uploadName);
			if (fallback != null && Files.exists(fallback)) return original.call(fallback, options);
		}
		return original.call(path, options);
	}
}
