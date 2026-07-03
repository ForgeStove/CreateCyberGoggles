package io.github.forgestove.create_cyber_goggles.config.network;
import io.github.forgestove.create_cyber_goggles.config.Config;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
/** C2S packet: admin client sends a lock/unlock request for a specific config entry. */
public record ConfigLockPayload(String configId, String value) implements CustomPacketPayload {
	public static final Type<ConfigLockPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Config.getModId(), "config_lock"));
	public static final StreamCodec<FriendlyByteBuf, ConfigLockPayload> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8,
		ConfigLockPayload::configId,
		ByteBufCodecs.STRING_UTF8,
		ConfigLockPayload::value,
		ConfigLockPayload::new
	);
	@Override
	public @NotNull Type<ConfigLockPayload> type() {
		return TYPE;
	}
}
