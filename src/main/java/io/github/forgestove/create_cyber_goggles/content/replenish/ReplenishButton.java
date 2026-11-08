package io.github.forgestove.create_cyber_goggles.content.replenish;

import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import net.createmod.catnip.gui.widget.AbstractSimiWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import static io.github.forgestove.create_cyber_goggles.foundation.util.CCGUtil.mc;

public class ReplenishButton extends AbstractSimiWidget {
	private final ResourceLocation bg;
	private final AllGuiTextures hover;
	private final boolean hoverNative;
	private final AllIcons innerIcon;
	private final Component label;
	public ReplenishButton(
		int x,
		int y,
		int w,
		int h,
		ResourceLocation bg,
		AllGuiTextures hover,
		boolean hoverNative,
		AllIcons innerIcon,
		Component label,
		Runnable action
	) {
		super(x, y, w, h, label == null ? Component.empty() : label);
		this.bg = bg;
		this.hover = hover;
		this.hoverNative = hoverNative;
		this.innerIcon = innerIcon;
		this.label = label;
		withCallback(action);
	}
	public ReplenishButton tooltip(Component text) {
		toolTip.clear();
		toolTip.add(text);
		return this;
	}
	@Override
	public void doRender(@NotNull GuiGraphics gui, int mx, int my, float pt) {
		if (isHovered() && hover != null) if (hoverNative) {
			int hw = hover.getWidth(), hh = hover.getHeight();
			gui.blit(
				hover.location,
				getX() + (getWidth() - hw) / 2,
				getY() + (getHeight() - hh) / 2,
				hover.getStartX(),
				hover.getStartY(),
				hw,
				hh
			);
		} else gui.blit(hover.location, getX(), getY(), hover.getStartX(), hover.getStartY(), getWidth(), getHeight());
		else if (bg != null) gui.blitSprite(bg, getX(), getY(), getWidth(), getHeight());
		if (innerIcon != null) innerIcon.render(gui, getX() + (getWidth() - 16) / 2, getY() + (getHeight() - 16) / 2);
		if (label != null) {
			int tw = mc.font.width(label);
			gui.drawString(mc.font, label, getX() + (getWidth() - tw) / 2, getY() + (getHeight() - 9) / 2 + 1, 0x252525, false);
		}
	}
}
