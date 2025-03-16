package com.ForgeStove.create_cyber_goggles.config;
import net.createmod.catnip.config.ConfigBase;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.config.ModConfig.Type;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.function.Supplier;
public class Configs {
	private static final Map<Type, ConfigBase> CONFIGS = new EnumMap<>(Type.class);
	@Nullable private static ClientConfig client;
	public static @NotNull Set<Map.Entry<Type, ConfigBase>> registerConfigs() {
		client = register(ClientConfig::new, Type.CLIENT);
		return CONFIGS.entrySet();
	}
	private static <T extends ConfigBase> @NotNull T register(Supplier<T> factory, Type side) {
		Pair<T, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(builder -> {
			T config = factory.get();
			config.registerAll(builder);
			return config;
		});
		T config = specPair.getLeft();
		config.specification = specPair.getRight();
		CONFIGS.put(side, config);
		return config;
	}
	public static void onLoad(ModConfig config) {
		for (ConfigBase configBase : CONFIGS.values())
			if (configBase.specification == config.getSpec()) configBase.onLoad();
	}
	public static void onReload(ModConfig config) {
		for (ConfigBase configBase : CONFIGS.values())
			if (configBase.specification == config.getSpec()) configBase.onReload();
	}
	public static ClientConfig client() {
		if (client != null) return client;
		throw new AssertionError("Create Cyber Goggles Client Config was accessed, but not registered yet!");
	}
}
