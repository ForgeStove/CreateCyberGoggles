package io.github.forgestove.flexconfig.client.gui.entry;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.*;
import io.github.forgestove.flexconfig.client.ClientUtil;
import io.github.forgestove.flexconfig.client.gui.ConfigCategoryTab;
import io.github.forgestove.flexconfig.client.gui.api.*;
import io.github.forgestove.flexconfig.client.gui.factory.*;
import io.github.forgestove.flexconfig.tree.ValueConfigNode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.*;

import java.util.*;
/**
 * 支持快捷键绑定的值配置条目基类。
 * 提供快捷键按钮、按键捕获、快捷键触发注册等公共逻辑。
 */
public abstract class CapturableValueConfigEntry<C, V> extends ValueConfigEntry<C, V>
	implements CaptureHandler, TabLifecycle, CrossRefreshable {
	protected final Button keybindButton;
	/** 进入屏幕时的 keybind 快照（用于 undo）。 */
	@Nullable private final String initialKeybindSerialized;
	protected boolean capturing;
	private CaptureCallback captureCallback = (e, c) -> {};
	private KeybindState state = KeybindState.UNBOUND;
	private List<Component> conflictUsages = List.of();
	public CapturableValueConfigEntry(ConfigCategoryTab<C, V> tab, ValueConfigNode<C, V> node) {
		super(tab, node);
		initialKeybindSerialized = tab.screen.getHandler().getTriggerKeybind(node.getPath());
		var keybind = initialKeybindSerialized != null ? EntryKeybind.deserialize(initialKeybindSerialized) : EntryKeybind.UNBOUND;
		keybindButton = Button.builder(
			keybind.getDisplayName(), b -> {
				capturing = true;
				captureCallback.onCaptureStateChanged(this, true);
				tab.screen.refresh();
			}
		).size(WIDTH / 2, HEIGHT).build();
		children.add(keybindButton);
	}
	/** 子类实现：注册全局快捷键触发动作（值变更逻辑 + 提示消息）。 */
	protected abstract void registerKeybindTask();
	/** 将值变更动作注册到 handler 的全局快捷键系统。由子类的 {@link #registerKeybindTask()} 调用。 */
	protected final void registerTriggerAction(Runnable valueChanger) {
		var handler = tab.screen.getHandler();
		var path = node.getPath();
		var serialized = handler.getTriggerKeybind(path);
		handler.setTriggerKeybind(
			path, serialized, () -> {
				if (isLocked()) return;
				valueChanger.run();
				node.writeEditingToConfig(tab.config);
				tab.screen.saveByKeybind();
			}
		);
	}
	@Override
	public void refresh() {
		super.refresh();
		var keybind = getCurrentKeybind();
		var keyText = keybind.getDisplayName().copy().withStyle(state.color());
		if (capturing) keybindButton.setMessage(Component.literal("> ").append(keyText).append(" <"));
		else keybindButton.setMessage(keyText);
		if (state == KeybindState.CONFLICT && !conflictUsages.isEmpty()) keybindButton.setTooltip(Tooltip.create(getConflictTooltip()));
		else keybindButton.setTooltip(null);
		// 撤销：keybind 与初始快照不同
		var currentSerialized = getCurrentKeybindSerialized();
		if (!Objects.equals(currentSerialized, initialKeybindSerialized)) {
			hasChanged = true;
			undoButton.active = true;
		}
		// 重置：keybind 不为默认（null）
		if (currentSerialized != null) resetButton.active = true;
	}
	/** 获取当前绑定快捷键。 */
	private EntryKeybind getCurrentKeybind() {
		var serialized = tab.screen.getHandler().getTriggerKeybind(node.getPath());
		return serialized != null ? EntryKeybind.deserialize(serialized) : EntryKeybind.UNBOUND;
	}
	private Component getConflictTooltip() {
		var usedBy = Component.empty();
		for (var i = 0; i < conflictUsages.size(); i++) {
			if (i > 0) usedBy = usedBy.append(Component.literal(", "));
			usedBy = usedBy.append(conflictUsages.get(i));
		}
		return Component.translatable("controls.keybinds.duplicateKeybinds", usedBy);
	}
	private @Nullable String getCurrentKeybindSerialized() {
		return tab.screen.getHandler().getTriggerKeybind(node.getPath());
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
		var valueWidget = getValueWidget();
		renderGui(gui, y, x, width, mouseX, mouseY, delta, lockButton, undoButton, resetButton, keybindButton, valueWidget);
		valueWidget.active = !isLocked();
		keybindButton.active = !isLocked();
	}
	/** 子类实现：返回值控件（valueButton、dropdownButton 等），用于 render。 */
	protected abstract AbstractWidget getValueWidget();
	@Override
	public boolean handleCaptureKey(int keyCode) {
		if (!capturing) return false;
		if (EntryKeybind.isModifier(keyCode)) return true;
		var handler = tab.screen.getHandler();
		if (keyCode == InputConstants.KEY_ESCAPE) handler.setTriggerKeybind(node.getPath(), null, null);
		else handler.setTriggerKeybind(
			node.getPath(),
			new EntryKeybind(Type.KEYSYM.getOrCreate(keyCode), EntryKeybind.getActiveModifierFlags()).serialize(),
			null
		);
		capturing = false;
		captureCallback.onCaptureStateChanged(this, false);
		tab.screen.refresh();
		return true;
	}
	@Override
	public boolean handleCaptureMouse(int button) {
		if (!capturing) return false;
		tab.screen.getHandler()
			.setTriggerKeybind(
				node.getPath(),
				new EntryKeybind(Type.MOUSE.getOrCreate(button), EntryKeybind.getActiveModifierFlags()).serialize(),
				null
			);
		capturing = false;
		captureCallback.onCaptureStateChanged(this, false);
		tab.screen.refresh();
		return true;
	}
	@Override
	public boolean beginCrossEntryRefresh(List<ConfigEntry> siblings) {
		var keyEntries = new ArrayList<CapturableValueConfigEntry<?, ?>>();
		var localKeyCount = new HashMap<EntryKeybind, Integer>();
		var localKeyEntries = new HashMap<EntryKeybind, List<CapturableValueConfigEntry<?, ?>>>();
		for (var entry : siblings) {
			if (!(entry instanceof CapturableValueConfigEntry<?, ?> capturable)) continue;
			keyEntries.add(capturable);
			var keybind = capturable.getCurrentKeybind();
			if (keybind.isUnbound()) continue;
			localKeyCount.merge(keybind, 1, Integer::sum);
			localKeyEntries.computeIfAbsent(keybind, k -> new ArrayList<>()).add(capturable);
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
		keyEntries.forEach(ke -> {
			var keybind = ke.getCurrentKeybind();
			if (keybind.isUnbound()) {
				ke.state = KeybindState.UNBOUND;
				ke.conflictUsages = List.of();
				return;
			}
			var hasConflict = localKeyCount.getOrDefault(keybind, 0) > 1 || registeredKeyCount.getOrDefault(keybind.key(), 0) > 1;
			ke.state = hasConflict ? KeybindState.CONFLICT : KeybindState.BOUND;
			if (!hasConflict) {
				ke.conflictUsages = List.of();
				return;
			}
			var usages = new ArrayList<Component>();
			for (var localEntry : localKeyEntries.getOrDefault(keybind, List.of())) {
				if (localEntry == ke) continue;
				usages.add(localEntry.node.getTitle());
			}
			usages.addAll(registeredKeyUsages.getOrDefault(keybind.key(), List.of()));
			ke.conflictUsages = usages;
		});
		return true;
	}
	@Override
	public void onAttachedToTab(ConfigCategoryTab<?, ?> tab) {
		captureCallback = tab.screen::onEntryCaptureChanged;
	}
	@Override
	public void resetToDefault() {
		tab.screen.getHandler().setTriggerKeybind(node.getPath(), null, null);
		super.resetToDefault();
	}
	@Override
	public void resetToActive() {
		tab.screen.getHandler().setTriggerKeybind(node.getPath(), initialKeybindSerialized, null);
		super.resetToActive();
	}
}
