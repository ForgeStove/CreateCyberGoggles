package io.github.forgestove.create_cyber_goggles.content.filterPreview;
import com.simibubi.create.content.logistics.filter.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.*;
public class ReadOnlyAttributeFilterScreen extends AttributeFilterScreen {
	public ReadOnlyAttributeFilterScreen(int id, Inventory inv, ItemStack stack) {
		super(AttributeFilterMenu.create(id, inv, stack), inv, stack.getHoverName());
	}
	@Override
	protected void init() {
		super.init();
		FilterPreviewUtil.lock(this);
	}
	@Override
	protected void slotClicked(@Nullable Slot slot, int slotId, int mouseButton, @NotNull ClickType type) {}
}
