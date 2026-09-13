package io.github.forgestove.create_cyber_goggles.mixin.compact.createMobilePackages;
import de.theidler.create_mobile_packages.compat.jei.DroneControllerTransferHandler;
import io.github.forgestove.create_cyber_goggles.CCG;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
/**
 * 让 CMP 便携仓储管理员的 JEI 拖拽也支持动力合成配方（原料数 10~81），与 Create 仓管的
 * {@code StockKeeperTransferHandlerMixin} 同款：宿主原本硬编码 9（订单满 9 拒绝、待加类型数 >9 拒绝、
 * crafting grid 9 格），会把大配方截成 9 个原料 → 用户点发送时就少了东西。
 */
@Pseudo
@Mixin(DroneControllerTransferHandler.class)
public abstract class DroneControllerTransferHandlerMixin {
	@ModifyConstant(method = "transferRecipeOnClient", constant = @Constant(intValue = 9))
	private int ccg$allowLargeCrafting(int constant) {
		return CCG.config.misc.jei.allowLargeCrafting ? 81 : constant;
	}
	/** 订单类型上限是 long 比较（`(long)size() + … >= 9L`），常量类型单独改 */
	@ModifyConstant(method = "transferRecipeOnClient", constant = @Constant(longValue = 9L))
	private long ccg$allowLargeCraftingLong(long constant) {
		return CCG.config.misc.jei.allowLargeCrafting ? 81L : constant;
	}
	/** 流体分支的「还能塞几个新类型」判定同样是 9 */
	@ModifyConstant(method = "canFitNewTypes", constant = @Constant(intValue = 9))
	private int ccg$largeTypeLimit(int constant) {
		return CCG.config.misc.jei.allowLargeCrafting ? 81 : constant;
	}
}
