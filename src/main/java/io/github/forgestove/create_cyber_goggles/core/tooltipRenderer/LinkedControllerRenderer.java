package io.github.forgestove.create_cyber_goggles.core.tooltipRenderer;
import com.simibubi.create.content.redstone.link.controller.LinkedControllerItem;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.api.AutoTooltipRenderer;
import io.github.forgestove.create_cyber_goggles.core.util.CCGMods;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.awt.Point;
import java.util.ArrayList;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
@AutoTooltipRenderer
public final class LinkedControllerRenderer extends AbstractItemGridRenderer {
	private static final ResourceLocation CONTROLLER_BG = CCGMods.create.rl("textures/gui/curiosities_2.png");
	private static final int TEXTURE_SIZE = 256;
	/** 面板裁剪区域：左上/右下两个点 */
	private static final Point CROP_TOP_LEFT = new Point(1, 14);
	private static final Point CROP_BOTTOM_RIGHT = new Point(170, 80);
	/** 槽位左上角，按 buildItemGrid 槽位顺序 */
	private static final Point[] SLOT_POINTS = {
		new Point(12, 34),
		new Point(12, 52),
		new Point(36, 34),
		new Point(36, 52),
		new Point(60, 34),
		new Point(60, 52),
		new Point(84, 34),
		new Point(84, 52),
		new Point(119, 34),
		new Point(119, 52),
		new Point(143, 34),
		new Point(143, 52)
	};
	@Override
	public boolean supports(ItemStack stack) {
		return CCG.config.tooltip.linkedController && stack.getItem() instanceof LinkedControllerItem;
	}
	@Override
	public int width(ItemStack stack) {
		return hasItems(stack) ? CROP_BOTTOM_RIGHT.x - CROP_TOP_LEFT.x : 0;
	}
	private boolean hasItems(ItemStack stack) {
		return buildItemGrid(stack) != null;
	}
	@Override
	public @Nullable OverlayData buildItemGrid(ItemStack stack) {
		var frequencyItems = LinkedControllerItem.getFrequencyItems(stack);
		var items = new ArrayList<ItemStack>(12);
		var hasAnyItem = false;
		for (var row = 0; row < 2; row++)
			for (var column = 0; column < 6; column++) {
				var slotIndex = column * 2 + row;
				var slot = frequencyItems.getStackInSlot(slotIndex);
				if (!slot.isEmpty()) hasAnyItem = true;
				items.add(slot.copyWithCount(1));
			}
		return hasAnyItem ? new OverlayData(items, 6) : null;
	}
	@Override
	public int height(ItemStack stack) {
		return hasItems(stack) ? CROP_BOTTOM_RIGHT.y - CROP_TOP_LEFT.y : 0;
	}
	@Override
	public void render(GuiGraphics gui, ItemStack stack, int x, int y) {
		var data = buildItemGrid(stack);
		if (data == null) return;
		var pose = gui.pose();
		pose.pushPose();
		pose.translate(x, y, 600F);
		var cropWidth = CROP_BOTTOM_RIGHT.x - CROP_TOP_LEFT.x;
		var cropHeight = CROP_BOTTOM_RIGHT.y - CROP_TOP_LEFT.y;
		gui.blit(CONTROLLER_BG, 0, 0, CROP_TOP_LEFT.x, CROP_TOP_LEFT.y, cropWidth, cropHeight, TEXTURE_SIZE, TEXTURE_SIZE);
		for (var i = 0; i < SLOT_POINTS.length; i++) {
			var item = data.items().get(i);
			var slot = SLOT_POINTS[i];
			var slotX = slot.x - CROP_TOP_LEFT.x;
			var slotY = slot.y - CROP_TOP_LEFT.y;
			gui.renderItem(item, slotX, slotY);
			gui.renderItemDecorations(mc.font, item, slotX, slotY);
		}
		pose.popPose();
	}
}
