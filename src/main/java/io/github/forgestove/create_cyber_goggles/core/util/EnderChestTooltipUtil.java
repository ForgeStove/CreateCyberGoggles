package io.github.forgestove.create_cyber_goggles.core.util;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import org.jetbrains.annotations.NotNull;

import java.util.*;
public final class EnderChestTooltipUtil {
	public static volatile List<ItemStack> cachedItems = Collections.emptyList();
	public static void capture(@NotNull PlayerEnderChestContainer container) {
		cachedItems = Collections.unmodifiableList(container.getItems());
	}
	public static void clear(LoggingOut ignoredEvent) {
		cachedItems = Collections.emptyList();
	}
}
