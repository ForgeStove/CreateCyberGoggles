package io.github.forgestove.flexconfig.client.gui.api;
@FunctionalInterface
public interface CaptureCallback {
	void onCaptureStateChanged(CaptureHandler entry, boolean capturing);
}
