package io.github.forgestove.create_cyber_goggles.core.schematic;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.*;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.ClientTickEvent.Post;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
/**
 * 蓝图渲染进度条：在动作栏显示「阶段 + 进度条 + 百分比」，失败与完成态短暂停留后消失。
 * <p>
 * 移植自 Create: Blueprinted（MIT），去掉了分享相关状态，改用本模组语言键。
 */
public enum ImageActionProgress {
	EXPORTED(1F, Type.FINAL, SchematicLang.LIGHT_GREEN),
	EXPORT_FAILED(1F, Type.FAIL, SchematicLang.ERROR_PRIMARY),
	RENDER_FAILED(1F, Type.FAIL, SchematicLang.ERROR_PRIMARY),
	EXPORTING(0.8F),
	RENDERING(0.4F),
	BAKING(0F),
	INACTIVE(0F);
	private static final int BAR_SEGMENTS = 10;
	private static final long LINGER_MILLIS = 1500L;
	private static final ProgressState INACTIVE_STATE = new ProgressState(INACTIVE, "");
	private static final AtomicReference<ProgressState> PROGRESS_STATE = new AtomicReference<>(INACTIVE_STATE);
	public final float fraction;
	public final int barColor;
	public final Type type;
	public final String messageKey;
	ImageActionProgress(float fraction) {
		this(fraction, Type.INTERMEDIATE, SchematicLang.LIGHT_BLUE);
	}
	ImageActionProgress(float fraction, Type type, int barColor) {
		this.fraction = Math.clamp(fraction, 0F, 1F);
		this.barColor = barColor;
		this.type = type;
		messageKey = "message.action_progress." + name().toLowerCase(Locale.ENGLISH);
	}
	public static void start(@NotNull String fileName) {
		PROGRESS_STATE.set(new ProgressState(BAKING, fileName));
	}
	public static void setState(ImageActionProgress progress) {
		var progressState = PROGRESS_STATE.get();
		var fileName = progressState.fileName;
		if (fileName == null) throw new NullPointerException("Failed to set intermediary progress state. Image file name cannot be null.");
		if (progress.type == Type.INTERMEDIATE) PROGRESS_STATE.set(new ProgressState(progress, fileName));
		else PROGRESS_STATE.set(new ProgressState(progress, fileName, now() + LINGER_MILLIS));
	}
	private static long now() {
		return System.currentTimeMillis();
	}
	public static void cancel() {
		PROGRESS_STATE.set(INACTIVE_STATE);
	}
	public static void onClientTick(Post ignoredEvent) {
		var state = PROGRESS_STATE.get();
		var progress = state.progress;
		Player player = Minecraft.getInstance().player;
		if (progress == INACTIVE) return;
		if (progress.type != Type.INTERMEDIATE && now() >= state.clearAt) {
			PROGRESS_STATE.compareAndSet(state, INACTIVE_STATE);
			return;
		}
		if (player != null) player.displayClientMessage(buildMessage(state), true);
	}
	private static Component buildMessage(ProgressState state) {
		var progress = state.progress;
		var clamped = Math.clamp(progress.fraction, 0F, 1F);
		var filled = Math.round(clamped * BAR_SEGMENTS);
		var bar = new StringBuilder();
		for (var i = 0; i < BAR_SEGMENTS; i++) bar.append(i < filled ? '█' : '▒');
		var failed = progress.type == Type.FAIL;
		var message = SchematicLang.translatable(progress.messageKey, state.fileName)
			.withColor(failed ? SchematicLang.ERROR_DARKER : SchematicLang.DARK_BLUE)
			.append(Component.literal(" " + bar).withStyle(Style.EMPTY.withColor(progress.barColor)));
		if (!failed) message.append(Component.literal(" " + Math.round(clamped * 100F) + "%").withColor(SchematicLang.LIGHT_BLUE));
		return message;
	}
	public enum Type {
		FINAL,
		FAIL,
		INTERMEDIATE
	}
	record ProgressState(ImageActionProgress progress, String fileName, long clearAt) {
		ProgressState(ImageActionProgress progress, String fileName) {
			this(progress, fileName, 0);
		}
	}
}
