package io.github.forgestove.create_cyber_goggles.config.gui;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import io.github.forgestove.create_cyber_goggles.config.gui.entry.*;
import io.github.forgestove.create_cyber_goggles.config.gui.entry.KeybindValueConfigEntry.KeybindState;
import io.github.forgestove.create_cyber_goggles.config.gui.util.*;
import io.github.forgestove.create_cyber_goggles.mixin.accessor.KeyMappingAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.*;
public final class ConfigEntryList extends ContainerObjectSelectionList<ConfigEntry> {
	private final ConfigCategoryTab<?> tab;
	private final SmoothScrool smoothScrool = new SmoothScrool(this::setScrollAmount, this::scrollAmount, this::maxScrollAmount);
	private final Highlight highlight = new Highlight(() -> children().indexOf(getHovered()), this::getRowTop);
	public ConfigEntryList(
		ConfigCategoryTab<?> tab,
		Minecraft minecraft,
		int width,
		int height,
		int headerHeight,
		int itemHeight,
		@NotNull Iterable<ConfigEntry> entries
	) {
		super(minecraft, width, height, headerHeight, itemHeight);
		this.tab = tab;
		entries.forEach(this::addEntry);
	}
	@Override
	public void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float delta) {
		smoothScrool.tick(delta);
		highlight.tick(gui, getX(), getY(), width, height, defaultEntryHeight, delta);
		super.renderWidget(gui, mouseX, mouseY, delta);
		var entry = getHovered();
		if (entry == null) return;
		var widgetTooltip = entry.getHoveredWidgetTooltip(mouseX, mouseY);
		if (widgetTooltip != null) {
			gui.setTooltipForNextFrame(minecraft.font, widgetTooltip.toCharSequence(minecraft), mouseX, mouseY);
			return;
		}
		if (entry.getTooltip() != null) {
			var tooltip = entry.getTooltip();
			gui.setTooltipForNextFrame(minecraft.font, tooltip, mouseX, mouseY);
		}
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
		smoothScrool.onMouseScroll(vertical, defaultEntryHeight);
		return true;
	}
	@Override
	public boolean mouseDragged(@NonNull MouseButtonEvent event, double dragX, double dragY) {
		var result = super.mouseDragged(event, dragX, dragY);
		// 同步拖拽后的滚动位置，防止 smoothScrool 在下帧覆盖掉
		smoothScrool.sync();
		return result;
	}
	@Override
	public int getRowWidth() {
		return width * 4 / 5;
	}
	public void refreshEntries() {
		var keyEntries = new ArrayList<KeybindValueConfigEntry<?>>();
		var localKeyCount = new HashMap<Key, Integer>();
		var localKeyEntries = new HashMap<Key, List<KeybindValueConfigEntry<?>>>();
		for (var entry : children()) {
			if (!(entry instanceof KeybindValueConfigEntry<?> keyEntry)) continue;
			keyEntries.add(keyEntry);
			var key = keyEntry.getBoundKey();
			if (key.equals(InputConstants.UNKNOWN)) continue;
			localKeyCount.merge(key, 1, Integer::sum);
			localKeyEntries.computeIfAbsent(key, k -> new ArrayList<>()).add(keyEntry);
		}
		var registeredKeyCount = new HashMap<Key, Integer>();
		var registeredKeyUsages = new HashMap<Key, List<Component>>();
		for (var mapping : tab.getMinecraft().options.keyMappings) {
			var key = ((KeyMappingAccessor) mapping).getKey();
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
				usages.add(localEntry.getDisplayTitle());
			}
			usages.addAll(registeredKeyUsages.getOrDefault(key, List.of()));
			keyEntry.setConflictUsages(usages);
		});
		children().forEach(ConfigEntry::refresh);
	}
	public boolean hasEntryError() {
		for (var configEntry : children()) if (configEntry.hasError()) return true;
		return false;
	}
	public boolean handleKeyCapture(int keyCode) {
		for (var entry : children())
			if (entry instanceof KeybindValueConfigEntry<?> keybindEntry && keybindEntry.handleCaptureKey(keyCode)) return true;
		return false;
	}
	public boolean handleMouseCapture(int button) {
		for (var entry : children())
			if (entry instanceof KeybindValueConfigEntry<?> keybindEntry && keybindEntry.handleCaptureMouse(button)) return true;
		return false;
	}
	public boolean isCapturingKeybind() {
		for (var entry : children())
			if (entry instanceof KeybindValueConfigEntry<?> keybindEntry && keybindEntry.isCapturing()) return true;
		return false;
	}
}
