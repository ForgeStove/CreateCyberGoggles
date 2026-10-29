package io.github.forgestove.create_cyber_goggles.content.filterPreview;
import com.simibubi.create.content.logistics.filter.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import org.jetbrains.annotations.*;
public class ReadOnlyAttributeFilterScreen extends AttributeFilterScreen {
	public ReadOnlyAttributeFilterScreen(AttributeFilterMenu menu, Inventory inv, Component title) {
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
