package io.github.forgestove.create_cyber_goggles.core.schematic;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jetbrains.annotations.NotNull;
import org.joml.*;
/**
 * 把 {@link VertexConsumer} 的顶点按给定姿态变换 —— 流体渲染走
 * {@code BlockRenderDispatcher#renderLiquid}，它不接受 PoseStack，只能在这里补上变换。
 */
final class PoseAppliedVertexConsumer implements VertexConsumer {
	private final Matrix4f pose = new Matrix4f();
	private final Matrix3f normal = new Matrix3f();
	private final Vector3f scratch = new Vector3f();
	private VertexConsumer delegate;
	private float offX, offY, offZ;
	void prepare(VertexConsumer delegate, Matrix4f pose, Matrix3f normal, float offX, float offY, float offZ) {
		this.delegate = delegate;
		this.pose.set(pose);
		this.normal.set(normal);
		this.offX = offX;
		this.offY = offY;
		this.offZ = offZ;
	}
	@Override
	public @NotNull VertexConsumer addVertex(float x, float y, float z) {
		pose.transformPosition(x + offX, y + offY, z + offZ, scratch);
		delegate.addVertex(scratch.x(), scratch.y(), scratch.z());
		return this;
	}
	@Override
	public @NotNull VertexConsumer setNormal(float x, float y, float z) {
		normal.transform(x, y, z, scratch);
		delegate.setNormal(scratch.x(), scratch.y(), scratch.z());
		return this;
	}
	@Override
	public @NotNull VertexConsumer setColor(int red, int green, int blue, int alpha) {
		delegate.setColor(red, green, blue, alpha);
		return this;
	}
	@Override
	public @NotNull VertexConsumer setUv(float u, float v) {
		delegate.setUv(u, v);
		return this;
	}
	@Override
	public @NotNull VertexConsumer setUv1(int u, int v) {
		delegate.setUv1(u, v);
		return this;
	}
	@Override
	public @NotNull VertexConsumer setUv2(int u, int v) {
		delegate.setUv2(u, v);
		return this;
	}
}
