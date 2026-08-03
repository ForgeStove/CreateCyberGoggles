package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.redstoneRequester.*;
import com.simibubi.create.content.logistics.redstoneRequester.RedstoneRequesterMenu.SorterProofSlot;
import com.simibubi.create.content.logistics.stockTicker.*;
import com.simibubi.create.foundation.gui.menu.GhostItemMenu;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.api.*;
import io.github.forgestove.create_cyber_goggles.mixin.accessor.RedstoneRequesterScreenAccessor;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
/** 给红石请求器菜单添加 screenReference，使 JEI 转移时能拿到对应 Screen 更新请求数量 */
@Mixin(RedstoneRequesterMenu.class)
public abstract class RedstoneRequesterMenuMixin extends GhostItemMenu<RedstoneRequesterBlockEntity>
	implements ScreenReferenced, RequestPageProvider {
	@Unique private Object ccg$screenReference;
	/** 分页当前页码（Screen 与 JEI 拖入共享） */
	@Unique private int ccg$requestPage;
	protected RedstoneRequesterMenuMixin(MenuType<?> type, int id, Inventory inv, RedstoneRequesterBlockEntity contentHolder) {
		super(type, id, inv, contentHolder);
	}
	@Override
	public void ccg$setScreenReference(Object screen) {
		ccg$screenReference = screen;
	}
	@Override
	public int ccg$getRequestPage() {
		return ccg$requestPage;
	}
	@Override
	public void ccg$setRequestPage(int page) {
		ccg$requestPage = page;
	}
	/** 保存时用 Screen 数量构建请求（转移数量 = 配方连续格数，对动力合成正确） */
	@Inject(
		method = "saveData(Lcom/simibubi/create/content/logistics/redstoneRequester/RedstoneRequesterBlockEntity;)V",
		at = @At("HEAD"),
		cancellable = true
	)
	private void saveData(RedstoneRequesterBlockEntity contentHolder, CallbackInfo ci) {
		if (!CCG.config.misc.redstoneRequesterLargeRequest) return;
		if (contentHolder.getLevel() == null || !contentHolder.getLevel().isClientSide) return;
		var screen = ccg$getScreenReference() instanceof RedstoneRequesterScreen s ? s : null;
		var amounts = screen == null ? null : ((RedstoneRequesterScreenAccessor) screen).getAmounts();
		List<BigItemStack> list = new ArrayList<>();
		for (var i = 0; i < ghostInventory.getSlots(); i++) {
			var stackInSlot = ghostInventory.getStackInSlot(i);
			if (stackInSlot.isEmpty()) continue;  // 不保留空槽占位，避免请求携带空气
			var count = amounts != null && i < amounts.size() ? amounts.get(i) : 1;
			list.add(new BigItemStack(stackInSlot.copyWithCount(1), count));
		}
		var newRequest = new PackageOrderWithCrafts(new PackageOrder(list), contentHolder.encodedRequest.orderedCrafts());
		if (!newRequest.orderedStacksMatchOrderedRecipes()) newRequest = PackageOrderWithCrafts.simple(newRequest.stacks());
		contentHolder.encodedRequest = newRequest;
		contentHolder.sendData();
		ci.cancel();
	}
	@Override
	public Object ccg$getScreenReference() {
		return ccg$screenReference;
	}
	/** 突破 9 格：幽灵库存扩到 81 槽，支持大配方请求 */
	@ModifyConstant(method = "createGhostInventory", constant = @Constant(intValue = 9))
	private int ghostInventorySize(int constant) {
		return CCG.config.misc.redstoneRequesterLargeRequest ? 81 : constant;
	}
	/** 81 槽全部注册在屏幕外，由 Screen 分页自绘当前 9 格（GhostItemMenu 基类按 ghostInventory 槽数索引，需注册全部 Slot 防越界） */
	@Inject(method = "addSlots", at = @At("HEAD"), cancellable = true)
	private void addSlots(CallbackInfo ci) {
		if (!CCG.config.misc.redstoneRequesterLargeRequest) return;
		addPlayerSlots(5, 142);
		for (var i = 0; i < 81; i++)
			addSlot(new SorterProofSlot(ghostInventory, i, -999, -999));
		ci.cancel();
	}
}
