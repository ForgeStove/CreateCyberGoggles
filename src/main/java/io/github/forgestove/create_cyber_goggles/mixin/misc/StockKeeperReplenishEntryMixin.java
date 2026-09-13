package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.*;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.factory.*;
import io.github.forgestove.create_cyber_goggles.core.util.contract.Self;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
/**
 * 在 Create {@link StockKeeperRequestScreen} 右下角加「自动补齐缺货」。
 * 需求来源 = 剪贴板目标成品（{@link ClipboardEntry} icon+itemAmount），求解交给与宿主无关的
 * {@link ReplenishPlanner}；本 mixin 只提供宿主能力（库存快照 {@link StockTickerBlockEntity}、发订单通道）。
 */
@Mixin(StockKeeperRequestScreen.class)
public abstract class StockKeeperReplenishEntryMixin extends AbstractSimiContainerScreen<StockKeeperRequestMenu>
	implements Self<StockKeeperRequestScreen>, CCGReplenishTree {
	@Shadow List<List<ClipboardEntry>> clipboardItem;
	@Shadow StockTickerBlockEntity blockEntity;
	public StockKeeperReplenishEntryMixin(StockKeeperRequestMenu container, Inventory inv, Component title) {
		super(container, inv, title);
	}
	@Inject(method = "init", at = @At("TAIL"))
	private void addAutoReplenishButton(CallbackInfo ci) {
		if (!CCG.config.misc.autoReplenishStock || !thiz().isSchematicListMode()) return;
		var btn = new IconButton(leftPos + imageWidth - 10, topPos + 20, AllIcons.I_ADD);
		btn.withCallback(() -> {
			List<ReplenishGroup> groups = ccg$buildGroups();
			var summary = ccg$summary();
			CCG.LOGGER.debug("AutoReplenish click: groups={} summary={}", groups.size(), summary == null ? "null" : "ok");
			mc.setScreen(new AutoReplenishScreen(thiz(), this, groups));
		});
		btn.setToolTip(Component.translatable("create_cyber_goggles.gui.auto_replenish.title"));
		addRenderableWidget(btn);
	}
	/** {@link CCGReplenishTree}：界面切换配方后要重新构建整棵树 */
	@Unique
	@Override
	public List<ReplenishGroup> ccg$buildGroups() {
		return ReplenishPlanner.build(clipboardItem, ccg$summary());
	}
	@Unique
	@Override
	public InventorySummary ccg$summary() {
		return blockEntity.getLastClientsideStockSnapshotAsSummary();
	}
	@Unique
	@Override
	public void ccg$setRecipeChoice(Item item, ResourceLocation recipeId) {
		ReplenishPlanner.setRecipeChoice(item, recipeId);
	}
	@Unique
	@Override
	public void ccg$sendOrder(PackageOrderWithCrafts order, String address) {
		CatnipServices.NETWORK.sendToServer(new PackageOrderRequestPacket(blockEntity.getBlockPos(), order, address, false));
	}
}
