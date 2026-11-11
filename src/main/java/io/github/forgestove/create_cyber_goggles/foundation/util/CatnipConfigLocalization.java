package io.github.forgestove.create_cyber_goggles.foundation.util;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import io.github.forgestove.flexconfig.api.ConfigTranslationKeys;
import io.github.forgestove.flexconfig.foundation.client.TranslationChecker;
import net.createmod.catnip.config.ui.ConfigAnnotations.*;
import net.createmod.catnip.lang.FontHelper;
import net.createmod.catnip.lang.FontHelper.Palette;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.fml.config.ModConfigs;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.*;

import java.util.*;
public class CatnipConfigLocalization {
	private static final CCGMods[] CONFIG_MODS = {
		CCGMods.create,
		CCGMods.aeronautics,
		CCGMods.simulated,
		CCGMods.offroad,
		CCGMods.sable,
		CCGMods.flywheel,
		CCGMods.ponder
	};
	public static void registerSource() {
		for (var mod : CONFIG_MODS)
			if (mod.isLoaded()) TranslationChecker.registerSource("catnip:" + mod.id(), () -> collectKeys(mod.id()));
	}
	public static String optionKey(String modId, List<String> path) {
		if (path == null || path.isEmpty()) return null;
		var name = path.getLast();
		if (path.size() == 1) return ConfigTranslationKeys.option(modId, name);
		return ConfigTranslationKeys.option(modId, String.join(".", path.subList(0, path.size() - 1)), name);
	}
	public static String findGroupPath(ModConfigSpec spec, UnmodifiableConfig group) {
		if (group == spec.getValues()) return "";
		var path = new ArrayList<String>();
		return findGroupPath(spec.getValues(), group, path) ? String.join(".", path) : null;
	}
	private static boolean findGroupPath(UnmodifiableConfig current, UnmodifiableConfig target, List<String> path) {
		for (var entry : current.entrySet()) {
			if (!(entry.getValue() instanceof UnmodifiableConfig child)) continue;
			path.add(entry.getKey());
			if (child == target || findGroupPath(child, target, path)) return true;
			path.removeLast();
		}
		return false;
	}
	public static void applyTooltip(
		List<Component> tooltip,
		Component header,
		String text,
		String modId,
		Type type,
		List<String> path,
		Map<String, String> annotations
	) {
		var rebuilt = new ArrayList<Component>();
		rebuilt.add(header);
		for (var line : text.split("\n"))
			rebuilt.addAll(FontHelper.cutTextComponent(Component.literal(line), Palette.ALL_GRAY));
		if (annotations.containsKey(RequiresRelog.TRUE.getName()))
			rebuilt.addAll(FontHelper.cutTextComponent(
				Component.translatable("catnip.ui.value_entry.relog_required"),
				Palette.GRAY_AND_GOLD
			));
		if (annotations.containsKey(RequiresRestart.CLIENT.getName()))
			rebuilt.addAll(FontHelper.cutTextComponent(
				Component.translatable("catnip.ui.value_entry.restart_required"),
				Palette.GRAY_AND_RED
			));
		var fullPath = modId + ":" + type.extension() + "." + String.join(".", path);
		var font = Minecraft.getInstance().font;
		if (font.width(fullPath) > FontHelper.MAX_WIDTH_PER_LINE) {
			var trimPos = 0;
			while (trimPos < fullPath.length() && font.width("..." + fullPath.substring(trimPos)) > FontHelper.MAX_WIDTH_PER_LINE)
				trimPos++;
			fullPath = "..." + fullPath.substring(trimPos);
		}
		rebuilt.add(Component.literal(fullPath).withStyle(ChatFormatting.DARK_GRAY));
		tooltip.clear();
		tooltip.addAll(rebuilt);
	}
	private static Collection<String> collectKeys(String modId) {
		var keys = new LinkedHashSet<String>();
		for (var config : ModConfigs.getModConfigs(modId))
			if (config.getSpec() instanceof ModConfigSpec spec) collectKeys(spec, spec.getValues(), "", modId, keys);
		return keys;
	}
	private static void collectKeys(ModConfigSpec spec, UnmodifiableConfig group, String prefix, String modId, Set<String> keys) {
		if (!prefix.isEmpty()) keys.add(ConfigTranslationKeys.category(modId, prefix));
		for (var entry : group.entrySet()) {
			var value = entry.getValue();
			if (value instanceof UnmodifiableConfig child) {
				collectKeys(spec, child, prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey(), modId, keys);
				continue;
			}
			if (!(value instanceof ConfigValue<?> configValue)) continue;
			var path = configValue.getPath();
			if (String.join(".", path).contains("stressValues")) continue;
			var key = optionKey(modId, path);
			if (key == null) continue;
			keys.add(key);
			keys.add(ConfigTranslationKeys.tooltip(key));
			ValueSpec raw = spec.getSpec().getRaw(path);
			if (raw != null && raw.getComment() != null && raw.getComment().contains("[in ")) keys.add(ConfigTranslationKeys.unit(key));
			try {
				if (configValue.get() instanceof Enum<?> constant) keys.add(ConfigTranslationKeys.enumValue(modId, constant));
			} catch (RuntimeException ignored) {
			}
		}
	}
}
