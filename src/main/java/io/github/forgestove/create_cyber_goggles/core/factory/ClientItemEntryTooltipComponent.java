package io.github.forgestove.create_cyber_goggles.core.factory;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
public record ClientItemEntryTooltipComponent(ItemStack stack, int indent, Component label) implements ClientTooltipComponent {
	private static final int SLOT_WIDTH = 14;
	private static final int SLOT_HEIGHT = 14;
	private static final int TEXT_GAP = 0;
	public static void register(@NotNull RegisterClientTooltipComponentFactoriesEvent event) {
		event.register(
			ItemEntryTooltipComponent.class,
			data -> new ClientItemEntryTooltipComponent(data.stack(), data.indent(), data.label())
		);
	}
	@Override
	public int getHeight() {
		return SLOT_HEIGHT;
	}
	@Override
	public int getWidth(@NotNull Font font) {
		return indentPixels(font) + SLOT_WIDTH + TEXT_GAP + font.width(label);
	}
	@Override
	public void renderText(@NotNull Font font, int x, int y, @NotNull Matrix4f matrix, @NotNull BufferSource source) {
		var textX = x + indentPixels(font) + SLOT_WIDTH + TEXT_GAP;
		var textY = y + Mth.floor((SLOT_HEIGHT - font.lineHeight) / 2F);
		font.drawInBatch(label, textX, textY, -1, true, matrix, source, DisplayMode.NORMAL, 0, 15728880);
	}
	@Override
	public void renderImage(@NotNull Font font, int x, int y, @NotNull GuiGraphics guiGraphics) {
		var pose = guiGraphics.pose();
		pose.pushPose();
		var slotX = x + indentPixels(font);
		pose.translate(slotX, y, 450F);
		pose.scale(0.75F, 0.75F, 1F);
		guiGraphics.renderItem(stack, 0, 0);
		guiGraphics.renderItemDecorations(font, stack, 0, 0);
		pose.popPose();
	}
	private int indentPixels(@NotNull Font font) {
		return indent * font.width(" ");
	}
	public record ItemEntryTooltipComponent(ItemStack stack, int indent, Component label) implements TooltipComponent {
		public ItemEntryTooltipComponent(ItemStack stack) {
			this(stack, 0, stack.getHoverName());
		}
	}
}
