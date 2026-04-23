package io.github.forgestove.create_cyber_goggles.mixin.compact;
import com.mojang.authlib.GameProfile;
import io.github.forgestove.create_cyber_goggles.core.api.Self;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.isInGUI;
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer implements Self<LocalPlayer> {
	public LocalPlayerMixin(ClientLevel clientLevel, GameProfile gameProfile) {
		super(clientLevel, gameProfile);
	}
	@Override
	public @NotNull ItemStack getItemBySlot(@NotNull EquipmentSlot slot) {
		if (slot != EquipmentSlot.MAINHAND || isInGUI()) return super.getItemBySlot(slot);
		var stack = LocalItemUtil.getCreate();
		if (stack != null) return stack;
		if (!CCGMods.SIMULATED.isLoaded()) return super.getItemBySlot(slot);
		stack = LocalItemUtil.getSimulated();
		if (stack != null) return stack;
		return super.getItemBySlot(slot);
	}
}
