package io.github.forgestove.create_cyber_goggles;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity.FuelType;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity;
import com.simibubi.create.content.schematics.cannon.SchematicannonBlockEntity.State;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.List;
public class TooltipUtil {
	/**
	 * 为风扇组件添加悬浮提示信息。
	 * <p>
	 * 此方法根据风扇的推/拉状态和作用范围，格式化并添加相应的提示文本。
	 *
	 * @param tooltip 需要添加提示信息的组件列表
	 * @param pushing 风扇是否处于推动模式（{@code true}为推动，{@code false}为拉动）
	 * @param range   风扇的作用范围（原始值）
	 * @param divide  范围除数，用于计算显示的实际范围值
	 * @return 如果范围为{@code 0}则返回{@code false}，否则返回{@code true}
	 */
	public static boolean addFanTooltip(List<Component> tooltip, boolean pushing, float range, int divide) {
		if (range == 0) return false;
		CCGLang.translate("tooltip.windState").forGoggles(tooltip);
		CCGLang.number(range / divide)
			.space()
			.translate(pushing ? "tooltip.pushRange" : "tooltip.pullRange")
			.color(pushing ? CCG.CONFIG.delayRender.windPushColor : CCG.CONFIG.delayRender.windPullColor)
			.forGoggles(tooltip);
		return true;
	}
	/**
	 * 为燃烧室添加悬浮提示信息，显示燃烧状态、剩余燃烧时间和燃料类型颜色标识
	 *
	 * @param tooltip           用于显示提示信息的组件列表
	 * @param remainingBurnTime 剩余燃烧时间（单位：{@code tick}）
	 * @param isCreative        是否为创造模式燃烧室
	 * @param activeFuel        当前激活的燃料类型
	 * @return 总是返回{@code true}
	 */
	public static boolean addBurnerTooltip(List<Component> tooltip, int remainingBurnTime, boolean isCreative, FuelType activeFuel) {
		CCGLang.translate("tooltip.burnerState").forGoggles(tooltip);
		CCGLang.text(isCreative ? "∞" : String.format("%.2f", remainingBurnTime / 20f))
			.text(String.format(" / %d ", BlazeBurnerBlockEntity.MAX_HEAT_CAPACITY / 20))
			.translate("tooltip.seconds")
			.style(switch (activeFuel) {
				case SPECIAL -> ChatFormatting.AQUA;
				case NORMAL -> ChatFormatting.YELLOW;
				default -> ChatFormatting.GRAY;
			})
			.forGoggles(tooltip);
		return true;
	}
	/**
	 * 为蓝图加农炮添加悬浮提示信息。
	 * <p>
	 * 此方法会根据加农炮的当前状态，向 {@link List<Component> tooltip} 列表中添加如下信息：
	 * <ul>
	 *   <li>加农炮状态（如运行中、暂停等）</li>
	 *   <li>剩余火药量百分比</li>
	 *   <li>剩余可发射次数（含备份物品）</li>
	 *   <li>若为创造模式箱，则显示特殊提示</li>
	 *   <li>若加农炮正在运行，显示打印进度百分比及颜色</li>
	 * </ul>
	 *
	 * @param tooltip 需要添加提示信息的组件列表
	 * @param sbe     当前的 SchematicannonBlockEntity 实例
	 * @return 总是返回{@code true}
	 */
	public static boolean addCannonTooltip(List<Component> tooltip, @NotNull SchematicannonBlockEntity sbe) {
		CCGLang.translate("tooltip.cannonState").forGoggles(tooltip);
		CreateLang.translate("schematicannon.status." + sbe.statusMsg).style(ChatFormatting.GOLD).forGoggles(tooltip);
		var shotsLeft = sbe.remainingFuel;
		var shotsLeftWithItems = shotsLeft + sbe.inventory.getStackInSlot(4).getCount() * sbe.getShotsPerGunpowder();
		if (sbe.hasCreativeCrate) {
			CreateLang.translate("gui.schematicannon.gunpowderLevel", "" + 100).forGoggles(tooltip);
			CCGLang.text("(").add(AllBlocks.CREATIVE_CRATE.get().getName()).text(")").style(ChatFormatting.DARK_PURPLE).forGoggles(tooltip);
		} else {
			var fillPercent = (int) (shotsLeft / (float) sbe.getShotsPerGunpowder() * 100);
			CreateLang.translate("gui.schematicannon.gunpowderLevel", fillPercent).forGoggles(tooltip);
			CreateLang.translate("gui.schematicannon.shotsRemaining", CCGLang.text(Integer.toString(shotsLeft)).style(ChatFormatting.BLUE))
				.style(ChatFormatting.GRAY)
				.forGoggles(tooltip);
			if (shotsLeftWithItems != shotsLeft) CreateLang.translate(
				"gui.schematicannon.shotsRemainingWithBackup",
				CCGLang.text(Integer.toString(shotsLeftWithItems)).style(ChatFormatting.BLUE)
			).style(ChatFormatting.GRAY).forGoggles(tooltip);
		}
		if (sbe.state.equals(State.RUNNING)) {
			var progress = sbe.schematicProgress * 100;
			CCGLang.translate("tooltip.printProgress").forGoggles(tooltip);
			CCGLang.text(String.format("%d/%d", sbe.blocksPlaced, sbe.blocksToPlace))
				.text(String.format(" (%.2f%%)", progress))
				.color(Color.HSBtoRGB(sbe.schematicProgress * 0.33f, 1.0f, 1.0f))
				.forGoggles(tooltip);
			var totalBars = 32;
			var filledBars = (int) (sbe.schematicProgress * totalBars);
			CCGLang.text("|".repeat(filledBars))
				.style(ChatFormatting.GREEN)
				.add(CCGLang.text("|".repeat(totalBars - filledBars)).style(ChatFormatting.GRAY))
				.forGoggles(tooltip);
		}
		return true;
	}
}
