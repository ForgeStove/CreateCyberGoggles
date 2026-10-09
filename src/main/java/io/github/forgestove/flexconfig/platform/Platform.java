package io.github.forgestove.flexconfig.platform;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.function.Supplier;
/**
 * 加载器需要提供的少量能力。
 * <p>
 * 核心部分（配置树、序列化、锁、热重载、多配置文件界面、延迟生效）只依赖本接口与 Minecraft 自身；
 * 各加载器的实现放在 {@code platform/<loader>} 下，事件订阅本身留在各自的加载器适配类里
 * （例如 NeoForge 的 {@code ConfigEvents}）。
 */
public interface Platform {
	static Platform get() {
		return Holder.INSTANCE;
	}
	/** 全局配置目录（如 {@code config/}）。 */
	Path configDir();
	/** 指定模组是否已加载。 */
	boolean isModLoaded(String modId);
	/** 是否为客户端（与专用服务器相对）。 */
	boolean isClient();
	/** 是否处于开发环境（用于只在开发期生效的检查，例如缺失翻译键告警）。 */
	boolean isDevelopmentEnvironment();
	/** 模组显示名；该模组未加载时返回 {@code null}。 */
	@Nullable String modDisplayName(String modId);
	/** 把配置界面挂到「模组列表」的入口（NeoForge 用 {@code IConfigScreenFactory}，Fabric 用 ModMenu）。 */
	void registerConfigScreen(String modId, Supplier<Screen> screenFactory);
	/** 向指定玩家发送（S2C）。 */
	void sendToPlayer(ServerPlayer player, CustomPacketPayload payload);
	/** 向所有玩家发送（S2C）。 */
	void sendToAllPlayers(CustomPacketPayload payload);
	/** 向服务端发送（C2S）。 */
	void sendToServer(CustomPacketPayload payload);
	/** 包处理器上下文的最小面。 */
	interface PayloadContext {
		@Nullable ServerPlayer player();
		void enqueue(Runnable task);
	}
	@FunctionalInterface
	interface PayloadHandler<P extends CustomPacketPayload> {
		void handle(P payload, PayloadContext context);
	}
	/** 包注册入口，由具体加载器实现并传给核心。 */
	interface Payloads {
		<P extends CustomPacketPayload> void clientToServer(Type<P> type, StreamCodec<FriendlyByteBuf, P> codec,
			PayloadHandler<P> handler);
		<P extends CustomPacketPayload> void serverToClient(Type<P> type, StreamCodec<FriendlyByteBuf, P> codec,
			PayloadHandler<P> handler);
	}
	final class Holder {
		private static final Platform INSTANCE = new NeoForgePlatform();
	}
}
