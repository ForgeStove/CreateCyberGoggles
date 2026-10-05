package io.github.forgestove.create_cyber_goggles.compat.sable;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.render.BlockEntityRenderHelper;
import dev.ryanhcode.sable.neoforge.mixinterface.compatibility.create.schematics.*;
import dev.ryanhcode.sable.neoforge.mixinterface.compatibility.create.schematics.SchematicLevelExtension.SchematicSubLevel;
import net.createmod.catnip.levelWrappers.SchematicLevel;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import org.joml.*;

import java.lang.Math;
import java.util.ArrayList;
import java.util.BitSet;
/**
 * sable 子维度（sublevel）的渲染支持。
 * <p>
 * 子维度的方块不在主模板中，蓝图将其单独存放在 {@code sub_levels} 标签下。sable 仅在世界蓝图虚影路径
 * （{@code SchematicHandler.setupRenderer}）把子维度展开到渲染层级；本模组的离屏渲染与 3D 预览各自新建
 * {@link SchematicLevel}，须按同样的规则展开，否则子维度空白。
 */
public final class SchematicSubLevelHelper {
	/**
	 * 把模板中的子维度展开成渲染层级并挂到主层级上。
	 * <p>
	 * {@code level} 须由调用方传入：{@code SchematicLevel#getLevel()} 是 {@code IServerWorld#getWorld}
	 * 的实现，客户端调用会抛出 {@code IllegalStateException}。
	 */
	public static void attachSubLevels(StructureTemplate template, SchematicLevel target, Level level) {
		var subLevelTemplates = ((StructureTemplateExtension) template).sable$getSubLevels();
		if (subLevelTemplates.isEmpty()) return;
		var targetSubLevels = ((SchematicLevelExtension) target).sable$getSubLevels();
		for (var subLevelTemplate : subLevelTemplates) {
			var subLevel = new SchematicLevel(level);
			subLevelTemplate.template()
				.placeInWorld(
					subLevel,
					BlockPos.ZERO,
					BlockPos.ZERO,
					new StructurePlaceSettings(),
					level.getRandom(),
					Block.UPDATE_CLIENTS
				);
			targetSubLevels.add(new SchematicSubLevel(
				subLevelTemplate.uuid(),
				subLevelTemplate.position(),
				subLevelTemplate.orientation(),
				subLevel
			));
		}
	}
	/** 主层级上是否挂了子维度 */
	public static boolean hasSubLevels(SchematicLevel level) {
		return !((SchematicLevelExtension) level).sable$getSubLevels().isEmpty();
	}
	/**
	 * 补画子维度的方块实体。
	 * <p>
	 * sable 的 {@code SchematicRendererMixin} 只 tesselate 子维度的方块模型，不处理方块实体；主层级那部分由
	 * Create 的 {@code SchematicRenderer} 按构造时取得的快照绘制，覆盖不到子维度。此处按各自位姿补画一遍，
	 * 同时覆盖 {@code RenderShape} 非 MODEL 的方块（箱子、告示牌等纯实体渲染的方块），它们不经过 sable 的模型分支。
	 * <p>
	 * 须在 {@code SchematicRenderer#render} 之后、{@code buffers.draw()} 之前调用，以保证位姿一致。
	 */
	public static void renderBlockEntities(
		PoseStack ms,
		SuperRenderTypeBuffer buffers,
		SchematicLevel mainLevel,
		float partialTicks
	) {
		var subLevels = ((SchematicLevelExtension) mainLevel).sable$getSubLevels();
		if (subLevels.isEmpty()) return;
		for (var subLevel : subLevels) {
			SchematicLevel level = subLevel.level();
			var blockEntities = new ArrayList<BlockEntity>();
			for (var blockEntity : level.getRenderedBlockEntities()) blockEntities.add(blockEntity);
			if (blockEntities.isEmpty()) continue;
			var shouldRender = new BitSet(blockEntities.size());
			shouldRender.set(0, blockEntities.size());
			ms.pushPose();
			ms.translate(subLevel.position().x, subLevel.position().y, subLevel.position().z);
			ms.mulPose(new Quaternionf(subLevel.orientation()));
			// realLevel 传子层级而非真实 ClientLevel（Create 同样传虚拟层级）：传真 ClientLevel 时
			// Flywheel 的 skipVanillaRender 会跳过带 visual 的方块实体，而蓝图中的方块实体没有 visual 可绘制
			BlockEntityRenderHelper.renderBlockEntities(
				blockEntities,
				shouldRender,
				new BitSet(),
				null,
				level,
				ms,
				null,
				buffers,
				partialTicks
			);
			ms.popPose();
		}
	}
	/**
	 * 主层级包围盒与各子层级包围盒（按各自位姿变换后）的并集，供取景使用。
	 * <p>
	 * 仅做读取：{@code getBounds()} 返回层级自身的包围盒对象，就地修改会污染后续的方块查找。
	 * 层级归渲染管线所有并跨帧复用，不可 close，故压制资源检查。
	 */
	@SuppressWarnings("resource")
	public static BoundingBox frameBounds(SchematicLevel level) {
		var subLevels = ((SchematicLevelExtension) level).sable$getSubLevels();
		if (subLevels.isEmpty()) return level.getBounds();
		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
		// 主层级可能为空（sable 飞船整体位于子维度中），其包围盒会退化为原点一格，计入取景会偏移构图
		if (!level.getBlockMap().isEmpty()) {
			var main = level.getBounds();
			minX = main.minX();
			minY = main.minY();
			minZ = main.minZ();
			maxX = main.maxX();
			maxY = main.maxY();
			maxZ = main.maxZ();
		}
		for (var subLevel : subLevels) {
			var sub = subLevel.level().getBounds();
			// 子层级方块使用自身坐标（自 0 起），摆放位置由 position 与 orientation 决定，故需变换 8 个角点
			var matrix = new Matrix4f().translation(
					(float) subLevel.position().x,
					(float) subLevel.position().y,
					(float) subLevel.position().z
				)
				.rotate(new Quaternionf(subLevel.orientation()));
			for (var x = 0; x < 2; x++)
				for (var y = 0; y < 2; y++)
					for (var z = 0; z < 2; z++) {
						var corner = matrix.transformPosition(new Vector3f(
							x == 0 ? sub.minX() : sub.maxX() + 1,
							y == 0 ? sub.minY() : sub.maxY() + 1,
							z == 0 ? sub.minZ() : sub.maxZ() + 1
						));
						minX = Math.min(minX, Mth.floor(corner.x));
						minY = Math.min(minY, Mth.floor(corner.y));
						minZ = Math.min(minZ, Mth.floor(corner.z));
						maxX = Math.max(maxX, Mth.ceil(corner.x) - 1);
						maxY = Math.max(maxY, Mth.ceil(corner.y) - 1);
						maxZ = Math.max(maxZ, Mth.ceil(corner.z) - 1);
					}
		}
		return BoundingBox.fromCorners(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ));
	}
}
