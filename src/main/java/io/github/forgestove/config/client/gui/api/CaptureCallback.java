package io.github.forgestove.config.client.gui.api;
@FunctionalInterface
public interface CaptureCallback {
	void onCaptureStateChanged(CaptureHandler entry, boolean capturing);
}
