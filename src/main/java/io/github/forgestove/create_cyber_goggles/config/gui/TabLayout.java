package io.github.forgestove.create_cyber_goggles.config.gui;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.*;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;
public class TabLayout implements Layout {
	private final ConfigCategoryTab<?> tab;
	public TabLayout(ConfigCategoryTab<?> tab) {
		this.tab = tab;
	}
	@Override
	public void visitChildren(Consumer<LayoutElement> consumer) {
		consumer.accept(tab.list);
	}
	@Override
	public void removeChildren() {
	}
	@Override
	public int getX() {
		return tab.list.getX();
	}
	@Override
	public void setX(int x) {
		tab.list.setX(x);
	}
	@Override
	public int getY() {
		return tab.list.getY();
	}
	@Override
	public void setY(int y) {
		tab.list.setY(y);
	}
	@Override
	public int getWidth() {
		return tab.list.getWidth();
	}
	@Override
	public int getHeight() {
		return tab.list.getHeight();
	}
	@Override
	public void visitWidgets(@NonNull Consumer<AbstractWidget> consumer) {
		tab.list.visitWidgets(consumer);
	}
}
