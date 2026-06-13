package io.github.forgestove.create_cyber_goggles.mixin.compact;

import com.mojang.authlib.GameProfile;
import io.github.forgestove.create_cyber_goggles.core.api.Self;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.*;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer implements Self<LocalPlayer> {
	public LocalPlayerMixin(ClientLevel clientLevel, GameProfile gameProfile) {
		super(clientLevel, gameProfile);
	}
}
