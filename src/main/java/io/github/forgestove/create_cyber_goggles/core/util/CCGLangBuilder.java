package io.github.forgestove.create_cyber_goggles.core.util;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import joptsimple.internal.Strings;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.text.DecimalFormat;
import java.util.List;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.mc;
import static net.minecraft.ChatFormatting.*;
@SuppressWarnings("unused")
public class CCGLangBuilder {
	public static final float DEFAULT_SPACE_WIDTH = 4.0F;
	public String namespace;
	private @Nullable MutableComponent component;
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
			if (args[i] instanceof CCGLangBuilder builder) args[i] = builder.component();
		return args;
	}
	public CCGLangBuilder space() {
		return text(" ");
	}
	public CCGLangBuilder newLine() {
		return text("\n");
	}
	public CCGLangBuilder seconds() {
		return add(CreateLang.translate("generic.unit.seconds").component());
	}
	public CCGLangBuilder seconds(ChatFormatting format) {
		return add(CreateLang.translate("generic.unit.seconds").style(format).component());
	}
	public CCGLangBuilder translate(String langKey) {
		return add(Component.translatable(namespace + "." + langKey));
	}
	public CCGLangBuilder translate(String langKey, ChatFormatting format) {
		return add(Component.translatable(namespace + "." + langKey).withStyle(format));
	}
	public CCGLangBuilder translate(String langKey, Object... args) {
		return add(Component.translatable(namespace + "." + langKey, resolveBuilders(args)));
	}
	public CCGLangBuilder translate(String langKey, ChatFormatting format, Object... args) {
		return add(Component.translatable(namespace + "." + langKey, resolveBuilders(args)).withStyle(format));
	}
	public CCGLangBuilder text(String literalText) {
		return add(Component.literal(literalText));
	}
	public CCGLangBuilder text(String literalText, ChatFormatting format) {
		return add(Component.literal(literalText).withStyle(format));
	}
	public CCGLangBuilder text(String literalText, int color) {
		return add(Component.literal(literalText).withStyle(style -> style.withColor(color)));
	}
	public CCGLangBuilder number(double number) {
		return text(formatNumber(number));
	}
	public CCGLangBuilder number(double number, int color) {
		return text(formatNumber(number), color);
	}
	public CCGLangBuilder number(double number, ChatFormatting format) {
		return text(formatNumber(number), format);
	}
	public CCGLangBuilder number(float number) {
		return text(formatNumber(number));
	}
	public CCGLangBuilder number(float number, int color) {
		return text(formatNumber(number), color);
	}
	public CCGLangBuilder number(float number, ChatFormatting format) {
		return text(formatNumber(number), format);
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
		return is ? translate("message.is", GREEN) : translate("message.not", RED);
	}
	public CCGLangBuilder enabled(boolean enabled) {
		return enabled ? translate("message.enabled", GREEN) : translate("message.disabled", RED);
	}
	public CCGLangBuilder progress(float progress, int totalBars) {
		var filledBars = (int) (Mth.clamp(progress, 0, 1) * totalBars);
		return text("|".repeat(filledBars), GREEN).text("|".repeat(totalBars - filledBars), GRAY);
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
	public CCGLangBuilder add(CCGLangBuilder builder) {
		return add(builder.component());
	}
	public CCGLangBuilder add(MutableComponent customComponent) {
		if (customComponent.getStyle().isEmpty()) customComponent.withStyle(WHITE);
		component = component == null ? customComponent : component.append(customComponent);
		return this;
	}
	public CCGLangBuilder add(Component component) {
		if (component instanceof MutableComponent mutableComponent) return add(mutableComponent);
		return add(component.copy());
	}
	public CCGLangBuilder style(ChatFormatting... formats) {
		if (component != null) component = component.withStyle(formats);
		return this;
	}
	public CCGLangBuilder color(int color) {
		if (component != null) component = component.withStyle(s -> s.withColor(color));
		return this;
	}
	public CCGLangBuilder color(Color color) {
		return color(color.getRGB());
	}
	public CCGLangBuilder fluidName(FluidStack stack) {
		var fluidKey = BuiltInRegistries.FLUID.getKey(stack.getFluid());
			return add(Component.translatable(fluidKey.toLanguageKey("fluid")));
	}
	public MutableComponent component() {
		if (component == null) throw new IllegalStateException("Component is null");
		return component;
	}
	public String string() {
		return component().getString();
	}
	public String json() {
		return component().getString();
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
		tooltip.add(new CCGLangBuilder(namespace).text(Strings.repeat(' ', getIndents(mc.font, 4 + indents))).add(this).component());
	}
	public void forGoggles(int index, List<? super MutableComponent> tooltip, int indents) {
		tooltip.add(index,
			new CCGLangBuilder(namespace).text(Strings.repeat(' ', getIndents(mc.font, 4 + indents))).add(this).component());
	}
	private static String formatNumber(double number) {
		if (number == (long) number) return String.valueOf((long) number);
		return new DecimalFormat("#.##").format(number);
	}
	private static String formatNumber(float number) {
		if (number == (long) number) return String.valueOf((long) number);
		return new DecimalFormat("#.##").format(number);
	}
}
