package io.github.forgestove.flexconfig.api;
import java.util.*;
/** 配置更改完全生效所需的动作。 */
public enum RestartType {
	/**
	 * 重新加载世界即可：退出到标题界面再进世界。
	 * 不需要重启游戏进程。
	 */
	WORLD,
	/**
	 * 需要重启整个游戏进程。
	 * <p>
	 * 不能用于 {@link ConfigType#SERVER} 型配置：那类配置每次进世界都会重新加载，与「要重启游戏」自相矛盾。
	 */
	GAME(ConfigType.SERVER);
	private final Set<ConfigType> invalidTypes;
	RestartType(ConfigType... invalidTypes) {
		this.invalidTypes = EnumSet.noneOf(ConfigType.class);
		this.invalidTypes.addAll(Arrays.asList(invalidTypes));
	}
	/** 该重启级别是否可用于指定类型的配置；配置树构建期用它校验。 */
	public boolean isValid(ConfigType type) {
		return !invalidTypes.contains(type);
	}
}
