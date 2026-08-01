package io.github.forgestove.create_cyber_goggles.core.util;
import com.simibubi.create.foundation.utility.CreateLang;
import joptsimple.internal.Strings;
import net.createmod.catnip.lang.LangNumberFormat;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.awt.Color;
import java.util.List;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
import static net.minecraft.ChatFormatting.*;
public class CCGLangBuilder {
	public static final float DEFAULT_SPACE_WIDTH = 4.0F;
	public String namespace;
	private @Nullable MutableComponent component;
	public CCGLangBuilder(String namespace) {
		this.namespace = namespace;
	}
	public CCGLangBuilder space() {
		return text(" ");
	}
	public CCGLangBuilder text(String literalText) {
		return add(Component.literal(literalText));
	}
	public CCGLangBuilder add(MutableComponent customComponent) {
		if (customComponent.getStyle().isEmpty()) customComponent.withStyle(WHITE);
		component = component == null ? customComponent : component.append(customComponent);
		return this;
	}
	public CCGLangBuilder seconds() {
		return add(CreateLang.translate("generic.unit.seconds").component());
	}
	public CCGLangBuilder seconds(ChatFormatting format) {
		return add(CreateLang.translate("generic.unit.seconds").style(format).component());
	}
	public CCGLangBuilder text(String literalText, int color) {
		return add(Component.literal(literalText).withStyle(style -> style.withColor(color)));
	}
	public CCGLangBuilder number(double number) {
		return text(LangNumberFormat.format(number));
	}
	public CCGLangBuilder number(double number, int color) {
		return text(LangNumberFormat.format(number), color);
	}
	public CCGLangBuilder number(double number, ChatFormatting format) {
		return text(LangNumberFormat.format(number), format);
	}
	public CCGLangBuilder number(float number) {
		return text(LangNumberFormat.format(number));
	}
	public CCGLangBuilder number(float number, int color) {
		return text(LangNumberFormat.format(number), color);
	}
	public CCGLangBuilder number(float number, ChatFormatting format) {
		return text(LangNumberFormat.format(number), format);
	}
	public CCGLangBuilder number(int number) {
		return text(String.valueOf(number));
	}
	public CCGLangBuilder number(int number, int color) {
		return text(String.valueOf(number), color);
	}
	public CCGLangBuilder number(int number, ChatFormatting format) {
		return text(String.valueOf(number), format);
	}
	public CCGLangBuilder is(boolean is) {
		return CCGLang.add(is
			? Component.translatable("create_cyber_goggles.message.is").withStyle(GREEN)
			: Component.translatable("create_cyber_goggles.message.not").withStyle(RED));
	}
	public CCGLangBuilder translate(String langKey, ChatFormatting format) {
		return add(Component.translatable(namespace + "." + langKey).withStyle(format));
	}
	public CCGLangBuilder enabled(boolean enabled) {
		return CCGLang.add(enabled
			? Component.translatable("create_cyber_goggles.message.enabled").withStyle(GREEN)
			: Component.translatable("create_cyber_goggles.message.disabled").withStyle(RED));
	}
	public CCGLangBuilder progress(float progress, int totalBars) {
		var filledBars = (int) (Mth.clamp(progress, 0, 1) * totalBars);
		return text("|".repeat(filledBars), GREEN).text("|".repeat(totalBars - filledBars), GRAY);
	}
	public CCGLangBuilder text(String literalText, ChatFormatting format) {
		return add(Component.literal(literalText).withStyle(format));
	}
	public CCGLangBuilder fraction(int current, int total) {
		return number(current, Color.HSBtoRGB((float) current / total * 0.33F, 1, 1)).text(" / ", GRAY).number(total, DARK_GRAY);
	}
	public CCGLangBuilder fraction(float current, float total) {
		return number(current, Color.HSBtoRGB(current / total * 0.33F, 1, 1)).text(" / ", GRAY).number(total, DARK_GRAY);
	}
	public CCGLangBuilder fraction(double current, double total) {
		return number(current, Color.HSBtoRGB((float) (current / total * 0.33), 1, 1)).text(" / ", GRAY).number(total, DARK_GRAY);
	}
	public CCGLangBuilder add(Component component) {
		if (component instanceof MutableComponent mutableComponent) return add(mutableComponent);
		return add(component.copy());
	}
	public CCGLangBuilder style(ChatFormatting... formats) {
		if (component == null) return this;
		component = component.withStyle(formats);
		var siblings = component.getSiblings();
		siblings.replaceAll(c -> c.copy().withStyle(formats));
		return this;
	}
	public CCGLangBuilder color(Color color) {
		return color(color.getRGB());
	}
	public CCGLangBuilder color(int color) {
		if (component != null) component = component.withStyle(s -> s.withColor(color));
		return this;
	}
	@SuppressWarnings("unused")
	public CCGLangBuilder fluidName(FluidStack stack) {
		return add(stack.getHoverName().copy());
	}
	public String string() {
		return component().getString();
	}
	public MutableComponent component() {
		if (component == null) throw new IllegalStateException("Component is null");
		return component;
	}
	@SuppressWarnings("unused")
	public String json() {
		return Serializer.toJson(component(), RegistryAccess.EMPTY);
	}
	@SuppressWarnings("unused")
	public void sendStatus(Player player) {
		player.displayClientMessage(component(), true);
	}
	@SuppressWarnings("unused")
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
	public void forGoggles(List<? super MutableComponent> tooltip, int indents) {
		tooltip.add(new CCGLangBuilder(namespace).text(Strings.repeat(' ', getIndents(mc.font, 4 + indents))).add(this).component());
	}
	public CCGLangBuilder add(CCGLangBuilder builder) {
		return add(builder.component());
	}
	static int getIndents(Font font, int defaultIndents) {
		var spaceWidth = font.width(" ");
		if (DEFAULT_SPACE_WIDTH == spaceWidth) return defaultIndents;
		return Mth.ceil(DEFAULT_SPACE_WIDTH * defaultIndents / spaceWidth);
	}
	public void forGoggles(int index, List<? super MutableComponent> tooltip) {
		forGoggles(index, tooltip, 0);
	}
	public void forGoggles(int index, List<? super MutableComponent> tooltip, int indents) {
		tooltip.add(index,
			new CCGLangBuilder(namespace).text(Strings.repeat(' ', getIndents(mc.font, 4 + indents))).add(this).component());
	}
}
