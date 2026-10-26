package io.github.forgestove.create_cyber_goggles.api;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
@FunctionalInterface
public interface FilterPreviewFactory {
	Screen create(int id, Inventory inventory, ItemStack filter);
}
