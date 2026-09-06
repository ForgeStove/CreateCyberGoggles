package io.github.forgestove.create_cyber_goggles.mixin.misc.jei;
import com.simibubi.create.content.logistics.redstoneRequester.*;
import com.simibubi.create.content.logistics.stockTicker.LogisticalStockRequestPacket;
import com.simibubi.create.foundation.gui.*;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.compat.jei.ScreenReferenced;
import io.github.forgestove.create_cyber_goggles.core.event.CCGKey;
import io.github.forgestove.create_cyber_goggles.core.util.RedstoneRequesterInteractions;
import io.github.forgestove.create_cyber_goggles.core.util.contract.Self;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(RedstoneRequesterScreen.class)
public abstract class RedstoneRequesterScreenMixin extends AbstractSimiContainerScreen<RedstoneRequesterMenu>
	implements Self<RedstoneRequesterScreen> {
	/** 扩展交互（ghost 槽拖拽、撤销）委托给工具类 */
	@Unique private RedstoneRequesterInteractions ccg$interactions;
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
	/** 在「完成」按钮左侧添加撤销按钮：点击恢复到打开界面时的初始内容 */
	@Inject(method = "init", at = @At("TAIL"))
	private void addUndoButton(CallbackInfo ci) {
		var x = getGuiLeft();
		var y = getGuiTop();
		var bgWidth = AllGuiTextures.REDSTONE_REQUESTER.getWidth();
		var bgHeight = AllGuiTextures.REDSTONE_REQUESTER.getHeight();
		var undo = new IconButton(x + bgWidth - 59, y + bgHeight - 25, AllIcons.I_CONFIG_RESET);
		undo.setToolTip(Component.translatable("config.ui.undo.tooltip"));
		undo.withCallback(() -> ccg$interactions.undo());
		addRenderableWidget(undo);
		// 首次进入时创建交互并缓存初始内容供撤销；从其他屏返回/窗口重排再次 init 只重绑按钮，
		// 不覆盖已缓存的基准（否则被改动过的内容会成为新的存档，撤销丢失最初状态）
		if (ccg$interactions == null) {
			ccg$interactions = new RedstoneRequesterInteractions(thiz());
			ccg$interactions.init(undo);
		} else ccg$interactions.retarget(undo);
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!CCG.config.misc.quickRequestActions) return super.mouseClicked(mouseX, mouseY, button);
		if (CCGKey.stockRequestSetter.isDown() && ccg$interactions.openPopupForHoveredSlot()) return true;
		if (button == 0 && ccg$interactions.onPress(mouseX, mouseY)) return true;
		return super.mouseClicked(mouseX, mouseY, button);
	}
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (!CCG.config.misc.quickRequestActions) return super.mouseReleased(mouseX, mouseY, button);
		if (button == 0 && ccg$interactions.onRelease()) return true;
		return super.mouseReleased(mouseX, mouseY, button);
	}
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (!CCG.config.misc.quickRequestActions) return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
		if (button == 0 && ccg$interactions.onDrag(mouseX, mouseY)) return true;
		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}
	/** 每帧更新撤销按钮可用状态并渲染悬空物品与源槽高亮 */
	@Inject(method = "renderForeground", at = @At("TAIL"))
	private void renderPicked(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
		if (!CCG.config.misc.quickRequestActions) return;
		ccg$interactions.tickUndoActive();
		ccg$interactions.renderPicked(graphics, font, mouseX, mouseY);
	}
}
