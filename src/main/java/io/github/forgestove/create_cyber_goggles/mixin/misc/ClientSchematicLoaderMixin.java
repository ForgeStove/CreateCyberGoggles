package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.simibubi.create.Create;
import com.simibubi.create.content.schematics.client.ClientSchematicLoader;
import com.simibubi.create.foundation.utility.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.api.Self;
import io.github.forgestove.create_cyber_goggles.core.util.SchematicFolderUtil;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.file.*;
@Mixin(ClientSchematicLoader.class)
public abstract class ClientSchematicLoaderMixin implements Self<ClientSchematicLoader> {
	@ModifyArg(
		method = "startNewUpload", at = @At(
		value = "INVOKE",
		target = "Lcom/simibubi/create/content/schematics/packet/SchematicUploadPacket;begin(Ljava/lang/String;J)"
			+ "Lcom/simibubi/create/content/schematics/packet/SchematicUploadPacket;"
	), index = 0
	)
	private @NotNull String normalizeUploadNameForBegin(String schematic) {
		return SchematicFolderUtil.normalizeUploadName(schematic);
	}
	@ModifyArg(
		method = "continueUpload", at = @At(
		value = "INVOKE",
		target = "Lcom/simibubi/create/content/schematics/packet/SchematicUploadPacket;write(Ljava/lang/String;[B)"
			+ "Lcom/simibubi/create/content/schematics/packet/SchematicUploadPacket;"
	), index = 0
	)
	private @NotNull String normalizeUploadNameForWrite(String schematic) {
		return SchematicFolderUtil.normalizeUploadName(schematic);
	}
	@ModifyArg(
		method = "finishUpload", at = @At(
		value = "INVOKE",
		target = "Lcom/simibubi/create/content/schematics/packet/SchematicUploadPacket;finish(Ljava/lang/String;)"
			+ "Lcom/simibubi/create/content/schematics/packet/SchematicUploadPacket;"
	), index = 0
	)
	private @NotNull String normalizeUploadNameForFinish(String schematic) {
		return SchematicFolderUtil.normalizeUploadName(schematic);
	}
	@Inject(method = "refresh", at = @At("HEAD"), cancellable = true)
	private void refreshFromSelectedFolderOnly(CallbackInfo ci) {
		if (!CCG.config.misc.recursiveSchematicScan) return;
		FilesHelper.createFolderIfMissing(CreatePaths.SCHEMATICS_DIR);
		var availableSchematics = thiz().getAvailableSchematics();
		availableSchematics.clear();
		var folder = SchematicFolderUtil.getSelectedDirectory();
		try (var paths = Files.list(folder)) {
			paths.filter(path -> !Files.isDirectory(path) && path.getFileName().toString().endsWith(".nbt"))
				.map(CreatePaths.SCHEMATICS_DIR::relativize)
				.map(relative -> relative.toString().replace('\\', '/'))
				.map(Component::literal)
				.forEach(availableSchematics::add);
		} catch (NoSuchFileException ignored) {
			// No Schematics created yet
		} catch (IOException e) {
			Create.LOGGER.error("Failed to refresh schematics", e);
		}
		availableSchematics.sort((aT, bT) -> {
			var a = aT.getString();
			var b = bT.getString();
			if (a.endsWith(".nbt")) a = a.substring(0, a.length() - 4);
			if (b.endsWith(".nbt")) b = b.substring(0, b.length() - 4);
			var aLength = a.length();
			var bLength = b.length();
			var minSize = Math.min(aLength, bLength);
			char aChar;
			char bChar;
			boolean aNumber;
			boolean bNumber;
			var asNumeric = false;
			var lastNumericCompare = 0;
			for (var i = 0; i < minSize; i++) {
				aChar = a.charAt(i);
				bChar = b.charAt(i);
				aNumber = aChar >= '0' && aChar <= '9';
				bNumber = bChar >= '0' && bChar <= '9';
				if (asNumeric) if (aNumber && bNumber) {
					if (lastNumericCompare == 0) lastNumericCompare = aChar - bChar;
				} else if (aNumber) return 1;
				else if (bNumber) return -1;
				else if (lastNumericCompare == 0) {
					if (aChar != bChar) return aChar - bChar;
					asNumeric = false;
				} else return lastNumericCompare;
				else if (aNumber && bNumber) {
					asNumeric = true;
					if (lastNumericCompare == 0) lastNumericCompare = aChar - bChar;
				} else if (aChar != bChar) return aChar - bChar;
			}
			if (asNumeric) if (aLength > bLength && a.charAt(bLength) >= '0' && a.charAt(bLength) <= '9') return 1;
			else if (bLength > aLength && b.charAt(aLength) >= '0' && b.charAt(aLength) <= '9') return -1;
			else if (lastNumericCompare == 0) return aLength - bLength;
			else return lastNumericCompare;
			return aLength - bLength;
		});
		ci.cancel();
	}
}
