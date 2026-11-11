package io.github.forgestove.create_cyber_goggles.foundation.events;
import io.github.forgestove.create_cyber_goggles.foundation.lang.CCGLang;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack.Position;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.neoforge.event.AddPackFindersEvent;

import static io.github.forgestove.create_cyber_goggles.foundation.util.CCGUtil.getCCGRes;
public class ConfigLangPack {
	public static void register(AddPackFindersEvent event) {
		event.addPackFinders(
			getCCGRes("resourcepacks/create_config_i18n"),
			PackType.CLIENT_RESOURCES,
			CCGLang.translateDirect("resourcepack.config_i18n"),
			PackSource.BUILT_IN,
			false,
			Position.BOTTOM
		);
	}
}
