package io.github.forgestove.config.network;
import io.github.forgestove.config.FlexConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
/** S2C数据包：服务器将锁定的配置条目作为TOML分区广播给所有客户端。 */
public record ConfigSyncPayload(String modId, String tomlContent) implements CustomPacketPayload {
	public static final Type<ConfigSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(FlexConfig.ID, "config_sync"));
	public static final StreamCodec<FriendlyByteBuf, ConfigSyncPayload> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8,
		ConfigSyncPayload::modId,
		ByteBufCodecs.STRING_UTF8,
		ConfigSyncPayload::tomlContent,
		ConfigSyncPayload::new
	);
	@Override
	public @NotNull Type<ConfigSyncPayload> type() {
		return TYPE;
	}
}
