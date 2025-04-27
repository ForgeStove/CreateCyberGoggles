package com.forgestove.create_cyber_goggles;
import com.forgestove.create_cyber_goggles.config.ModConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.jetbrains.annotations.NotNull;
@Mod(value = CreateCyberGoggles.ID, dist = Dist.CLIENT)
public class CreateCyberGoggles {
	public static final String ID = "create_cyber_goggles";
	public static ModConfig config = AutoConfig.register(ModConfig.class, Toml4jConfigSerializer::new).getConfig();
	public CreateCyberGoggles(@NotNull ModContainer container) {
		container.registerExtensionPoint(
				IConfigScreenFactory.class, (modContainer, screen) -> AutoConfig.getConfigScreen(ModConfig.class, screen).get()
		);
	}
}
