package io.github.forgestove.create_cyber_goggles.config.server;
import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;
/**
 * Stores locked config entries per world save.
 * Persisted as a nested-section TOML file in the world's serverconfig directory.
 * Format matches the mod's config style: sections with indented key = value lines.
 * Lifecycle is managed by {@link ServerLifecycleHandler}.
 */
public final class ServerConfigLockStore {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Pattern SECTION_PATTERN = Pattern.compile("\\[([^]]+)]");
	private static final Pattern ENTRY_PATTERN = Pattern.compile("\\s*(\\w+)\\s*=\\s*(.+)");
	private final Path lockFilePath;
	private final Map<String, String> locks = new HashMap<>();
	public ServerConfigLockStore(MinecraftServer server, String modId) {
		lockFilePath = server.getWorldPath(new LevelResource("serverconfig/" + modId + "_locks.toml"));
	}
	/** Load locks from disk. Called on server start. */
	public void load() {
		locks.clear();
		if (!Files.exists(lockFilePath)) return;
		try {
			var currentSection = "";
			for (var line : Files.readAllLines(lockFilePath)) {
				line = line.stripTrailing();
				if (line.isBlank() || line.stripLeading().startsWith("#")) continue;
				var sectionMatcher = SECTION_PATTERN.matcher(line.stripLeading());
				if (sectionMatcher.matches()) {
					currentSection = sectionMatcher.group(1);
					continue;
				}
				var entryMatcher = ENTRY_PATTERN.matcher(line);
				if (!entryMatcher.matches()) continue;
				var key = currentSection.isEmpty() ? entryMatcher.group(1) : currentSection + "." + entryMatcher.group(1);
				var rawValue = entryMatcher.group(2);
				var value = unquote(rawValue);
				locks.put(key, value);
			}
			LOGGER.info("Loaded {} config lock(s)", locks.size());
		} catch (IOException e) {
			LOGGER.error("Failed to load config locks from {}", lockFilePath, e);
		}
	}
	private static String unquote(String raw) {
		raw = raw.strip();
		if (raw.startsWith("\"") && raw.endsWith("\""))
			return raw.substring(1, raw.length() - 1).replace("\\\"", "\"").replace("\\\\", "\\");
		return raw;
	}
	/** Lock a config entry to a specific value string. */
	public void lockEntry(String configId, String value) {
		locks.put(configId, value);
		save();
	}
	/** Save locks to disk. Called on server stop and after each change. */
	public void save() {
		try {
			Files.createDirectories(lockFilePath.getParent());
		} catch (IOException e) {
			LOGGER.error("Failed to create lock directory", e);
			return;
		}
		try {
			writeLines(lockFilePath);
		} catch (IOException e) {
			LOGGER.error("Failed to save config locks to {}", lockFilePath, e);
		}
	}
	public String toTomlString() {
		return String.join("\n", buildTomlLines());
	}
	private List<String> buildTomlLines() {
		var sections = new LinkedHashMap<String, List<Entry<String, String>>>();
		for (var entry : locks.entrySet()) {
			var key = entry.getKey();
			var dot = key.lastIndexOf('.');
			var section = dot < 0 ? "" : key.substring(0, dot);
			sections.computeIfAbsent(section, k -> new ArrayList<>()).add(entry);
		}
		var sortedSections = new ArrayList<>(sections.keySet());
		sortedSections.sort(Comparator.comparingInt(s -> s.isEmpty() ? -1 : s.split("\\.").length));
		var lines = new ArrayList<String>();
		for (var section : sortedSections) {
			var entries = sections.get(section);
			if (!section.isEmpty()) lines.add("[" + section + "]");
			for (var entry : entries) {
				var name = entry.getKey().substring(section.isEmpty() ? 0 : section.length() + 1);
				lines.add("\t" + name + " = " + quote(entry.getValue()));
			}
		}
		return lines;
	}
	private void writeLines(Path path) throws IOException {
		Files.write(path, buildTomlLines());
	}
	private static String quote(String value) {
		if (value.equals("true") || value.equals("false")) return value;
		try {
			Integer.parseInt(value);
			return value;
		} catch (NumberFormatException ignored) {}
		try {
			Integer.decode(value); // 支持 0xFF0000 等十六进制颜色
			return value;
		} catch (NumberFormatException ignored) {}
		try {
			Long.parseLong(value);
			return value;
		} catch (NumberFormatException ignored) {}
		try {
			Long.decode(value);
			return value;
		} catch (NumberFormatException ignored) {}
		try {
			Float.parseFloat(value);
			return value;
		} catch (NumberFormatException ignored) {}
		try {
			Double.parseDouble(value);
			return value;
		} catch (NumberFormatException ignored) {}
		return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
	}
	/** Unlock a config entry. Removes it from the lock store. */
	public void unlockEntry(String configId) {
		locks.remove(configId);
		save();
	}
}
