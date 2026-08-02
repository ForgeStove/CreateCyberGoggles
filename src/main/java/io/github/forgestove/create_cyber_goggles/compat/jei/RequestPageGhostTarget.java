package io.github.forgestove.create_cyber_goggles.compat.jei;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterMenu;
import com.simibubi.create.foundation.gui.menu.GhostItemSubmitPacket;
import io.github.forgestove.create_cyber_goggles.api.RequestPageProvider;
import mezz.jei.api.gui.handlers.IGhostIngredientHandler.Target;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
/* JEI 拖入红石请求器当前页某格的目标（映射到 ghostInventory 对应页的槽） */ public class RequestPageGhostTarget<I> implements Target<I> {
	private final RedstoneRequesterMenu menu;
	private final Rect2i area;
	private final int gridIndex;
	public RequestPageGhostTarget(RedstoneRequesterMenu menu, int gridIndex, Rect2i area) {
		this.menu = menu;
		this.gridIndex = gridIndex;
		this.area = area;
	}
	@Override
	public Rect2i getArea() {
		return area;
	}
	@Override
	public void accept(I ingredient) {
		var slot = ((RequestPageProvider) menu).ccg$getRequestPage() * 9 + gridIndex;
		var stack = ((ItemStack) ingredient).copy();
		stack.setCount(1);
		menu.ghostInventory.setStackInSlot(slot, stack);
		CatnipServices.NETWORK.sendToServer(new GhostItemSubmitPacket(stack, slot));
	}
}
