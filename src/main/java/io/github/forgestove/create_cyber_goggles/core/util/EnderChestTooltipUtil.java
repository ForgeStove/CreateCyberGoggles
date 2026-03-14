package io.github.forgestove.create_cyber_goggles.core.util;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
public final class EnderChestTooltipUtil {
	private static volatile boolean opened;
	private static volatile List<ItemStack> cachedItems = List.of();
	public static void capture(@NotNull PlayerEnderChestContainer container) {
		var snapshot = new ArrayList<ItemStack>();
		for (var i = 0; i < container.getContainerSize(); i++) {
			var stack = container.getItem(i);
			if (stack.isEmpty()) continue;
			snapshot.add(stack.copy());
		}
		cachedItems = Collections.unmodifiableList(snapshot);
		opened = true;
	}
	public static void appendForItem(@NotNull List<Component> tooltip) {
		if (!opened) return;
		if (cachedItems.isEmpty()) return;
		CCGLang.itemList(cachedItems, 9).addTo(1, tooltip);
	}
}
