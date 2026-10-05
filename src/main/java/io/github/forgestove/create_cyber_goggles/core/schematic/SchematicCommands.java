package io.github.forgestove.create_cyber_goggles.core.schematic;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.coordinates.RotationArgument;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

import java.util.concurrent.CompletableFuture;

import static io.github.forgestove.create_cyber_goggles.core.schematic.SchematicRenderSettings.*;
import static net.minecraft.commands.Commands.*;
/**
 * 客户端命令 {@code /ccg schematic export <文件名> [宽度] [朝向] [抗锯齿倍率]}。
 * <p>
 * 移植自 Create: Blueprinted（MIT）的 {@code /schematic export}：去掉分享分支，收进 {@code /ccg} 命名空间
 * 以免与其它蓝图类模组抢 {@code /schematic} 这个名字。
 */
public final class SchematicCommands {
	public static void register(RegisterClientCommandsEvent event) {
		event.getDispatcher()
			.register(literal("ccg").then(literal("schematic").then(literal("export").then(argument(
				"fileName",
				StringArgumentType.string()
			).suggests((ctx, builder) -> suggestSchematics(builder))
				.executes(ctx -> dispatch(ctx, builder()))
				.then(argument("width", IntegerArgumentType.integer(MIN_WIDTH, MAX_WIDTH)).executes(ctx -> dispatch(
						ctx,
						builder().imageWidth(IntegerArgumentType.getInteger(ctx, "width"))
					))
					.then(argument("rotation", RotationArgument.rotation()).executes(ctx -> dispatch(
							ctx,
							withWidth(ctx).orientation(getOrientation(ctx))
						))
						.then(argument("antialiasingFactor", IntegerArgumentType.integer(1, MAX_ANTIALIASING)).executes(ctx -> dispatch(
							ctx,
							withWidth(ctx).orientation(getOrientation(ctx))
								.antialiasingFactor(IntegerArgumentType.getInteger(ctx, "antialiasingFactor"))
						)))))))));
	}
	private static CompletableFuture<Suggestions> suggestSchematics(SuggestionsBuilder builder) {
		SchematicImageUtil.getAllSchematicNames().forEach(builder::suggest);
		return builder.buildFuture();
	}
	private static int dispatch(CommandContext<CommandSourceStack> ctx, Builder settingsBuilder) {
		var fileName = StringArgumentType.getString(ctx, "fileName");
		new SchematicImageHandler(fileName, ctx.getSource(), settingsBuilder).export();
		return Command.SINGLE_SUCCESS;
	}
	private static Builder withWidth(CommandContext<CommandSourceStack> ctx) {
		return builder().imageWidth(IntegerArgumentType.getInteger(ctx, "width"));
	}
	/** {@code ~ ~} 解析为玩家当前视角 */
	private static Orientation getOrientation(CommandContext<CommandSourceStack> ctx) {
		Vec2 rotation = RotationArgument.getRotation(ctx, "rotation").getRotation(ctx.getSource());
		return new Orientation(rotation.y, rotation.x);
	}
}
