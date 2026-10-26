package io.github.forgestove.create_cyber_goggles.gui;
import com.simibubi.create.content.logistics.filter.*;
import io.github.forgestove.create_cyber_goggles.util.FilterPreviewUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
public class ReadOnlyPackageFilterScreen extends PackageFilterScreen {
	public ReadOnlyPackageFilterScreen(PackageFilterMenu menu, Inventory inv, Component title) {
		super(menu, inv, title);
	}
	@Override
	protected void init() {
		super.init();
		FilterPreviewUtil.lock(this);
	}
	@Override
	protected void slotClicked(@Nullable Slot slot, int slotId, int mouseButton, @NotNull ClickType type) {}
}
