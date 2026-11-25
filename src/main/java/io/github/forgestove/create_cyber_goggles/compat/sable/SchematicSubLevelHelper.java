package io.github.forgestove.create_cyber_goggles.compat.sable;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.render.BlockEntityRenderHelper;
import dev.ryanhcode.sable.neoforge.mixinterface.compatibility.create.schematics.*;
import dev.ryanhcode.sable.neoforge.mixinterface.compatibility.create.schematics.SchematicLevelExtension.SchematicSubLevel;
import dev.ryanhcode.sable.neoforge.mixinterface.compatibility.create.schematics.StructureTemplateExtension.SubLevelTemplate;
import net.createmod.catnip.levelWrappers.SchematicLevel;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import org.joml.*;

import java.lang.Math;
import java.util.*;
public class SchematicSubLevelHelper {
	public static void attachSubLevels(StructureTemplate template, SchematicLevel target, Level level) {
		List<SubLevelTemplate> subLevelTemplates = ((StructureTemplateExtension) template).sable$getSubLevels();
		if (subLevelTemplates.isEmpty()) return;
		List<SchematicSubLevel> targetSubLevels = getSchematicSubLevels(target);
		for (SubLevelTemplate subLevelTemplate : subLevelTemplates) {
			var subLevel = new SchematicLevel(level);
			subLevelTemplate.template()
				.placeInWorld(
					subLevel,
					BlockPos.ZERO,
					BlockPos.ZERO,
					new StructurePlaceSettings(),
					RandomSource.create(),
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
	public static boolean hasSubLevels(SchematicLevel level) {
		return !getSchematicSubLevels(level).isEmpty();
	}
	private static List<SchematicSubLevel> getSchematicSubLevels(SchematicLevel level) {
		return ((SchematicLevelExtension) level).sable$getSubLevels();
	}
	public static void renderBlockEntities(PoseStack ms, SuperRenderTypeBuffer buffers, SchematicLevel mainLevel, float partialTicks) {
		List<SchematicSubLevel> subLevels = getSchematicSubLevels(mainLevel);
		if (subLevels.isEmpty()) return;
		for (SchematicSubLevel subLevel : subLevels) {
			SchematicLevel level = subLevel.level();
			var blockEntities = new ArrayList<BlockEntity>();
			for (BlockEntity blockEntity : level.getRenderedBlockEntities()) blockEntities.add(blockEntity);
			if (blockEntities.isEmpty()) continue;
			var shouldRender = new BitSet(blockEntities.size());
			shouldRender.set(0, blockEntities.size());
			ms.pushPose();
			ms.translate(subLevel.position().x, subLevel.position().y, subLevel.position().z);
			ms.mulPose(new Quaternionf(subLevel.orientation()));
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
	@SuppressWarnings("resource")
	public static BoundingBox frameBounds(SchematicLevel level) {
		List<SchematicSubLevel> subLevels = getSchematicSubLevels(level);
		if (subLevels.isEmpty()) return level.getBounds();
		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
		if (!level.getBlockMap().isEmpty()) {
			BoundingBox main = level.getBounds();
			minX = main.minX();
			minY = main.minY();
			minZ = main.minZ();
			maxX = main.maxX();
			maxY = main.maxY();
			maxZ = main.maxZ();
		}
		for (SchematicSubLevel subLevel : subLevels) {
			BoundingBox sub = subLevel.level().getBounds();
			Matrix4f matrix = new Matrix4f().translation(
					(float) subLevel.position().x,
					(float) subLevel.position().y,
					(float) subLevel.position().z
				)
				.rotate(new Quaternionf(subLevel.orientation()));
			for (var x = 0; x < 2; x++)
				for (var y = 0; y < 2; y++)
					for (var z = 0; z < 2; z++) {
						Vector3f corner = matrix.transformPosition(new Vector3f(
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
