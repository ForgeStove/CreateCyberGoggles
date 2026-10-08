package io.github.forgestove.flexconfig.network;
import io.github.forgestove.flexconfig.FlexConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
/** S2C数据包：服务器把某个 SERVER 型配置的整份内容下发给客户端（客户端只存内存，不落盘）。 */
public record ServerConfigPayload(String modId, String fileName, String tomlContent) implements CustomPacketPayload {
	public static final Type<ServerConfigPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(FlexConfig.ID, "server_config"));
	public static final StreamCodec<FriendlyByteBuf, ServerConfigPayload> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8,
		ServerConfigPayload::modId,
		ByteBufCodecs.STRING_UTF8,
		ServerConfigPayload::fileName,
		ByteBufCodecs.STRING_UTF8,
		ServerConfigPayload::tomlContent,
		ServerConfigPayload::new
	);
	@Override
	public @NotNull Type<ServerConfigPayload> type() {
		return TYPE;
	}
}
