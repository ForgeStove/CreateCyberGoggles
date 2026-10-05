package io.github.forgestove.create_cyber_goggles.core.schematic;
import org.jetbrains.annotations.Nullable;
/**
 * 由蓝图桌界面的 mixin 实现，供滚轮事件取到预览面板。
 */
public interface SchematicPreviewAccess {
	@Nullable SchematicPreviewPanel ccg$getPreviewPanel();
}
