package io.github.forgestove.create_cyber_goggles.core.schematic;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.*;
import com.simibubi.create.content.schematics.SchematicItem;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.compat.sable.SchematicSubLevelHelper;
import io.github.forgestove.create_cyber_goggles.core.factory.CCGMods;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.levelWrappers.SchematicLevel;
import net.createmod.catnip.render.SuperRenderTypeBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.stream.Stream;
/**
 * 蓝图预览与渲染共用的工具集：蓝图装载、列表取索引、图片落盘、线程调度。
 * <p>
 * 由 Create: Blueprinted 与 Create: Schematic Preview（均为 MIT）的同名工具类合并而来。
 */
public final class SchematicImageUtil {
	private static final Set<String> IMAGE_EXTENSIONS = Set.of("png", "jpg", "jpeg", "gif", "bmp", "webp");
	/** 以蓝图文件名（可含子文件夹相对路径）装载结构模板 */
	public static StructureTemplate loadTemplateFromSchematicName(String schematicName) {
		var client = Minecraft.getInstance();
		var owner = Objects.requireNonNull(client.player).getGameProfile().getName();
		var blueprint = AllItems.SCHEMATIC.asStack();
		blueprint.set(AllDataComponents.SCHEMATIC_OWNER, owner);
		blueprint.set(AllDataComponents.SCHEMATIC_FILE, schematicName);
		return SchematicItem.loadSchematic(client.level, blueprint);
	}
	/**
	 * 把蓝图中的 sable 子维度展开成渲染层级挂到 {@code level} 上；未安装 sable 时不做任何处理。
	 * <p>
	 * 须用 lambda 而非方法引用：方法引用在连接 invokedynamic 时即解析 sable 的类，未安装该模组会抛 NoClassDefFoundError。
	 */
	public static void attachSubLevels(StructureTemplate template, SchematicLevel target, Level level) {
		CCGMods.sable.executeIfInstalled(() -> SchematicSubLevelHelper.attachSubLevels(template, target, level));
	}
	/** 主层级上是否挂有 sable 子维度；未安装 sable 恒为 false */
	public static boolean hasSubLevels(SchematicLevel level) {
		return CCGMods.sable.runIfInstalled(() -> SchematicSubLevelHelper.hasSubLevels(level)).orElse(false);
	}
	/**
	 * 补画 sable 子维度的方块实体；未安装 sable 时不做任何处理。
	 * <p>
	 * 须在 {@code SchematicRenderer#render} 之后、{@code buffers.draw()} 之前调用，以保证位姿与模型渲染一致。
	 */
	public static void renderSubLevelBlockEntities(PoseStack ms, SuperRenderTypeBuffer buffers, SchematicLevel level) {
		CCGMods.sable.executeIfInstalled(() -> SchematicSubLevelHelper.renderBlockEntities(
			ms,
			buffers,
			level,
			AnimationTickHolder.getPartialTicks()
		));
	}
	/** 取景包围盒：安装 sable 时将子维度一并计入（其位置可能超出主模板包围盒） */
	public static BoundingBox frameBounds(SchematicLevel level) {
		return CCGMods.sable.runIfInstalled(() -> SchematicSubLevelHelper.frameBounds(level)).orElseGet(level::getBounds);
	}
	/** 取蓝图列表第 index 项的文件名；越界返回空。每帧调用，故不记日志 */
	public static Optional<String> getSchematicNameFromIndex(int schematicIndex) {
		var availableSchematics = CreateClient.SCHEMATIC_SENDER.getAvailableSchematics();
		if (schematicIndex < 0 || schematicIndex >= availableSchematics.size()) return Optional.empty();
		return Optional.of(availableSchematics.get(schematicIndex).getString());
	}
	/** 当前可用的全部蓝图名，供命令补全 */
	public static Stream<String> getAllSchematicNames() {
		return CreateClient.SCHEMATIC_SENDER.getAvailableSchematics().stream().map(Component::getString);
	}
	/** 先写临时文件再改名，避免渲染中断留下半张图 */
	public static @NotNull File saveImage(File directory, String fileName, String extension, byte @NotNull [] imageByteArray) throws
		IOException,
		IllegalArgumentException {
		var fileNameAndExt = fileName + "." + extension;
		var errorPrefix = "Failed to write image: " + fileNameAndExt + ". ";
		if (imageByteArray.length == 0) throw new IllegalArgumentException(errorPrefix + "Image byte array cannot be empty.");
		if (fileName.isBlank()) throw new IllegalArgumentException(errorPrefix + "File name cannot be blank");
		if (!IMAGE_EXTENSIONS.contains(extension)) throw new IllegalArgumentException(errorPrefix + "Unrecognised image file extension");
		var outputFile = new File(directory, fileNameAndExt);
		var tempFile = new File(directory, fileName + ".tmp");
		try {
			Files.write(tempFile.toPath(), imageByteArray);
			if (!tempFile.renameTo(outputFile))
				throw new IOException("Failed to rename temporary image file: " + tempFile.getAbsolutePath());
			return outputFile;
		} catch (IOException e) {
			if (!tempFile.delete()) CCG.LOGGER.error("Failed to delete temporary image file: {}", tempFile.getAbsolutePath());
			throw e;
		}
	}
	/** 在渲染线程执行，完成后交付 future（渲染必须回到渲染线程） */
	public static <T> CompletableFuture<T> onRenderThread(Supplier<T> work) {
		var future = new CompletableFuture<T>();
		RenderSystem.recordRenderCall(() -> {
			try {
				future.complete(work.get());
			} catch (Throwable t) {
				future.completeExceptionally(t);
			}
		});
		return future;
	}
}
