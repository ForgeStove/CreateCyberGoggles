package io.github.forgestove.create_cyber_goggles.mixin;
import com.mojang.authlib.GameProfile;
import io.github.forgestove.create_cyber_goggles.core.api.Self;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.*;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer implements Self<LocalPlayer> {
	public LocalPlayerMixin(ClientLevel clientLevel, GameProfile gameProfile) {
		super(clientLevel, gameProfile);
	}
	@Override
	public @NotNull ItemStack getItemBySlot(@NotNull EquipmentSlot slot) {
		if (slot != EquipmentSlot.MAINHAND) return super.getItemBySlot(slot);
		var stack = LocalItemUtil.getItemStack();
		if (stack != null) return stack;
		if (!CCGMods.SIMULATED.isLoaded()) return super.getItemBySlot(slot);
		var simulatedStack = LocalItemUtil.getSimulatedStack();
		if (simulatedStack != null) return simulatedStack;
		return super.getItemBySlot(slot);
	}
}
