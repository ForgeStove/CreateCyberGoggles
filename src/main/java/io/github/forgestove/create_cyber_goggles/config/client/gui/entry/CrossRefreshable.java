package io.github.forgestove.create_cyber_goggles.config.client.gui.entry;
import java.util.List;
/**
 * 需要知道同级别其他 entry 状态才能刷新的入口实现此接口。
 * 当列表刷新时，会依次调用所有 entry 的此方法，
 * 直到某个 entry 返回 true 表示已处理。
 * 典型用例：键位冲突检测。
 */
public interface CrossRefreshable {
	boolean beginCrossEntryRefresh(List<ConfigEntry> siblings);
}
