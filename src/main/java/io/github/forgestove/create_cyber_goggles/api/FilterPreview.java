package io.github.forgestove.create_cyber_goggles.api;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.logistics.filter.*;
import io.github.forgestove.create_cyber_goggles.gui.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
/**
 * 预览过滤器界面的工厂注册表，按物品查找；后注册者覆盖先注册者。
 * <p>仅客户端，注册应在客户端初始化阶段完成。</p>
 */
@SuppressWarnings("unused")
public final class FilterPreview {
	private static final Map<Item, FilterPreviewFactory> FACTORIES = new ConcurrentHashMap<>(Map.of(
		AllItems.FILTER.get(),
		(id, inv, stack) -> new ReadOnlyFilterScreen(FilterMenu.create(id, inv, stack), inv, stack.getHoverName()),
		AllItems.ATTRIBUTE_FILTER.get(),
		(id, inv, stack) -> new ReadOnlyAttributeFilterScreen(AttributeFilterMenu.create(id, inv, stack), inv, stack.getHoverName()),
		AllItems.PACKAGE_FILTER.get(),
		(id, inv, stack) -> new ReadOnlyPackageFilterScreen(PackageFilterMenu.create(id, inv, stack), inv, stack.getHoverName())
	));
	public static void register(Item item, FilterPreviewFactory factory) {
		FACTORIES.put(Objects.requireNonNull(item, "item"), Objects.requireNonNull(factory, "factory"));
	}
	public static @Nullable Screen create(int id, Inventory inventory, ItemStack filter) {
		FilterPreviewFactory factory = FACTORIES.get(filter.getItem());
		return factory == null ? null : factory.create(id, inventory, filter);
	}
}
