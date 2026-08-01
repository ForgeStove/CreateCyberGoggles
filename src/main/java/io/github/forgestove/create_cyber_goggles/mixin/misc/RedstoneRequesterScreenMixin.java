package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.logistics.redstoneRequester.*;
import com.simibubi.create.content.logistics.stockTicker.LogisticalStockRequestPacket;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.api.*;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
@Mixin(RedstoneRequesterScreen.class)
public abstract class RedstoneRequesterScreenMixin implements Self<RedstoneRequesterScreen> {
	@Shadow private List<Integer> amounts;
	/** 打开界面时把当前 Screen 关联到菜单，供 JEI 转移读取 */
	@Inject(method = "init", at = @At("HEAD"))
	private void ccg$linkScreen(CallbackInfo ci) {
		if (thiz().getMenu() instanceof RedstoneRequesterMenu menu) {
			((ScreenReferenced) menu).ccg$setScreenReference(thiz());
			// 打开界面时请求一次网络库存，供 JEI 转移按库存选择原料
			if (CCG.config.misc.jei.redstoneRequesterJEIRequest && menu.contentHolder != null)
				CatnipServices.NETWORK.sendToServer(new LogisticalStockRequestPacket(menu.contentHolder.getBlockPos()));
		}
	}
	@ModifyArg(
		method = "mouseScrolled", at = @At(
		value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(III)I"
	), index = 2
	)
	public int modifyMaxScrollAmount(int max) {
		return CCG.config.misc.removeRequestLimit ? Integer.MAX_VALUE : max;
	}
	@ModifyConstant(method = "mouseScrolled", constant = @Constant(intValue = 10))
	public int modifyPerScrollAmount(int original, @Local(name = "i") int i) {
		return CCG.config.misc.removeRequestLimit ? Item.DEFAULT_MAX_STACK_SIZE - (amounts.get(i) == 1 ? 1 : 0) : original;
	}
}
