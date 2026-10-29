package io.github.forgestove.create_cyber_goggles.foundation.lang;
import com.demonwav.mcdev.annotations.Translatable;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.content.goggles.tooltip.TooltipComponentUtil;
import io.github.forgestove.create_cyber_goggles.content.goggles.tooltip.component.ClientFluidEntryTooltipComponent.FluidEntryTooltipComponent;
import io.github.forgestove.create_cyber_goggles.content.goggles.tooltip.component.ClientFluidListTooltipComponent.FluidListTooltipComponent;
import io.github.forgestove.create_cyber_goggles.content.goggles.tooltip.component.ClientItemEntryTooltipComponent.ItemEntryTooltipComponent;
import io.github.forgestove.create_cyber_goggles.content.goggles.tooltip.component.ClientItemListTooltipComponent.ItemListTooltipComponent;
import joptsimple.internal.Strings;
import net.createmod.catnip.lang.LangNumberFormat;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.Component.Serializer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.awt.Color;
import java.util.List;

import static io.github.forgestove.create_cyber_goggles.foundation.util.CCGUtil.mc;
public class CCGLangBuilder {
	public static final float DEFAULT_SPACE_WIDTH = 4.0F;
	public String namespace;
	private @Nullable MutableComponent component;
	public CCGLangBuilder(String namespace) {
		this.namespace = namespace;
	}
	static int getIndents(Font font, int defaultIndents) {
		int spaceWidth = font.width(" ");
		if (DEFAULT_SPACE_WIDTH == spaceWidth) return defaultIndents;
		return Mth.ceil(DEFAULT_SPACE_WIDTH * defaultIndents / spaceWidth);
	}
	public CCGLangBuilder space() {
		return text(" ");
	}
	public CCGLangBuilder text(String literalText) {
		return add(Component.literal(literalText));
	}
	public CCGLangBuilder translate(@Translatable(prefix = CCG.ID + '.', foldMethod = true) String langKey) {
		return add(Component.translatable(namespace + "." + langKey));
	}
	public CCGLangBuilder translate(@Translatable(prefix = CCG.ID + '.', foldMethod = true) String langKey, ChatFormatting format) {
		return add(Component.translatable(namespace + "." + langKey).withStyle(format));
	}
	public CCGLangBuilder translate(@Translatable(prefix = CCG.ID + '.', foldMethod = true) String langKey, Object... args) {
		return add(Component.translatable(namespace + "." + langKey, args));
	}
	public CCGLangBuilder translate(
		@Translatable(prefix = CCG.ID + '.', foldMethod = true) String langKey,
		ChatFormatting format,
		Object... args
	) {
		return add(Component.translatable(namespace + "." + langKey, args).withStyle(format));
	}
	public CCGLangBuilder add(MutableComponent customComponent) {
		if (customComponent.getStyle().isEmpty()) customComponent.withStyle(ChatFormatting.WHITE);
		component = component == null ? customComponent : component.append(customComponent);
		return this;
	}
	public CCGLangBuilder seconds() {
		return add(CCGCreateLang.translateDirect("generic.unit.seconds"));
	}
	public CCGLangBuilder seconds(ChatFormatting... formats) {
		return add(CCGCreateLang.translateDirect("generic.unit.seconds").withStyle(formats));
	}
	public CCGLangBuilder text(String literalText, int color) {
		return add(Component.literal(literalText).withStyle(style -> style.withColor(color)));
	}
	public CCGLangBuilder text(String literalText, net.createmod.catnip.theme.Color color) {
		return add(Component.literal(literalText).withStyle(style -> style.withColor(color.getRGB())));
	}
	public CCGLangBuilder number(double number) {
		return text(LangNumberFormat.format(number));
	}
	public CCGLangBuilder number(double number, int color) {
		return text(LangNumberFormat.format(number), color);
	}
	public CCGLangBuilder number(double number, ChatFormatting... formats) {
		return text(LangNumberFormat.format(number), formats);
	}
	public CCGLangBuilder number(float number) {
		return text(LangNumberFormat.format(number));
	}
	public CCGLangBuilder number(float number, int color) {
		return text(LangNumberFormat.format(number), color);
	}
	public CCGLangBuilder number(float number, ChatFormatting... formats) {
		return text(LangNumberFormat.format(number), formats);
	}
	public CCGLangBuilder number(int number) {
		return text(String.valueOf(number));
	}
	public CCGLangBuilder number(int number, int color) {
		return text(String.valueOf(number), color);
	}
	public CCGLangBuilder number(int number, ChatFormatting... formats) {
		return text(String.valueOf(number), formats);
	}
	public CCGLangBuilder is(boolean is) {
		return add(is
			? CCGLang.translateDirect("message.is").withStyle(ChatFormatting.GREEN)
			: CCGLang.translateDirect("message.not").withStyle(ChatFormatting.RED));
	}
	public CCGLangBuilder enabled(boolean enabled) {
		return add(enabled
			? CCGLang.translateDirect("message.enabled").withStyle(ChatFormatting.GREEN)
			: CCGLang.translateDirect("message.disabled").withStyle(ChatFormatting.RED));
	}
	public CCGLangBuilder progress(float progress, int totalBars) {
		var filledBars = (int) (Mth.clamp(progress, 0, 1) * totalBars);
		return text("|".repeat(filledBars), ChatFormatting.GREEN).text("|".repeat(totalBars - filledBars), ChatFormatting.GRAY);
	}
	public CCGLangBuilder text(String literalText, ChatFormatting... formats) {
		return add(Component.literal(literalText).withStyle(formats));
	}
	public CCGLangBuilder fraction(int current, int total) {
		return number(current, Color.HSBtoRGB((float) current / total * 0.33F, 1, 1)).text(" / ", ChatFormatting.GRAY)
			.number(total, ChatFormatting.DARK_GRAY);
	}
	public CCGLangBuilder fraction(float current, float total) {
		return number(current, Color.HSBtoRGB(current / total * 0.33F, 1, 1)).text(" / ", ChatFormatting.GRAY)
			.number(total, ChatFormatting.DARK_GRAY);
	}
	public CCGLangBuilder fraction(double current, double total) {
		return number(current, Color.HSBtoRGB((float) (current / total * 0.33), 1, 1)).text(" / ", ChatFormatting.GRAY)
			.number(total, ChatFormatting.DARK_GRAY);
	}
	public CCGLangBuilder add(Component component) {
		if (component instanceof MutableComponent mutableComponent) return add(mutableComponent);
		return add(component.copy());
	}
	public CCGLangBuilder style(ChatFormatting... formats) {
		if (component == null) return this;
		component = component.withStyle(formats);
		List<Component> siblings = component.getSiblings();
		siblings.replaceAll(c -> c.copy().withStyle(formats));
		return this;
	}
	public CCGLangBuilder color(net.createmod.catnip.theme.Color color) {
		return color(color.getRGB());
	}
	public CCGLangBuilder color(int color) {
		if (component != null) component = component.withStyle(s -> s.withColor(color));
		return this;
	}
	public CCGLangBuilder color(Color color) {
		return color(color.getRGB());
	}
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
	public void forGoggles(int index, List<? super MutableComponent> tooltip) {
		forGoggles(index, tooltip, 0);
	}
	public void forGoggles(int index, List<? super MutableComponent> tooltip, int indents) {
		tooltip.add(index,
			new CCGLangBuilder(namespace).text(Strings.repeat(' ', getIndents(mc.font, 4 + indents))).add(this).component());
	}
	public CCGLangBuilder itemName(ItemStack stack) {
		return add(stack.getHoverName().copy().setStyle(stack.getDisplayName().getStyle()));
	}
	public CCGLangBuilder itemEntry(ItemStack stack) {
		return itemEntry(stack, itemName(stack).component().copy());
	}
	public CCGLangBuilder itemEntry(ItemStack stack, Component label) {
		MutableComponent marker = Component.empty();
		TooltipComponentUtil.ITEM_ENTRY_MAP.put(marker, new ItemEntryTooltipComponent(stack.copy(), 0, label.copy()));
		return add(marker);
	}
	public CCGLangBuilder itemList(List<ItemStack> items, int maxColumns) {
		MutableComponent marker = Component.empty();
		List<ItemStack> copied = items.stream().map(ItemStack::copy).toList();
		TooltipComponentUtil.ITEM_LIST_MAP.put(marker, new ItemListTooltipComponent(copied, 0, maxColumns));
		return add(marker);
	}
	public CCGLangBuilder fluidEntry(FluidStack fluid, int capacityMb) {
		return fluidEntry(fluid, capacityMb, null);
	}
	public CCGLangBuilder fluidEntry(FluidStack fluid, int capacityMb, @Nullable Component label) {
		MutableComponent marker = Component.empty();
		TooltipComponentUtil.FLUID_ENTRY_MAP.put(
			marker,
			new FluidEntryTooltipComponent(fluid.copy(), 0, Math.max(1, capacityMb), 0, label)
		);
		return add(marker);
	}
	@SuppressWarnings("unused")
	public CCGLangBuilder fluidList(List<FluidStack> fluids, int maxColumns) {
		MutableComponent marker = Component.empty();
		List<FluidStack> copied = fluids.stream().map(FluidStack::copy).toList();
		TooltipComponentUtil.FLUID_LIST_MAP.put(marker, new FluidListTooltipComponent(copied, 0, maxColumns));
		return add(marker);
	}
}
