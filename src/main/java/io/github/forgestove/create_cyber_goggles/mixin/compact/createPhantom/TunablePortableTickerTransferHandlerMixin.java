package io.github.forgestove.create_cyber_goggles.mixin.compact.createPhantom;
import com.yision.phantom.compat.jei.TunablePortableTickerTransferHandler;
import io.github.forgestove.create_cyber_goggles.CCG;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
/**
 * 让可调便携仓储管理员的 JEI 拖拽也支持动力合成配方（原料数 10~81），与 Create 仓管的
 * {@code StockKeeperTransferHandlerMixin} 同款：宿主原本硬编码 9（原料 >9 直接报错、订单满 9 拒绝、
 * crafting grid 9 格），会把大配方截成 9 个原料 → 用户点发送时就少了东西。
 */
@Pseudo
@Mixin(TunablePortableTickerTransferHandler.class)
public abstract class TunablePortableTickerTransferHandlerMixin {
	@ModifyConstant(method = "transferRecipeOnClient", constant = @Constant(intValue = 9))
	private int ccg$allowLargeCrafting(int constant) {
		return CCG.config.misc.jei.allowLargeCrafting ? 81 : constant;
	}
	/** JEI 计算 transfer 时用的目标合成格也是 9 格，同样要放开 */
	@ModifyConstant(method = "createCraftingGridSlots", constant = @Constant(intValue = 9))
	private int ccg$largeCraftingGrid(int constant) {
		return CCG.config.misc.jei.allowLargeCrafting ? 81 : constant;
	}
}
