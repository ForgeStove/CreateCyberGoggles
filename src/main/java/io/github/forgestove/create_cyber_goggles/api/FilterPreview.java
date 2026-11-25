package io.github.forgestove.create_cyber_goggles.api;
import com.simibubi.create.AllItems;
import io.github.forgestove.create_cyber_goggles.content.filterPreview.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
public class FilterPreview {
	public static final byte previewId = -1;
	private static final Map<Item, FilterPreviewFactory> FACTORIES = new ConcurrentHashMap<>();
	static {
		register(AllItems.FILTER.get(), ReadOnlyFilterScreen::new);
		register(AllItems.ATTRIBUTE_FILTER.get(), ReadOnlyAttributeFilterScreen::new);
		register(AllItems.PACKAGE_FILTER.get(), ReadOnlyPackageFilterScreen::new);
	}
	public static void register(Item item, FilterPreviewFactory factory) {
		FACTORIES.put(Objects.requireNonNull(item, "item"), Objects.requireNonNull(factory, "factory"));
	}
	public static @Nullable Screen create(int id, Inventory inventory, ItemStack filter) {
		FilterPreviewFactory factory = FACTORIES.get(filter.getItem());
		return factory == null ? null : factory.create(id, inventory, filter);
	}
}
