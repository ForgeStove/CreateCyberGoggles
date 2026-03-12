package io.github.forgestove.create_cyber_goggles.core.util;
import joptsimple.internal.Strings;
import net.createmod.catnip.theme.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.List;
@SuppressWarnings("unused")
public class CCGLangBuilder {
	public static final float DEFAULT_SPACE_WIDTH = 4.0F;
	public String namespace;
	@Nullable MutableComponent component;
	public CCGLangBuilder(String namespace) {
		this.namespace = namespace;
	}
	static int getIndents(Font font, int defaultIndents) {
		var spaceWidth = font.width(" ");
		if (DEFAULT_SPACE_WIDTH == spaceWidth) return defaultIndents;
		return Mth.ceil(DEFAULT_SPACE_WIDTH * defaultIndents / spaceWidth);
	}
	public static Object[] resolveBuilders(Object[] args) {
		for (var i = 0; i < args.length; i++)
			if (args[i] instanceof CCGLangBuilder cb) args[i] = cb.component();
		return args;
	}
	public CCGLangBuilder space() {
		return text(" ");
	}
	public CCGLangBuilder newLine() {
		return text("\n");
	}
	public CCGLangBuilder translate(String langKey, Object... args) {
		var args1 = resolveBuilders(args);
		return add(Component.translatable(namespace + "." + langKey, args1));
	}
	public CCGLangBuilder text(String literalText) {
		return add(Component.literal(literalText));
	}
	public CCGLangBuilder text(ChatFormatting format, String literalText) {
		return add(Component.literal(literalText).withStyle(format));
	}
	public CCGLangBuilder text(int color, String literalText) {
		return add(Component.literal(literalText).withStyle(s -> s.withColor(color)));
	}
	public CCGLangBuilder add(CCGLangBuilder otherBuilder) {
		return add(otherBuilder.component());
	}
	public CCGLangBuilder add(MutableComponent customComponent) {
		component = component == null ? customComponent : component.append(customComponent);
		return this;
	}
	public CCGLangBuilder add(Component component) {
		if (component instanceof MutableComponent mutableComponent) return add(mutableComponent);
		else return add(component.copy());
	}
	public CCGLangBuilder style(ChatFormatting format) {
		if (component != null) component = component.withStyle(format);
		return this;
	}
	public CCGLangBuilder color(int color) {
		if (component != null) component = component.withStyle(s -> s.withColor(color));
		return this;
	}
	public CCGLangBuilder color(Color color) {
		return this.color(color.getRGB());
	}
	public MutableComponent component() {
		if (component == null) throw new IllegalStateException("Component is null");
		return component;
	}
	public String string() {
		return component().getString();
	}
	public String json() {
		return Component.Serializer.toJson(component());
	}
	public void sendStatus(Player player) {
		player.displayClientMessage(component(), true);
	}
	public void sendChat(Player player) {
		player.displayClientMessage(component(), false);
	}
	public void addTo(List<? super MutableComponent> tooltip) {
		tooltip.add(component());
	}
	public void addTo(int index, List<? super MutableComponent> tooltip) {
		tooltip.add(index, component());
	}
	public void forGoggles(List<? super MutableComponent> tooltip) {
		forGoggles(tooltip, 0);
	}
	public void forGoggles(int index, List<? super MutableComponent> tooltip) {
		forGoggles(index, tooltip, 0);
	}
	public void forGoggles(List<? super MutableComponent> tooltip, int indents) {
		tooltip.add(new CCGLangBuilder(namespace).text(Strings.repeat(' ', getIndents(Minecraft.getInstance().font, 4 + indents)))
			.add(this)
			.component());
	}
	public void forGoggles(int index, List<? super MutableComponent> tooltip, int indents) {
		tooltip.add(
			index,
			new CCGLangBuilder(namespace).text(Strings.repeat(' ', getIndents(Minecraft.getInstance().font, 4 + indents)))
				.add(this)
				.component()
		);
	}
}
