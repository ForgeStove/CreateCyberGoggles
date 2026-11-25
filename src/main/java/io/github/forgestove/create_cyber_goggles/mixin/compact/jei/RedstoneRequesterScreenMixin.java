package io.github.forgestove.create_cyber_goggles.mixin.compact.jei;
import com.simibubi.create.content.logistics.redstoneRequester.*;
import com.simibubi.create.content.logistics.stockTicker.LogisticalStockRequestPacket;
import com.simibubi.create.foundation.gui.*;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.compat.jei.ScreenReferenced;
import io.github.forgestove.create_cyber_goggles.content.factoryPanel.MultiplyScreen;
import io.github.forgestove.create_cyber_goggles.content.redstoneRequester.RedstoneRequesterInteractions;
import io.github.forgestove.create_cyber_goggles.foundation.events.CCGKey;
import io.github.forgestove.create_cyber_goggles.foundation.util.Self;
import io.github.forgestove.create_cyber_goggles.mixin.accessor.RedstoneRequesterScreenAccessor;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
@Mixin(RedstoneRequesterScreen.class)
public abstract class RedstoneRequesterScreenMixin extends AbstractSimiContainerScreen<RedstoneRequesterMenu>
	implements Self<RedstoneRequesterScreen> {
	@Unique private RedstoneRequesterInteractions ccg$interactions;
	public RedstoneRequesterScreenMixin(RedstoneRequesterMenu container, Inventory inv, Component title) {
		super(container, inv, title);
	}
	@Inject(method = "init", at = @At("HEAD"))
	private void linkScreen(CallbackInfo ci) {
		RedstoneRequesterMenu requesterMenu = thiz().getMenu();
		((ScreenReferenced) requesterMenu).ccg$setScreenReference(thiz());
		if (CCG.config.misc.jei.redstoneRequesterJEIRequest && requesterMenu.contentHolder != null)
			CatnipServices.NETWORK.sendToServer(new LogisticalStockRequestPacket(requesterMenu.contentHolder.getBlockPos()));
	}
	@Inject(method = "init", at = @At("TAIL"))
	private void addUndoButton(CallbackInfo ci) {
		int x = getGuiLeft();
		int y = getGuiTop();
		int bgWidth = AllGuiTextures.REDSTONE_REQUESTER.getWidth();
		int bgHeight = AllGuiTextures.REDSTONE_REQUESTER.getHeight();
		var undo = new IconButton(x + bgWidth - 59, y + bgHeight - 25, AllIcons.I_CONFIG_RESET);
		undo.setToolTip(Component.translatable("flexconfig.undo.tooltip"));
		undo.withCallback(() -> ccg$interactions.undo());
		addRenderableWidget(undo);
		if (ccg$interactions == null) {
			ccg$interactions = new RedstoneRequesterInteractions(thiz());
			ccg$interactions.init(undo);
		} else ccg$interactions.retarget(undo);
	}
	@Inject(method = "init", at = @At("TAIL"))
	private void addMultiplier(CallbackInfo ci) {
		if (!CCG.config.misc.multiplyAllValues) return;
		addRenderableWidget(MultiplyScreen.createButton(getGuiLeft() + 48, getGuiTop() + 95, this::ccg$multiplyAll));
	}
	@Unique
	private void ccg$multiplyAll(double factor) {
		List<Integer> amounts = ((RedstoneRequesterScreenAccessor) thiz()).getAmounts();
		ItemStackHandler ghost = thiz().getMenu().ghostInventory;
		int max = CCG.config.misc.removeRequestLimit ? Integer.MAX_VALUE : 256;
		for (var i = 0; i < amounts.size(); i++)
			if (!ghost.getStackInSlot(i).isEmpty()) amounts.set(i, MultiplyScreen.scale(amounts.get(i), factor, 1, max));
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!CCG.config.misc.quickRequestActions) return super.mouseClicked(mouseX, mouseY, button);
		if (CCGKey.stockRequestSetter.isDown() && ccg$interactions.openPopupForHoveredSlot()) return true;
		if (button == 0 && ccg$interactions.onPress(mouseX, mouseY)) return true;
		return super.mouseClicked(mouseX, mouseY, button);
	}
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (!CCG.config.misc.quickRequestActions) return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
		if (button == 0 && ccg$interactions.onDrag(mouseX, mouseY)) return true;
		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (!CCG.config.misc.quickRequestActions) return super.mouseReleased(mouseX, mouseY, button);
		if (button == 0 && ccg$interactions.onRelease()) return true;
		return super.mouseReleased(mouseX, mouseY, button);
	}
	@Inject(method = "renderForeground", at = @At("TAIL"))
	private void renderPicked(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
		if (!CCG.config.misc.quickRequestActions) return;
		ccg$interactions.tickUndoActive();
		ccg$interactions.renderPicked(graphics, font, mouseX, mouseY);
	}
}
