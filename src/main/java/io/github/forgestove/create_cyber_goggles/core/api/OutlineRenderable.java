package io.github.forgestove.create_cyber_goggles.core.api;
import io.github.forgestove.create_cyber_goggles.CCG;
public interface OutlineRenderable {
	void ccg$render();
	default int ccg$getRenderDelay() {
		return CCG.config.outliner.delayRenderDuration;
	}
}
