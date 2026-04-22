package io.github.forgestove.create_cyber_goggles.core.factory;
import io.github.forgestove.create_cyber_goggles.core.util.SlotUtil;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
public final class ClientItemEntryTooltipComponent implements ClientTooltipComponent {
	private final ItemStack stack;
	private final int indent;
	private final Component label;
	public ClientItemEntryTooltipComponent(ItemStack stack, int indent, Component label) {
		this.stack = stack;
		this.indent = indent;
		this.label = label.copy().withStyle(stack.getDisplayName().getStyle());
	}
	public static void register(@NotNull RegisterClientTooltipComponentFactoriesEvent event) {
		event.register(
			ItemEntryTooltipComponent.class,
			data -> new ClientItemEntryTooltipComponent(data.stack(), data.indent(), data.label())
		);
	}
	@Override
	public int getHeight() {
		return SlotUtil.SIZE_SLIM;
	}
	@Override
	public int getWidth(@NotNull Font font) {
		return indentPixels(font) + SlotUtil.SIZE_SLIM + font.width(label);
	}
	@Override
	public void renderText(@NotNull Font font, int x, int y, @NotNull Matrix4f matrix, @NotNull BufferSource source) {
		var textX = x + indentPixels(font) + SlotUtil.SIZE_SLIM;
		var textY = y + Mth.floor((SlotUtil.SIZE_SLIM - font.lineHeight) / 2F);
		font.drawInBatch(label, textX, textY, -1, true, matrix, source, DisplayMode.NORMAL, 0, 15728880);
	}
	@Override
	public void renderImage(@NotNull Font font, int x, int y, @NotNull GuiGraphics gui) {
		var pose = gui.pose();
		pose.pushPose();
		var slotX = x + indentPixels(font);
		pose.translate(slotX, y, 450F);
		pose.scale(0.75F, 0.75F, 1F);
		gui.renderItem(stack, 0, 0);
		gui.renderItemDecorations(font, stack, 0, 0);
		pose.popPose();
	}
	private int indentPixels(@NotNull Font font) {
		return indent * font.width(" ");
	}
	public record ItemEntryTooltipComponent(ItemStack stack, int indent, Component label) implements TooltipComponent {}
}
