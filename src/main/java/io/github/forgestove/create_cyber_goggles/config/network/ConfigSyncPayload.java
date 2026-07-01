package io.github.forgestove.create_cyber_goggles.config.network;
import io.github.forgestove.create_cyber_goggles.config.Config;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
/** S2C packet: server broadcasts locked config entries as a TOML section to all clients. */
public record ConfigSyncPayload(String tomlContent) implements CustomPacketPayload {
	public static final Type<ConfigSyncPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Config.getModId(), "config_sync"));
	public static final StreamCodec<FriendlyByteBuf, ConfigSyncPayload> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8,
		ConfigSyncPayload::tomlContent,
		ConfigSyncPayload::new
	);
	@Override
	public @NotNull Type<ConfigSyncPayload> type() {
		return TYPE;
	}
}
