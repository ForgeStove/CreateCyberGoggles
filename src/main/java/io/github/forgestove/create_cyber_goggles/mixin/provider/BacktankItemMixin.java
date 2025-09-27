package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.simibubi.create.content.equipment.armor.*;
import io.github.forgestove.create_cyber_goggles.CCGLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import java.awt.Color;
import java.util.List;
@Mixin(BacktankItem.class)
public abstract class BacktankItemMixin extends ArmorItem {
	public BacktankItemMixin(Holder<ArmorMaterial> material, Type type, Properties properties) {
		super(material, type, properties);
	}
	@Override
	public void appendHoverText(
		@NotNull ItemStack stack,
		@NotNull TooltipContext context,
		@NotNull List<Component> tooltip,
		@NotNull TooltipFlag tooltipFlag
	) {
		var airLevel = BacktankItem.getRemainingAir(stack);
		var totalBars = 20;
		var max = BacktankUtil.maxAir(stack);
		var percent = (float) airLevel / max;
		var filledBars = (int) (percent * totalBars);
		CCGLang.translate("tooltip.airLevel").style(ChatFormatting.GRAY).addTo(tooltip);
		CCGLang.text(Color.HSBtoRGB(percent * 0.33f, 1.0f, 1.0f), "%d/%d".formatted(max, airLevel)).addTo(tooltip);
		CCGLang.text(ChatFormatting.GREEN, "|".repeat(filledBars))
			.add(CCGLang.text("|".repeat(totalBars - filledBars)).style(ChatFormatting.GRAY))
			.addTo(tooltip);
	}
}
