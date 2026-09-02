package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.simibubi.create.content.logistics.redstoneRequester.*;
import com.simibubi.create.foundation.gui.menu.GhostItemMenu;
import io.github.forgestove.create_cyber_goggles.compat.jei.ScreenReferenced;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.*;
/** 给红石请求器菜单添加 screenReference，使 JEI 转移时能拿到对应 Screen 更新请求数量 */
@Mixin(RedstoneRequesterMenu.class)
public abstract class RedstoneRequesterMenuMixin extends GhostItemMenu<RedstoneRequesterBlockEntity> implements ScreenReferenced {
	@Unique private Screen ccg$screenReference;
	protected RedstoneRequesterMenuMixin(MenuType<?> type, int id, Inventory inv, RedstoneRequesterBlockEntity contentHolder) {
		super(type, id, inv, contentHolder);
	}
	@Override
	public void ccg$setScreenReference(Screen screen) {
		ccg$screenReference = screen;
	}
	@Override
	public Screen ccg$getScreenReference() {
		return ccg$screenReference;
	}
}
