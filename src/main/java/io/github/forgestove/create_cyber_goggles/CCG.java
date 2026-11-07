package io.github.forgestove.create_cyber_goggles;
import com.mojang.logging.LogUtils;
import io.github.forgestove.flexconfig.api.registration.ConfigHandler;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
@Mod(CCG.ID)
public final class CCG {
	public static final String ID = "create_cyber_goggles";
	public static final Logger LOGGER = LogUtils.getLogger();
	public static final CCGConfig config = ConfigHandler.builder(CCGConfig.class).build().getConfig();
}
