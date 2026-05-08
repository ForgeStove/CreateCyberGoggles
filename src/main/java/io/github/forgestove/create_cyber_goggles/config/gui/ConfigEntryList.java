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
	private static final float ANIMATION_SPEED = 0.4f;
	private final ConfigCategoryTab<?> tab;
	@Nullable private EnumValueConfigEntry<?> expandedDropdown;
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
		// 追踪下拉菜单被展开
		expandedDropdown = null;
		for (var entry : children()) {
			if (!(entry instanceof EnumValueConfigEntry<?> enumEntry) || !enumEntry.isExpanded()) continue;
			expandedDropdown = enumEntry;
			break;
		}
		// 渲染下拉覆盖在所有物体上（外部剪刀）
		var showTooltip = expandedDropdown == null;
		if (expandedDropdown != null) {
			expandedDropdown.renderDropdownOverlay(gui, mouseX, mouseY);
			// 鼠标悬停在下拉菜单上时，不要显示提示
			showTooltip = !expandedDropdown.isMouseOverDropdown(mouseX, mouseY);
		}
		if (showTooltip) renderHighlight(gui, delta);
		super.renderWidget(gui, mouseX, mouseY, delta);
		// 渲染提示
		if (!showTooltip) return;
		var entry = getHovered();
		if (entry == null) return;
		if (entry instanceof ValueConfigEntry<?, ?, ?> valueEntry) if (valueEntry.resetButton.isHovered()) {
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
		if (highlightAlpha <= 0.01f || highlightY < 0) return;
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
		var hoveredEntry = getHovered();
		if (hoveredEntry != null) {
			var index = children().indexOf(hoveredEntry);
			if (index >= 0) {
				var entryTop = getRowTop(index);
				highlightTargetY = entryTop;
				// 淡入
				highlightAlpha = Mth.lerp(ANIMATION_SPEED * delta, highlightAlpha, 0.95F);
				// 如果第一次悬停，初始化位置
				if (highlightY < 0 || lastHoveredEntry == null) highlightY = entryTop;
			}
			lastHoveredEntry = hoveredEntry;
		} else highlightAlpha = Mth.lerp(ANIMATION_SPEED * delta, highlightAlpha, 0.0f); // 淡出
		// 带吸附到目标的平滑位置转换
		if (!(highlightTargetY >= 0) || !(highlightY >= 0)) return;
		highlightY = Mth.lerp(ANIMATION_SPEED * delta * 2, highlightY, highlightTargetY);
		// 靠近时再快速锁定目标
		if (Math.abs(highlightY - highlightTargetY) < 1.0f) highlightY = highlightTargetY;
	}
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		// 首先检查展开下拉菜单是否能控制点击声
		if (expandedDropdown != null) {
			if (expandedDropdown.isMouseOverDropdown(mouseX, mouseY)) return expandedDropdown.handleDropdownClick(mouseX, mouseY);
			// 点击外部下拉菜单关闭它
			expandedDropdown.closeDropdown();
			expandedDropdown = null;
			// 如果我们刚关闭下拉菜单，请不要继续处理
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if (expandedDropdown != null) expandedDropdown.mouseReleased(mouseX, mouseY, button);
		return super.mouseReleased(mouseX, mouseY, button);
	}
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (expandedDropdown != null) if (expandedDropdown.mouseDragged(mouseX, mouseY, button, dragX, dragY)) return true;
		return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
		// 首先检查展开下拉菜单是否能支持滚动
		if (expandedDropdown != null && expandedDropdown.handleDropdownScroll(mouseX, mouseY, vertical)) return true;
		return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
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
