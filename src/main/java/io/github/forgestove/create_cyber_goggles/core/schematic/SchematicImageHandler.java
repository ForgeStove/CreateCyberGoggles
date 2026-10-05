package io.github.forgestove.create_cyber_goggles.core.schematic;
import com.mojang.blaze3d.platform.NativeImage;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.schematic.SchematicRenderSettings.Builder;
import net.createmod.catnip.levelWrappers.SchematicLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.*;
import net.minecraft.network.chat.HoverEvent.Action;
import net.minecraft.util.PngInfo;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

import static io.github.forgestove.create_cyber_goggles.core.schematic.SchematicImageRenderer.downsample;
import static io.github.forgestove.create_cyber_goggles.core.schematic.SchematicImageUtil.onRenderThread;
/**
 * 一次「蓝图 → PNG」渲染的编排：后台烘焙 → 渲染线程离屏渲染 → 降采样 → 写盘，
 * 全程不阻塞游戏线程，进度通过 {@link ImageActionProgress} 显示在动作栏。
 * <p>
 * 移植自 Create: Blueprinted（MIT），去掉了分享（ShareProvider）与对外渲染事件。
 */
public class SchematicImageHandler {
	public static final int RENDER_TIMEOUT_SECS = 60;
	/** 渲染管线单线程：GL 无关的烘焙、降采样与写盘都排在这条队列上 */
	public static final ExecutorService PIPELINE = Executors.newSingleThreadExecutor(runnable -> {
		var thread = new Thread(runnable, "create-cyber-goggles-schematic-image");
		thread.setDaemon(true);
		return thread;
	});
	private static final Component RENDER_ERROR = SchematicLang.error("schematicImage");
	private static final Component EXPORT_ERROR = SchematicLang.error("schematicExport");
	private static final Component RENDER_FAILED = SchematicLang.error("schematicImage.renderFailed");
	private static final Component EMPTY_IMAGE_BAKE = SchematicLang.error("schematicImage.emptyImageBake");
	private static final Component CONVERT_AND_VALIDATE_FAILED = SchematicLang.error("schematicImage.convertAndValidateFailed");
	private static final Component TIMED_OUT = SchematicLang.error("schematicImage.timedOut", RENDER_TIMEOUT_SECS);
	private static final Component CLICK_TO_OPEN_EXPORT = SchematicLang.translatable("command.schematicExport.clickToOpen");
	private final CommandSourceStack source;
	private final String schematicName;
	private final Builder settingsBuilder;
	private final SchematicLevel schematicLevel;
	private final ClientLevel level;
	private @Nullable Supplier<SchematicImageRenderer> renderSupplier;
	public SchematicImageHandler(String schematicName, CommandSourceStack source, Builder settingsBuilder) {
		this.schematicName = schematicName;
		this.source = source;
		this.settingsBuilder = settingsBuilder;
		level = Minecraft.getInstance().level;
		if (level == null) throw new IllegalStateException("Cannot construct schematic image handler without a loaded level.");
		schematicLevel = new SchematicLevel(BlockPos.ZERO, level);
	}
	public void export() {
		var client = Minecraft.getInstance();
		renderAndDownsample().thenApplyAsync(imageByteArray -> processExport(imageByteArray, client), PIPELINE)
			.whenComplete((file, e) -> onExportFinish(file, e, client));
	}
	private CompletableFuture<byte[]> renderAndDownsample() {
		if (renderSupplier == null)
			renderSupplier = () -> SchematicImageRenderer.bakeFromTemplate(
					SchematicImageUtil.loadTemplateFromSchematicName(schematicName),
					schematicLevel,
					level
				)
				.orElseThrow(() -> new EmptyImageBakeException("Structure template is empty."));
		var settings = settingsBuilder.build();
		var ssaa = settings.antialiasingFactor();
		ImageActionProgress.start(schematicName);
		var client = Minecraft.getInstance();
		return CompletableFuture.supplyAsync(renderSupplier, PIPELINE)
			.thenCompose(renderer -> onRenderThread(() -> render(renderer)))
			.thenApplyAsync(image -> ssaa == 1 ? image : downsample(image, ssaa), PIPELINE)
			.thenApply(this::convertToByteArray)
			.orTimeout(RENDER_TIMEOUT_SECS, TimeUnit.SECONDS)
			.handle((imageByteArray, e) -> handleRenderExceptions(imageByteArray, e, client));
	}
	private File processExport(byte[] imageByteArray, Minecraft client) {
		if (imageByteArray == null) return null;
		var schematicDirectory = new File(client.gameDirectory.getPath(), "schematics");
		ImageActionProgress.setState(ImageActionProgress.EXPORTING);
		try {
			return SchematicImageUtil.saveImage(schematicDirectory, schematicName, "png", imageByteArray);
		} catch (IOException e) {
			throw new CompletionException(e);
		}
	}
	private void onExportFinish(@Nullable File outputFile, @Nullable Throwable e, Minecraft client) {
		if (outputFile == null) return; // 渲染失败或已超时
		var cause = getExceptionCause(e);
		if (cause != null) {
			if (cause instanceof IOException) client.execute(() -> source.sendFailure(EXPORT_ERROR));
			ImageActionProgress.setState(ImageActionProgress.EXPORT_FAILED);
			CCG.LOGGER.error("Failed to export schematic image {}.", schematicName, e);
			return;
		}
		client.execute(() -> {
			var finalMessage = SchematicLang.translatable("command.schematicExport.success")
				.withColor(SchematicLang.LIGHT_BLUE)
				.append(Component.literal(outputFile.toString())
					.withStyle(Style.EMPTY.withColor(SchematicLang.DARK_BLUE)
						.withUnderlined(true)
						.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, outputFile.getAbsolutePath()))
						.withHoverEvent(new HoverEvent(Action.SHOW_TEXT, CLICK_TO_OPEN_EXPORT))));
			source.sendSuccess(() -> finalMessage, false);
		});
		ImageActionProgress.setState(ImageActionProgress.EXPORTED);
	}
	private NativeImage render(SchematicImageRenderer renderer) {
		ImageActionProgress.setState(ImageActionProgress.RENDERING);
		return renderer.render(settingsBuilder.build());
	}
	private byte[] convertToByteArray(NativeImage image) {
		byte[] imageByteArray;
		try (image) {
			imageByteArray = image.asByteArray();
			PngInfo.fromBytes(imageByteArray);
			return imageByteArray;
		} catch (IOException e) {
			throw new CompletionException(e);
		}
	}
	private byte[] handleRenderExceptions(byte[] imageByteArray, @Nullable Throwable e, Minecraft client) {
		var cause = getExceptionCause(e);
		if (cause == null) return imageByteArray;
		ImageActionProgress.setState(ImageActionProgress.RENDER_FAILED);
		client.execute(() -> {
			var renderError = RENDER_ERROR.copy().append(" ");
			//noinspection IfCanBeSwitch
			if (cause instanceof EmptyImageBakeException) source.sendFailure(renderError.append(EMPTY_IMAGE_BAKE));
			else if (cause instanceof SchematicImageRenderException) source.sendFailure(renderError.append(RENDER_FAILED));
			else if (cause instanceof IOException) source.sendFailure(renderError.append(CONVERT_AND_VALIDATE_FAILED));
			else if (cause instanceof TimeoutException) source.sendFailure(renderError.append(TIMED_OUT));
		});
		CCG.LOGGER.error("Failed to render schematic {}.", schematicName, e);
		return null;
	}
	private static @Nullable Throwable getExceptionCause(@Nullable Throwable e) {
		return e instanceof CompletionException && e.getCause() != null ? e.getCause() : e;
	}
}
