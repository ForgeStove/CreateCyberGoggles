package io.github.forgestove.create_cyber_goggles.config.gui;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;
import io.github.forgestove.create_cyber_goggles.config.Translation;
import io.github.forgestove.create_cyber_goggles.config.gui.entry.*;
import io.github.forgestove.create_cyber_goggles.config.gui.entry.KeybindValueConfigEntry.KeybindState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.*;

import java.util.*;
public final class ConfigEntryList extends ContainerObjectSelectionList<ConfigEntry> {
	private final ConfigCategoryTab<?> tab;
	// 高亮动画状态
	private float highlightY;
	private float highlightTargetY;
	private float highlightAlpha;
	@Nullable private ConfigEntry lastHoveredEntry;
	public ConfigEntryList(
		ConfigCategoryTab<?> tab,
		Minecraft minecraft,
		int width,
		int contentHeight,
		int headerHeight,
		int itemSpacing,
		@NotNull Iterable<ConfigEntry> entries
	) {
		super(minecraft, width, contentHeight, headerHeight, itemSpacing);
		this.tab = tab;
		entries.forEach(this::addEntry);
	}
	@Override
	public void renderWidget(@NotNull GuiGraphics gui, int mouseX, int mouseY, float delta) {
		renderHighlight(gui, delta);
		super.renderWidget(gui, mouseX, mouseY, delta);
		var entry = getHovered();
		if (entry == null) return;
		if (entry instanceof ValueConfigEntry<?, ?> valueEntry) if (valueEntry.resetButton.isHovered()) {
			tab.getScreen().setTooltipForNextRenderPass(Translation.RESET_TOOLTIP);
			return;
		} else if (valueEntry.undoButton.isHovered()) {
			tab.getScreen().setTooltipForNextRenderPass(Translation.UNDO_TOOLTIP);
			return;
		} else if (valueEntry instanceof ColorValueConfigEntry<?> colorEntry && colorEntry.pickerButton.isHovered()) {
			tab.getScreen().setTooltipForNextRenderPass(Translation.COLOR_PICKER_TOOLTIP);
			return;
		}
		if (entry.getTooltip() != null) tab.getScreen().setTooltipForNextRenderPass(entry.getTooltip());
	}
	private void renderHighlight(@NotNull GuiGraphics gui, float delta) {
		var alpha = (int) (highlightAlpha * 48); // Max alpha 48 (0x30)
		var color = alpha << 24 | 0xFFFFFF;
		var left = getX();
		var right = getX() + getWidth();
		var offset = -1; // 向上移动高亮
		var top = (int) highlightY + offset;
		var bottom = top + itemHeight;
		// 剪辑到可见区域
		var visibleTop = getY();
		var visibleBottom = getY() + getHeight();
		if (top < visibleTop) top = visibleTop;
		if (bottom > visibleBottom) bottom = visibleBottom;
		if (top < bottom) gui.fill(left, top, right, bottom, color);
		var v = 0.5f;
		var hoveredEntry = getHovered();
		if (hoveredEntry != null) {
			var index = children().indexOf(hoveredEntry);
			if (index >= 0) {
				var entryTop = getRowTop(index);
				highlightTargetY = entryTop;
				// 淡入
				highlightAlpha = Mth.lerp(v * delta, highlightAlpha, 0.95F);
				// 如果第一次悬停，初始化位置
				if (highlightY < 0 || lastHoveredEntry == null) highlightY = entryTop;
			}
			lastHoveredEntry = hoveredEntry;
		} else highlightAlpha = Mth.lerp(v * delta, highlightAlpha, 0.0f); // 淡出
		// 带吸附到目标的平滑位置转换
		if (!(highlightTargetY >= 0) || !(highlightY >= 0)) return;
		highlightY = Mth.lerp(v * delta * 2, highlightY, highlightTargetY);
		// 靠近时再快速锁定目标
		if (Math.abs(highlightY - highlightTargetY) < 1.0f) highlightY = highlightTargetY;
	}
	@Override
	public int getRowWidth() {
		return width - 80;
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
			var key = mapping.getKey();
			if (key.equals(InputConstants.UNKNOWN)) continue;
			registeredKeyCount.merge(key, 1, Integer::sum);
			registeredKeyUsages.computeIfAbsent(key, k -> new ArrayList<>()).add(Component.translatable(mapping.getName()));
		}
		for (var keyEntry : keyEntries) {
			var key = keyEntry.getBoundKey();
			if (key.equals(InputConstants.UNKNOWN)) {
				keyEntry.setKeybindState(KeybindState.UNBOUND);
				keyEntry.setConflictUsages(List.of());
				continue;
			}
			var hasConflict = localKeyCount.getOrDefault(key, 0) > 1 || registeredKeyCount.getOrDefault(key, 0) > 1;
			keyEntry.setKeybindState(hasConflict ? KeybindState.CONFLICT : KeybindState.BOUND);
			if (!hasConflict) {
				keyEntry.setConflictUsages(List.of());
				continue;
			}
			var usages = new ArrayList<Component>();
			for (var localEntry : localKeyEntries.getOrDefault(key, List.of())) {
				if (localEntry == keyEntry) continue;
				usages.add(localEntry.getDisplayTitle());
			}
			usages.addAll(registeredKeyUsages.getOrDefault(key, List.of()));
			keyEntry.setConflictUsages(usages);
		}
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
