package io.github.forgestove.config.client.gui.factory;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
public record EntryKeybind(@NotNull Key key, int modifierFlags) {
	public static final EntryKeybind UNBOUND = new EntryKeybind(InputConstants.UNKNOWN, 0);
	public static final int NONE = 0;
	public static final int SHIFT = 1;
	public static final int CONTROL = 1 << 1;
	public static final int ALT = 1 << 2;
	/** 获取当前按下的修饰键标志（可多项）。 */
	public static int getActiveModifierFlags() {
		var flags = NONE;
		if (Screen.hasShiftDown()) flags |= SHIFT;
		if (Screen.hasControlDown()) flags |= CONTROL;
		if (Screen.hasAltDown()) flags |= ALT;
		return flags;
	}
	/** 从 {@link #serialize()} 的字符串反序列化。 */
	public static EntryKeybind deserialize(String str) {
		if (str == null || str.isEmpty()) return UNBOUND;
		var parts = str.split("\\+");
		var codeStr = parts[parts.length - 1];
		var flags = NONE;
		for (var i = 0; i < parts.length - 1; i++) {
			var m = parts[i].toLowerCase();
			flags |= switch (m) {
				case "shift", "s" -> SHIFT;
				case "control", "ctrl", "c" -> CONTROL;
				case "alt", "a" -> ALT;
				default -> NONE;
			};
		}
		try {
			var type = Type.KEYSYM;
			if (codeStr.startsWith("mouse:")) {
				type = Type.MOUSE;
				codeStr = codeStr.substring(6);
			} else if (codeStr.startsWith("scan:")) {
				type = Type.SCANCODE;
				codeStr = codeStr.substring(5);
			}
			return new EntryKeybind(type.getOrCreate(Integer.parseInt(codeStr)), flags);
		} catch (NumberFormatException e) {
			return UNBOUND;
		}
	}
	/** 检测原始按键是否匹配此快捷键（含修饰键检测）。 */
	public static boolean isKeyPressed(long window, EntryKeybind keybind) {
		var key = keybind.key();
		if (key.equals(InputConstants.UNKNOWN)) return false;
		var keyDown = switch (key.getType()) {
			case KEYSYM, SCANCODE -> InputConstants.isKeyDown(window, key.getValue());
			case MOUSE -> GLFW.glfwGetMouseButton(window, key.getValue()) == GLFW.GLFW_PRESS;
		};
		if (!keyDown) return false;
		if (keybind.modifierFlags() == NONE) return !isAnyModifierDown(window);
		if ((keybind.modifierFlags() & SHIFT) != 0 && !Screen.hasShiftDown()) return false;
		if ((keybind.modifierFlags() & CONTROL) != 0 && !Screen.hasControlDown()) return false;
		return (keybind.modifierFlags() & ALT) == 0 || Screen.hasAltDown();
	}
	private static boolean isAnyModifierDown(long window) {
		return InputConstants.isKeyDown(window, 340)
			|| InputConstants.isKeyDown(window, 344)
			|| InputConstants.isKeyDown(window, 341)
			|| InputConstants.isKeyDown(window, 345)
			|| InputConstants.isKeyDown(window, 342)
			|| InputConstants.isKeyDown(window, 346);
	}
	public Component getDisplayName() {
		return buildText().withStyle(getState().color());
	}
	private MutableComponent buildText() {
		var name = key.getDisplayName().copy();
		if (modifierFlags != NONE) {
			var prefix = Component.literal("");
			if ((modifierFlags & CONTROL) != 0) prefix = prefix.append(Component.literal("Ctrl+"));
			if ((modifierFlags & SHIFT) != 0) prefix = prefix.append(Component.literal("Shift+"));
			if ((modifierFlags & ALT) != 0) prefix = prefix.append(Component.literal("Alt+"));
			return prefix.append(name);
		}
		return name;
	}
	private KeybindState getState() {
		return isUnbound() ? KeybindState.UNBOUND : KeybindState.BOUND;
	}
	public boolean isUnbound() {
		return key.equals(InputConstants.UNKNOWN);
	}
	/**
	 * 序列化为字符串格式："[modifiers+]code"，
	 * modifier 用单字母：c=Ctrl, s=Shift, a=Alt，
	 * 例如 "c+32"（Ctrl+Space），"c+s+83"（Ctrl+Shift+S）。
	 */
	public String serialize() {
		if (isUnbound()) return "";
		var sb = new StringBuilder();
		if ((modifierFlags & CONTROL) != 0) sb.append("c+");
		if ((modifierFlags & SHIFT) != 0) sb.append("s+");
		if ((modifierFlags & ALT) != 0) sb.append("a+");
		var typePrefix = switch (key.getType()) {
			case MOUSE -> "mouse:";
			case SCANCODE -> "scan:";
			default -> "";
		};
		sb.append(typePrefix).append(key.getValue());
		return sb.toString();
	}
	public boolean matches(int keyCode, int eventModifiers) {
		if (isUnbound()) return false;
		if (isModifier(keyCode)) return false;
		if (!key.equals(Type.KEYSYM.getOrCreate(keyCode))) return false;
		return modifierFlags == getEventModifierFlags(eventModifiers);
	}
	public static boolean isModifier(int keyCode) {
		return keyCode == InputConstants.KEY_LSHIFT
			|| keyCode == InputConstants.KEY_RSHIFT
			|| keyCode == InputConstants.KEY_LCONTROL
			|| keyCode == InputConstants.KEY_RCONTROL
			|| keyCode == InputConstants.KEY_LALT
			|| keyCode == InputConstants.KEY_RALT;
	}
	private static int getEventModifierFlags(int eventModifiers) {
		var flags = NONE;
		if ((eventModifiers & GLFW.GLFW_MOD_SHIFT) != 0) flags |= SHIFT;
		if ((eventModifiers & GLFW.GLFW_MOD_CONTROL) != 0) flags |= CONTROL;
		if ((eventModifiers & GLFW.GLFW_MOD_ALT) != 0) flags |= ALT;
		return flags;
	}
}
