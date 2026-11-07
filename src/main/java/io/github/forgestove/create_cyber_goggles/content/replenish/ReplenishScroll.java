package io.github.forgestove.create_cyber_goggles.content.replenish;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.animation.LerpedFloat.Chaser;
import net.minecraft.util.Mth;
class ReplenishScroll {
	private final LerpedFloat anim = LerpedFloat.linear().startWithValue(0);
	private int value;
	private boolean dragging;
	private double dragOffset;
	int value() {
		return value;
	}
	int chaseTarget() {
		return (int) anim.getChaseTarget();
	}
	float display(float partialTick) {
		return anim.getValue(partialTick);
	}
	boolean dragging() {
		return dragging;
	}
	double dragOffset() {
		return dragOffset;
	}
	void startDrag(double offset) {
		dragging = true;
		dragOffset = offset;
	}
	void stopDrag() {
		dragging = false;
	}
	void moveTo(int target) {
		value = target;
		anim.setValue(target);
		anim.updateChaseTarget(target);
	}
	void chaseTo(int target) {
		value = target;
		anim.chase(target, 0.5, Chaser.EXP);
	}
	void tick() {
		anim.tickChaser();
		if (Math.abs(anim.getValue() - anim.getChaseTarget()) < 1 / 16f) anim.setValue(anim.getChaseTarget());
	}
	int clamped(float partialTick, int max) {
		return Mth.clamp((int) anim.getValue(partialTick), 0, max);
	}
}
