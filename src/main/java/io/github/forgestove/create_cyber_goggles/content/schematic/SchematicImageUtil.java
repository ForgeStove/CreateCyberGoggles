package io.github.forgestove.create_cyber_goggles.content.schematic;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.*;
import com.simibubi.create.content.schematics.SchematicItem;
import com.simibubi.create.content.schematics.client.ClientSchematicLoader;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.compat.sable.SchematicSubLevelHelper;
import io.github.forgestove.create_cyber_goggles.foundation.util.CCGMods;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.levelWrappers.SchematicLevel;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Stream;
public class SchematicImageUtil {
	private static final Set<String> IMAGE_EXTENSIONS = Set.of("png", "jpg", "jpeg", "gif", "bmp", "webp");
	public static StructureTemplate loadTemplateFromSchematicName(String schematicName) {
		Minecraft client = Minecraft.getInstance();
		String owner = Objects.requireNonNull(client.player).getGameProfile().getName();
		ItemStack blueprint = AllItems.SCHEMATIC.asStack();
		blueprint.set(AllDataComponents.SCHEMATIC_OWNER, owner);
		blueprint.set(AllDataComponents.SCHEMATIC_FILE, schematicName);
		return SchematicItem.loadSchematic(client.level, blueprint);
	}
	public static void attachSubLevels(StructureTemplate template, SchematicLevel target, Level level) {
		CCGMods.sable.executeIfInstalled(() -> SchematicSubLevelHelper.attachSubLevels(template, target, level));
	}
	public static boolean hasSubLevels(SchematicLevel level) {
		return CCGMods.sable.runIfInstalled(() -> SchematicSubLevelHelper.hasSubLevels(level)).orElse(false);
	}
	public static void renderSubLevelBlockEntities(PoseStack ms, SuperRenderTypeBuffer buffers, SchematicLevel level) {
		CCGMods.sable.executeIfInstalled(() -> SchematicSubLevelHelper.renderBlockEntities(
			ms,
			buffers,
			level,
			AnimationTickHolder.getPartialTicks()
		));
	}
	public static BoundingBox frameBounds(SchematicLevel level) {
		return CCGMods.sable.runIfInstalled(() -> SchematicSubLevelHelper.frameBounds(level)).orElseGet(level::getBounds);
	}
	public static Optional<String> getSchematicNameFromIndex(int schematicIndex) {
		List<Component> availableSchematics = CreateClient.SCHEMATIC_SENDER.getAvailableSchematics();
		if (schematicIndex < 0 || schematicIndex >= availableSchematics.size()) return Optional.empty();
		return Optional.of(availableSchematics.get(schematicIndex).getString());
	}
	public static List<Component> refreshSchematicNames() {
		ClientSchematicLoader sender = CreateClient.SCHEMATIC_SENDER;
		sender.refresh();
		return sender.getAvailableSchematics().stream().map(component -> {
			String value = component.getString().replace('\\', '/');
			return (Component) Component.literal(Paths.get(value).getFileName().toString());
		}).toList();
	}
	public static Stream<String> getAllSchematicNames() {
		return CreateClient.SCHEMATIC_SENDER.getAvailableSchematics().stream().map(Component::getString);
	}
	public static @NotNull File saveImage(File directory, String fileName, String extension, byte @NotNull [] imageByteArray) throws
		IOException,
		IllegalArgumentException {
		String fileNameAndExt = fileName + "." + extension;
		String errorPrefix = "Failed to write image: " + fileNameAndExt + ". ";
		if (imageByteArray.length == 0) throw new IllegalArgumentException(errorPrefix + "Image byte array cannot be empty.");
		if (fileName.isBlank()) throw new IllegalArgumentException(errorPrefix + "File name cannot be blank");
		if (!IMAGE_EXTENSIONS.contains(extension)) throw new IllegalArgumentException(errorPrefix + "Unrecognised image file extension");
		var outputFile = new File(directory, fileNameAndExt);
		var tempFile = new File(directory, fileName + ".tmp");
		try {
			Files.write(tempFile.toPath(), imageByteArray);
			if (!tempFile.renameTo(outputFile))
				throw new IOException("Failed to rename temporary image file: " + tempFile.getAbsolutePath());
			return outputFile;
		} catch (IOException e) {
			if (!tempFile.delete()) CCG.LOGGER.error("Failed to delete temporary image file: {}", tempFile.getAbsolutePath());
			throw e;
		}
	}
	public static <T> CompletableFuture<T> onRenderThread(Supplier<T> work) {
		var future = new CompletableFuture<T>();
		RenderSystem.recordRenderCall(() -> {
			try {
				future.complete(work.get());
			} catch (Throwable t) {
				future.completeExceptionally(t);
			}
		});
		return future;
	}
}
