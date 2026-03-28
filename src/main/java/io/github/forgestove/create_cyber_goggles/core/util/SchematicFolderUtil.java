package io.github.forgestove.create_cyber_goggles.core.util;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.utility.CreatePaths;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
public final class SchematicFolderUtil {
	private static final String ROOT = "";
	private static String selectedFolder = ROOT;
	public static synchronized String getSelectedFolder() {
		return selectedFolder;
	}
	public static synchronized void setSelectedFolder(String folder) {
		var normalized = normalize(folder);
		var selectedDir = CreatePaths.SCHEMATICS_DIR.resolve(normalized).normalize();
		if (!selectedDir.startsWith(CreatePaths.SCHEMATICS_DIR) || !Files.isDirectory(selectedDir)) {
			selectedFolder = ROOT;
			return;
		}
		selectedFolder = normalized;
	}
	public static synchronized Path getSelectedDirectory() {
		var selectedDir = CreatePaths.SCHEMATICS_DIR.resolve(selectedFolder).normalize();
		if (!selectedDir.startsWith(CreatePaths.SCHEMATICS_DIR) || !Files.isDirectory(selectedDir)) {
			selectedFolder = ROOT;
			return CreatePaths.SCHEMATICS_DIR;
		}
		return selectedDir;
	}
	public static List<String> listSelectableFolders() {
		List<String> folders = new ArrayList<>();
		folders.add(ROOT);
		try (var paths = Files.walk(CreatePaths.SCHEMATICS_DIR)) {
			paths.filter(Files::isDirectory)
				.filter(path -> !path.equals(CreatePaths.SCHEMATICS_DIR))
				.filter(SchematicFolderUtil::hasDirectSchematic)
				.map(CreatePaths.SCHEMATICS_DIR::relativize)
				.map(Path::toString)
				.map(path -> path.replace('\\', '/'))
				.filter(path -> !path.equalsIgnoreCase("uploaded") && !path.startsWith("uploaded/"))
				.sorted(Comparator.naturalOrder())
				.forEach(folders::add);
		} catch (IOException ignored) {
		}
		return folders;
	}
	private static boolean hasDirectSchematic(Path folder) {
		try (var children = Files.list(folder)) {
			return children.anyMatch(path -> !Files.isDirectory(path) && path.getFileName().toString().endsWith(".nbt"));
		} catch (IOException ignored) {
			return false;
		}
	}
	private static String normalize(String folder) {
		if (folder == null) return ROOT;
		var normalized = folder.replace('\\', '/').trim();
		if (normalized.isEmpty() || normalized.equals(".")) return ROOT;
		while (normalized.startsWith("/")) normalized = normalized.substring(1);
		return normalized;
	}
	public static @NotNull String normalizeUploadName(String schematic) {
		var normalized = schematic == null ? "" : schematic.replace('\\', '/').trim();
		if (normalized.isEmpty()) return "upload.nbt";
		var slash = normalized.lastIndexOf('/');
		var dir = slash >= 0 ? normalized.substring(0, slash) : "";
		var file = slash >= 0 ? normalized.substring(slash + 1) : normalized;
		if (file.isEmpty()) file = "upload.nbt";
		dir = dir.replaceAll("[\\\\/:*?\"<>|]+", "_");
		file = file.replaceAll("[\\\\/:*?\"<>|]+", "_");
		var uploadName = dir.isEmpty() ? file : dir + "_" + file;
		if (!uploadName.endsWith(".nbt")) return uploadName + ".nbt";
		return uploadName;
	}
	public static Path resolveLocalSchematicByFileName(String fileName) {
		try (var paths = Files.walk(CreatePaths.SCHEMATICS_DIR)) {
			var matches = paths.filter(Files::isRegularFile).filter(p -> p.getFileName().toString().equalsIgnoreCase(fileName)).toList();
			if (matches.isEmpty()) return null;
			if (matches.size() == 1) return matches.get(0);
			Create.LOGGER.warn("Ambiguous local schematics for {}: {} candidates, using most recently modified", fileName, matches.size());
			return matches.stream().max(Comparator.comparingLong(SchematicFolderUtil::getLastModified)).orElse(matches.get(0));
		} catch (IOException ignored) {
			return null;
		}
	}
	public static long getLastModified(Path path) {
		try {
			return Files.getLastModifiedTime(path).toMillis();
		} catch (IOException ignored) {
			return Long.MIN_VALUE;
		}
	}
}
