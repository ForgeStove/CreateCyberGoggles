package io.github.forgestove.create_cyber_goggles.core.factory;
import net.createmod.catnip.theme.Color;
import org.jetbrains.annotations.*;
@SuppressWarnings("unused")
public enum TooltipTheme {
	Default,
	Vanilla(0xF0100010, 0x505000FF, 0x5028007F),
	Dark(0x80000000, 0x00000000, 0x00000000),
	Create(0xC0101010, 0x50505050, 0x30303030),
	Cyber(0x80000020, 0xFF00FF80, 0x8000FF80),
	Neon(0x800000FF, 0xFFFF00FF, 0x8000FFFF);
	public final @Nullable Theme theme;
	TooltipTheme(int back, int top, int bot) {
		theme = new Theme(back, top, bot);
	}
	@Contract(pure = true)
	TooltipTheme() {
		theme = null;
	}
	public record Theme(int back, int top, int bot) {
		public Theme(@NotNull Color back, @NotNull Color top, @NotNull Color bot) {
			this(back.getRGB(), top.getRGB(), bot.getRGB());
		}
		@Contract(value = " -> new", pure = true)
		public @NotNull Color backColor() {
			return new Color(back);
		}
		@Contract(value = " -> new", pure = true)
		public @NotNull Color topColor() {
			return new Color(top);
		}
		@Contract(value = " -> new", pure = true)
		public @NotNull Color botColor() {
			return new Color(bot);
		}
	}
}
