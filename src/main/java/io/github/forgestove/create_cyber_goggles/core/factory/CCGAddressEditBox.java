package io.github.forgestove.create_cyber_goggles.core.factory;
import com.simibubi.create.content.logistics.AddressEditBox;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;
/**
 * 自动补齐界面专用的地址输入框：Create 的 {@link AddressEditBox} 是 widget，
 * 渲染发生在 {@code AbstractSimiScreen} 关闭内容区 scissor 之后，滚出视口也不会被裁
 * （右挂的剪贴板图标会整体画到视口外）。这里在自身渲染时重新套上内容区裁剪。
 *
 * <p>代价：Create 在 {@code renderWidget} 里一并绘制的地址下拉建议也会被裁，
 * 地址框靠近视口顶部时下拉会被切掉一截（且仍可点击）。</p>
 */
public class CCGAddressEditBox extends AddressEditBox {
	private final Screen screen;
	public CCGAddressEditBox(Screen screen, Font font, int x, int y, int width, int height, boolean anchorToBottom) {
		super(screen, font, x, y, width, height, anchorToBottom);
		this.screen = screen;
	}
	@Override
	public void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
		if (screen instanceof AutoReplenishScreen owner) gui.enableScissor(owner.scissorL, owner.scissorT, owner.scissorR, owner.scissorB);
		super.renderWidget(gui, mouseX, mouseY, partialTick);
		if (screen instanceof AutoReplenishScreen) gui.disableScissor();
	}
}
