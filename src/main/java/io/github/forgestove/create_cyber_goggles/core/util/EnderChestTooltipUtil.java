package io.github.forgestove.create_cyber_goggles.core.util;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
public final class EnderChestTooltipUtil {
	private static volatile boolean opened;
	private static volatile List<ItemStack> cachedItems = List.of();
	public static void capture(@NotNull PlayerEnderChestContainer container) {
		var snapshot = new ArrayList<ItemStack>();
		for (var i = 0; i < container.getContainerSize(); i++) snapshot.add(container.getItem(i));
		cachedItems = Collections.unmodifiableList(snapshot);
		opened = true;
	}
	public static @NotNull List<ItemStack> getCachedItems() {
		if (!opened) return List.of();
		return cachedItems;
	}
}
