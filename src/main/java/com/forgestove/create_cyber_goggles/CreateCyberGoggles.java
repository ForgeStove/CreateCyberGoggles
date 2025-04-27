package com.forgestove.create_cyber_goggles;
import com.forgestove.create_cyber_goggles.config.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.minecraftforge.client.ConfigScreenHandler.ConfigScreenFactory;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
@Mod(CreateCyberGoggles.ID)
public class CreateCyberGoggles {
	public static final String ID = "create_cyber_goggles";
	public static ModConfig config = AutoConfig.register(ModConfig.class, Toml4jConfigSerializer::new).getConfig();
	public CreateCyberGoggles() {
		ModLoadingContext.get().registerExtensionPoint(
				ConfigScreenFactory.class,
				() -> new ConfigScreenFactory((m, s) -> AutoConfig.getConfigScreen(ModConfig.class, s).get())
		);
	}
}
