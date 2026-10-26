package io.github.forgestove.create_cyber_goggles.compat.emi;
import com.simibubi.create.AllMenuTypes;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterMenu;
import dev.emi.emi.api.*;
import dev.emi.emi.api.recipe.handler.EmiRecipeHandler;
import dev.emi.emi.registry.EmiRecipeFiller;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.world.inventory.MenuType;

import java.util.List;
/**
 * EMI 插件入口。红石请求器的填充不依赖 JEI，仅装 EMI 时也应可用。
 */
@EmiEntrypoint
@SuppressWarnings("unused")
public final class CCGEmiPlugin implements EmiPlugin {
	@Override
	public void register(EmiRegistry registry) {
		if (!CCG.config.misc.jei.redstoneRequesterJEIRequest) return;
		MenuType<RedstoneRequesterMenu> type = AllMenuTypes.REDSTONE_REQUESTER.get();
		registry.addRecipeHandler(type, RedstoneRequesterEmiHandler.INSTANCE);
		// 排到最前，避免被 JEMI 桥接的 JEI handler 抢走（JEMI 未覆盖 getTooltip）
		List<EmiRecipeHandler<?>> handlers = EmiRecipeFiller.handlers.get(type);
		if (handlers == null) return;
		handlers.remove(RedstoneRequesterEmiHandler.INSTANCE);
		handlers.addFirst(RedstoneRequesterEmiHandler.INSTANCE);
	}
}
