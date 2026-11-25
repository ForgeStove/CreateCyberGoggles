package io.github.forgestove.create_cyber_goggles;
import io.github.forgestove.flexconfig.api.registration.ConfigHandler;
import net.neoforged.fml.common.Mod;
@Mod(CCG.ID)
public class CCG {
	public static final String ID = "create_cyber_goggles";
	public static final CCGConfig config = ConfigHandler.builder(CCGConfig.class).lockable(true).build().getConfig();
}
