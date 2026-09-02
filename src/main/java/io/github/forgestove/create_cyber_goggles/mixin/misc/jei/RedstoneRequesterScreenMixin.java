package io.github.forgestove.create_cyber_goggles.mixin.misc.jei;
import com.simibubi.create.content.logistics.redstoneRequester.*;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterMenu.SorterProofSlot;
import com.simibubi.create.content.logistics.stockTicker.LogisticalStockRequestPacket;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.compat.jei.ScreenReferenced;
import io.github.forgestove.create_cyber_goggles.core.event.CCGKey;
import io.github.forgestove.create_cyber_goggles.core.factory.RequestAmountScreen;
import io.github.forgestove.create_cyber_goggles.core.util.contract.Self;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
@Mixin(RedstoneRequesterScreen.class)
public abstract class RedstoneRequesterScreenMixin extends AbstractSimiContainerScreen<RedstoneRequesterMenu>
	implements Self<RedstoneRequesterScreen> {
	@Shadow private List<Integer> amounts;
	public RedstoneRequesterScreenMixin(RedstoneRequesterMenu container, Inventory inv, Component title) {
		super(container, inv, title);
	}
	/** 打开界面时把当前 Screen 关联到菜单，供 JEI 转移读取 */
	@Inject(method = "init", at = @At("HEAD"))
	private void linkScreen(CallbackInfo ci) {
		var requesterMenu = thiz().getMenu();
		((ScreenReferenced) requesterMenu).ccg$setScreenReference(thiz());
		// 打开界面时请求一次网络库存，供 JEI 转移按库存选择原料
		if (CCG.config.misc.jei.redstoneRequesterJEIRequest && requesterMenu.contentHolder != null)
			CatnipServices.NETWORK.sendToServer(new LogisticalStockRequestPacket(requesterMenu.contentHolder.getBlockPos()));
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (CCG.config.misc.quickRequestActions && CCGKey.stockRequestSetter.isDown() && ccg$openPopupForHoveredSlot()) return true;
		return super.mouseClicked(mouseX, mouseY, button);
	}
	@Unique
	private boolean ccg$openPopupForHoveredSlot() {
		if (!(hoveredSlot instanceof SorterProofSlot ghostSlot)) return false;
		var index = ghostSlot.getSlotIndex();
		var stack = ghostSlot.getItem();
		if (stack.isEmpty()) return false;
		mc.setScreen(new RequestAmountScreen(
			thiz(),
			stack,
			amounts.get(index),
			CCG.config.misc.removeRequestLimit ? Integer.MAX_VALUE : 256,
			count -> amounts.set(index, count)
		));
		return true;
	}
}
