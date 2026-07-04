package io.github.forgestove.create_cyber_goggles.config.client.gui.entry;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.*;
import io.github.forgestove.create_cyber_goggles.config.client.ClientUtil;
import io.github.forgestove.create_cyber_goggles.config.client.gui.*;
import io.github.forgestove.create_cyber_goggles.config.client.gui.api.*;
import io.github.forgestove.create_cyber_goggles.config.tree.ValueConfigNode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.*;
public final class KeybindValueConfigEntry<C> extends ValueConfigEntry<C, Key> implements TabLifecycle, CrossRefreshable, CaptureHandler {
	private final Button bindButton;
	private boolean capturing;
	private CaptureCallback captureCallback = (e, c) -> {};
	private KeybindState state = KeybindState.BOUND;
	private List<Component> conflictUsages = List.of();
	public KeybindValueConfigEntry(ConfigCategoryTab<C, Key> tab, ValueConfigNode<C, Key> valueNode) {
		super(tab, valueNode);
		bindButton = Button.builder(
			Component.empty(), b -> {
				capturing = true;
				captureCallback.onCaptureStateChanged(this, true);
				this.tab.screen.refresh();
			}
		).size(WIDTH, HEIGHT).build();
		children.add(bindButton);
		refresh();
	}
	@Override
	public void refresh() {
		super.refresh();
		var keyText = getValue().getDisplayName().copy().withStyle(state.color());
		if (capturing) bindButton.setMessage(Component.literal("> ").append(keyText).append(" <"));
		else bindButton.setMessage(keyText);
		if (state == KeybindState.CONFLICT && !conflictUsages.isEmpty()) bindButton.setTooltip(Tooltip.create(getConflictTooltip()));
		else bindButton.setTooltip(null);
	}
	private Component getConflictTooltip() {
		var usedBy = Component.empty();
		for (var i = 0; i < conflictUsages.size(); i++) {
			if (i > 0) usedBy = usedBy.append(Component.literal(", "));
			usedBy = usedBy.append(conflictUsages.get(i));
		}
		return Component.translatable("controls.keybinds.duplicateKeybinds", usedBy);
	}
	public boolean handleCaptureKey(int keyCode) {
		if (!capturing) return false;
		if (keyCode == InputConstants.KEY_ESCAPE) setValue(InputConstants.UNKNOWN);
		else setValue(Type.KEYSYM.getOrCreate(keyCode));
		capturing = false;
		captureCallback.onCaptureStateChanged(this, false);
		tab.screen.refresh();
		return true;
	}
	public boolean handleCaptureMouse(int button) {
		if (!capturing) return false;
		setValue(Type.MOUSE.getOrCreate(button));
		capturing = false;
		captureCallback.onCaptureStateChanged(this, false);
		tab.screen.refresh();
		return true;
	}
	@Override
	public void render(
		@NotNull GuiGraphics gui,
		int index,
		int y,
		int x,
		int width,
		int height,
		int mouseX,
		int mouseY,
		boolean hovering,
		float delta
	) {
		renderGui(gui, y, x, width, mouseX, mouseY, delta, undoButton, resetButton, bindButton);
	}
	@Override
	public boolean beginCrossEntryRefresh(List<ConfigEntry> siblings) {
		var keyEntries = new ArrayList<KeybindValueConfigEntry<?>>();
		var localKeyCount = new HashMap<Key, Integer>();
		var localKeyEntries = new HashMap<Key, List<KeybindValueConfigEntry<?>>>();
		for (var entry : siblings) {
			if (!(entry instanceof KeybindValueConfigEntry<?> keyEntry)) continue;
			keyEntries.add(keyEntry);
			var key = keyEntry.getBoundKey();
			if (key.equals(InputConstants.UNKNOWN)) continue;
			localKeyCount.merge(key, 1, Integer::sum);
			localKeyEntries.computeIfAbsent(key, k -> new ArrayList<>()).add(keyEntry);
		}
		if (keyEntries.isEmpty()) return false;
		var registeredKeyCount = new HashMap<Key, Integer>();
		var registeredKeyUsages = new HashMap<Key, List<Component>>();
		for (var mapping : ClientUtil.mc.options.keyMappings) {
			var key = mapping.getKey();
			if (key.equals(InputConstants.UNKNOWN)) continue;
			registeredKeyCount.merge(key, 1, Integer::sum);
			registeredKeyUsages.computeIfAbsent(key, k -> new ArrayList<>()).add(Component.translatable(mapping.getName()));
		}
		keyEntries.forEach(keyEntry -> {
			var key = keyEntry.getBoundKey();
			if (key.equals(InputConstants.UNKNOWN)) {
				keyEntry.setKeybindState(KeybindState.UNBOUND);
				keyEntry.setConflictUsages(List.of());
				return;
			}
			var hasConflict = localKeyCount.getOrDefault(key, 0) > 1 || registeredKeyCount.getOrDefault(key, 0) > 1;
			keyEntry.setKeybindState(hasConflict ? KeybindState.CONFLICT : KeybindState.BOUND);
			if (!hasConflict) {
				keyEntry.setConflictUsages(List.of());
				return;
			}
			var usages = new ArrayList<Component>();
			for (var localEntry : localKeyEntries.getOrDefault(key, List.of())) {
				if (localEntry == keyEntry) continue;
				usages.add(localEntry.valueNode.getTitle());
			}
			usages.addAll(registeredKeyUsages.getOrDefault(key, List.of()));
			keyEntry.setConflictUsages(usages);
		});
		return true;
	}
	public Key getBoundKey() {
		return getValue();
	}
	public void setKeybindState(KeybindState state) {
		this.state = state;
	}
	public void setConflictUsages(List<Component> conflictUsages) {
		this.conflictUsages = conflictUsages;
	}
	@Override
	public void onAttachedToTab(ConfigCategoryTab<?, ?> tab) {
		setCaptureCallback(tab.screen::onEntryCaptureChanged);
	}
	public void setCaptureCallback(CaptureCallback callback) {
		captureCallback = callback;
	}
	public enum KeybindState {
		UNBOUND(ChatFormatting.DARK_GRAY),
		CONFLICT(ChatFormatting.YELLOW),
		BOUND(ChatFormatting.GREEN);
		private final ChatFormatting color;
		KeybindState(ChatFormatting color) {
			this.color = color;
		}
		public ChatFormatting color() {
			return color;
		}
	}
	@FunctionalInterface
	public interface CaptureCallback {
		void onCaptureStateChanged(CaptureHandler entry, boolean capturing);
	}
}
