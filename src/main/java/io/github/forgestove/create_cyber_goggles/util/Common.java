package io.github.forgestove.create_cyber_goggles.util;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.logistics.filter.*;
import com.simibubi.create.foundation.gui.ScreenOpener;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import org.jetbrains.annotations.Nullable;
public class Common {
	/**
	 * 在屏幕中央区域渲染指定物品堆的图标及关联的悬浮提示信息。
	 *
	 * @param guiGraphics GUI渲染上下文对象，用于执行图形绘制操作
	 * @param itemStack   需要渲染的物品堆实例。若值为null或空物品堆叠时方法立即返回
	 */
	public static void renderItemStack(GuiGraphics guiGraphics, ItemStack itemStack) {
		if (itemStack == null || itemStack.isEmpty()) return;
		var mc = Minecraft.getInstance();
		var font = mc.font;
		var tooltipFlag = mc.options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL;
		var tooltipLines = itemStack.getTooltipLines(mc.player, tooltipFlag);
		var height = Math.max(10, tooltipLines.size() * font.lineHeight - 60);
		var x = guiGraphics.guiWidth() / 2;
		var y = guiGraphics.guiHeight() / 2;
		guiGraphics.renderItem(itemStack, x + 10, y - 15);
		guiGraphics.renderItemDecorations(font, itemStack, x + 10, y - 15);
		guiGraphics.renderTooltip(font, itemStack, x + 22, y - height);
	}
	/**
	 * 获取当前玩家选中的方块实体，并将其转换为 {@link KineticBlockEntity} 类型。
	 * 如果选中的方块实体不是 {@link KineticBlockEntity} 类型，则返回 null。
	 *
	 * @return 当前选中的 {@link KineticBlockEntity} 实例，如果没有选中或类型不匹配则返回 null
	 */
	public static @Nullable KineticBlockEntity getSelectedKBE() {
		if (!(getSelectedBE() instanceof KineticBlockEntity kbe)) return null;
		return kbe;
	}
	/**
	 * 获取当前玩家选中的方块实体。
	 * 如果没有选中方块或选中的方块不是 {@link BlockEntity} 类型，则返回 null。
	 *
	 * @return 当前选中的 {@link BlockEntity} 实例，如果没有选中或类型不匹配则返回 null
	 */
	public static @Nullable BlockEntity getSelectedBE() {
		var mc = Minecraft.getInstance();
		if (mc.level == null) return null;
		if (!(mc.hitResult instanceof BlockHitResult blockHitResult)) return null;
		if (!(blockHitResult.getType() == Type.BLOCK)) return null;
		return mc.level.getBlockEntity(blockHitResult.getBlockPos());
	}
	/**
	 * 显示一条格式化的客户端消息。
	 *
	 * @param currentValue 当前值，用于确定消息的启用或禁用状态
	 * @param messageKey   消息的键，用于生成完整的消息标识符
	 */
	public static void displayClientMessage(boolean currentValue, String messageKey) {
		var mc = Minecraft.getInstance();
		if (mc.player == null || mc.screen != null) return;
		var formatted = "message.%s.%sable%s".formatted(CCG.ID, currentValue ? "en" : "dis", messageKey);
		mc.player.displayClientMessage(Component.translatable(formatted), true);
	}
	/**
	 * 打开与指定过滤器物品相关的筛选器界面。
	 * <p>
	 * 此方法会根据过滤器物品的类型，动态打开对应的筛选器界面。
	 *
	 * @param filter 需要打开筛选器界面的物品堆实例。
	 *               如果物品不是 {@link FilterItem} 类型，方法将立即返回。
	 */
	public static void openFilterScreen(ItemStack filter) {
		SafeRun.run(() -> {
			if (!(filter.getItem() instanceof FilterItem filterItem)) return;
			var mc = Minecraft.getInstance();
			if (mc.player == null) return;
			var inv = mc.player.getInventory();
			var name = filter.getHoverName();
			var field = FilterItem.class.getDeclaredField("type");
			field.setAccessible(true);
			var ordinal = ((Enum<?>) field.get(filterItem)).ordinal();
			ScreenOpener.open(switch (ordinal) {
				case 0 -> new FilterScreen(FilterMenu.create(-1, inv, filter), inv, name);
				case 1 -> new AttributeFilterScreen(AttributeFilterMenu.create(-1, inv, filter), inv, name);
				default -> throw new IllegalStateException("Unexpected value: " + ordinal);
			});
		});
	}
}
