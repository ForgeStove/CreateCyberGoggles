package io.github.forgestove.config.client;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
public final class Translation {
	public static final Component ON_LABEL = Component.literal("✔");
	public static final Component OFF_LABEL = Component.literal("❌");
	public static final Component RESET_LABEL = Component.literal("↺");
	public static final Component RESET_TOOLTIP = Component.translatable("controls.reset");
	public static final Component UNDO_LABEL = Component.literal("↩");
	public static final Component UNDO_TOOLTIP = Component.translatable("config.ui.undo.tooltip");
	public static final Component KEYBINDS_LABEL = Component.translatable("config.ui.keybinds");
	public static final Component CANCEL_LABEL = Component.translatable("gui.cancel");
	public static final Component QUIT_UNSAVED_LABEL = Component.translatable("config.ui.quit.unsaved");
	public static final Component SAVE_LABEL = Component.translatable("selectWorld.edit.save");
	public static final Component CANNOT_SAVE_LABEL = Component.translatable("config.ui.cannot_save");
	public static final Component QUIT_CONFIRM_LABEL = Component.translatable("config.ui.quit.confirm");
	public static final Component QUIT_CONFIRM_TITLE = Component.translatable("config.ui.quit.confirm.title");
	public static final Component QUIT_CONFIRM_WARNING = Component.translatable("config.ui.quit.confirm.warning");
	public static final Component RESTART_REQUIRED_LABEL = Component.translatable("config.ui.restart_required");
	public static final Component RESTART_REQUIRED_TITLE = Component.translatable("config.ui.restart_required.title");
	public static final Component QUIT_GAME = Component.translatable("menu.quit");
	public static final Component IGNORE_RESTART_LABEL = Component.translatable("config.ui.ignore_restart");
	public static final Component COLOR_PICKER_LABEL = Component.literal("🎨");
	public static final Component COLOR_PICKER_TOOLTIP = Component.translatable("config.ui.color_picker.tooltip");
	public static final Component VALIDATOR_MIN = Component.translatable("config.ui.validator.min");
	public static final Component VALIDATOR_MAX = Component.translatable("config.ui.validator.max");
	public static final Component UNSUPPORTED_TYPE = Component.translatable("config.ui.unsupported_type");
	public static final Component MULTIPLE_ERRORS = Component.translatable("config.ui.validator.multiple_errors");
	public static final Component UNLOCK_LABEL = Component.literal("🔓");
	public static final Component UNLOCK_TOOLTIP = Component.translatable("config.ui.unlock.tooltip");
	public static final Component LOCKED_LABEL = Component.literal("🔒");
	public static final Component LOCKED_TOOLTIP = Component.translatable("config.ui.locked.tooltip");
	public static @Nullable String getString(String key) {
		return I18n.exists(key) ? I18n.get(key) : null;
	}
}
