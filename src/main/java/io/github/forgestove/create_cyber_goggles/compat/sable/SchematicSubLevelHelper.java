package io.github.forgestove.create_cyber_goggles.compat.sable;
import dev.ryanhcode.sable.neoforge.mixinterface.compatibility.create.schematics.*;
import dev.ryanhcode.sable.neoforge.mixinterface.compatibility.create.schematics.SchematicLevelExtension.SchematicSubLevel;
import net.createmod.catnip.levelWrappers.SchematicLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import org.joml.*;

import java.lang.Math;
/**
 * sable 子维度（sublevel）的渲染支持。
 * <p>
 * 子维度的方块不在主模板里 —— 蓝图把它单独存在 {@code sub_levels} 标签下，由 sable 的
 * {@code StructureTemplateMixin} 读进 {@code StructureTemplateExtension}。sable 自己只把它展开到
 * 「世界里的蓝图虚影」那一条路径（{@code SchematicHandler.setupRenderer}）；本模组的离屏渲染与 3D 预览
 * 各建自己的 {@link SchematicLevel}，不走那条路径，所以必须自己补一次，否则渲染出来的图里子维度是空的。
 * <p>
 * 展开规则与 {@code SchematicHandlerMixin} 保持一致，这样 sable 挂在 {@code SchematicRenderer#drawLayer}
 * 上的 mixin 才会把这些层级一并画出来。
 */
public final class SchematicSubLevelHelper {
	/**
	 * 把模板里的子维度展开成渲染层级并挂到主层级上。
	 * <p>
	 * {@code level} 必须由调用方传入：{@code SchematicLevel#getLevel()} 是 {@code IServerWorld#getWorld} 的实现，
	 * 客户端调用直接抛 {@code IllegalStateException}，拿不到被包裹的原 Level。
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
	 * 主层级包围盒与各子层级包围盒（按各自位姿变换后）的并集，供取景用。
	 * <p>
	 * 只做纯计算：{@code getBounds()} 返回的是层级自己的包围盒对象，不能就地改（会污染后续的方块查找）。
	 * <p>
	 * 这些 {@link SchematicLevel} 归渲染管线所有、要跨帧复用，绝不能在这里 close，故压掉资源检查。
	 */
	@SuppressWarnings("resource")
	public static BoundingBox frameBounds(SchematicLevel level) {
		var subLevels = ((SchematicLevelExtension) level).sable$getSubLevels();
		if (subLevels.isEmpty()) return level.getBounds();
		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
		// 主层级可能是空的（sable 的飞船整艘都在子维度里），此时它的包围盒退化成原点一个格，算进取景会把图撑偏
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
			// 子层级的方块用自身坐标（从 0 起），摆放位置由 position + orientation 决定，故须变换 8 个角点
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
