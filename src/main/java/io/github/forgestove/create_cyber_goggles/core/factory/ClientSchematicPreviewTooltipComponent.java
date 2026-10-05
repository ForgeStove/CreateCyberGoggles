package io.github.forgestove.create_cyber_goggles.core.factory;
import io.github.forgestove.create_cyber_goggles.core.schematic.SchematicPreviewPanel;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import org.jetbrains.annotations.NotNull;
/**
 * 蓝图物品 tooltip 里的 3D 预览块：按住 Alt 悬停蓝图时显示。
 * <p>
 * 面板实例是静态共享的 —— tooltip 渲染生命周期极短，每帧重建 {@link SchematicPreviewPanel} 会反复装载蓝图。
 */
public final class ClientSchematicPreviewTooltipComponent implements ClientTooltipComponent {
	private static final SchematicPreviewPanel PANEL = new SchematicPreviewPanel();
	private final SchematicPreviewTooltip preview;
	public static void register(@NotNull RegisterClientTooltipComponentFactoriesEvent event) {
		event.register(SchematicPreviewTooltip.class, ClientSchematicPreviewTooltipComponent::new);
	}
	private ClientSchematicPreviewTooltipComponent(SchematicPreviewTooltip preview) {
		this.preview = preview;
	}
	@Override
	public int getHeight() {
		return preview.height();
	}
	@Override
	public int getWidth(@NotNull Font font) {
		return preview.width();
	}
	@Override
	public void renderImage(@NotNull Font font, int x, int y, @NotNull GuiGraphics graphics) {
		PANEL.setSelected(preview.fileName());
		PANEL.render(graphics, x, y, preview.width(), preview.height());
	}
	public record SchematicPreviewTooltip(String fileName, int width, int height) implements TooltipComponent {}
}
