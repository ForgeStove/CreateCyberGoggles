package io.github.forgestove.flexconfig;
import io.github.forgestove.flexconfig.api.Config;
import io.github.forgestove.flexconfig.client.ConfigScreenFactory;
import io.github.forgestove.flexconfig.tree.*;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.forgestove.create_cyber_goggles.CCG.ID;
public final class ConfigRegistry {
	private static final Map<String, ConfigHandler<?, ?>> HANDLERS = new ConcurrentHashMap<>();
	public static <C> C init(Class<C> clazz) {
		var modId = getModId(clazz);
		var handler = HANDLERS.computeIfAbsent(modId, string -> ConfigHandler.builder(clazz).build());
		initConfigScreenForClient(modId);
		var config = handler.getConfig();
		if (clazz.isInstance(config)) return clazz.cast(config);
		throw new IllegalStateException("ConfigHandler returned config of type %s, expected %s".formatted(
			config.getClass().getName(),
			clazz.getName()
		));
	}
	public static String getModId(@NotNull Class<?> configClass) {
		var modId = configClass.getAnnotation(Config.class).value();
		if (modId != null && !modId.isBlank()) return modId;
		throw new IllegalStateException("Mod id must be provided before building ConfigHandler");
	}
	private static void initConfigScreenForClient(String modId) {
		if (FMLEnvironment.dist.isDedicatedServer()) return;
		ModList.get().getModContainerById(modId).ifPresent(container -> ConfigScreenFactory.initConfigScreen(container, ID));
	}
	/** 将锁定值应用到活动配置（运行时）。不会修改 savedConfig，因此TOML文件不会被污染。 */
	public static void applyLockedValue(String modId, ValueConfigNode<Object, Object> node, Object parsed) {
		var handler = HANDLERS.get(modId);
		if (handler == null) return;
		node.setActiveValue(handler.getConfig(), parsed);
	}
	/** 从TOML重新加载并复制到两个配置，完全撤销任何锁定修改。 */
	public static void resetToSaved(String modId) {
		var handler = HANDLERS.get(modId).cast();
		var fresh = handler.load();
		handler.getConfigTree().copy(fresh, handler.getSavedConfig());
		handler.getConfigTree().copy(fresh, handler.getConfig());
	}
	/** 获取指定模组ID的活动配置POJO。 */
	public static Object getActiveConfig(String modId) {
		var handler = HANDLERS.get(modId);
		return handler != null ? handler.getConfig() : null;
	}
	/** 获取指定模组ID的根配置节点树。 */
	public static RootConfigNode<Object, Object> getRootConfigNode(String modId) {
		var handler = HANDLERS.get(modId).cast();
		return handler.getConfigTree();
	}
	/** 由仅客户端的 {@link ConfigScreenFactory} 用于创建配置界面。 */
	public static @NotNull ConfigHandler<?, ?> getHandler(String modId) {
		var handler = HANDLERS.get(modId);
		if (handler != null) return handler;
		throw new IllegalStateException("Config handler for id '%s' is not initialized.".formatted(modId));
	}
	/** 获取所有已注册模组的 modId。 */
	public static Set<String> getRegisteredModIds() {
		return HANDLERS.keySet();
	}
}
