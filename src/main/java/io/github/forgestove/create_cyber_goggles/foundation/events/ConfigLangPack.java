package io.github.forgestove.create_cyber_goggles.foundation.events;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack.Position;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.neoforge.event.AddPackFindersEvent;

import static io.github.forgestove.create_cyber_goggles.foundation.util.CCGUtil.getCCGRes;
public class ConfigLangPack {
	public static void register(AddPackFindersEvent event) {
		event.addPackFinders(
			getCCGRes("resourcepacks/config_i18n"),
			PackType.CLIENT_RESOURCES,
			Component.literal("Config I18n"),
			PackSource.BUILT_IN,
			true,
			Position.BOTTOM
		);
	}
}
