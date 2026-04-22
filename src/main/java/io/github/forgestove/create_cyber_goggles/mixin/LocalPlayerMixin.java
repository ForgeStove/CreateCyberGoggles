package io.github.forgestove.create_cyber_goggles.mixin;
import com.mojang.authlib.GameProfile;
import com.simibubi.create.AllItems;
import dev.simulated_team.simulated.index.SimItems;
import io.github.forgestove.create_cyber_goggles.core.api.Self;
import io.github.forgestove.create_cyber_goggles.core.event.CCGKey;
import io.github.forgestove.create_cyber_goggles.core.util.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer implements Self<LocalPlayer> {
	public LocalPlayerMixin(ClientLevel clientLevel, GameProfile gameProfile) {
		super(clientLevel, gameProfile);
	}
	@Override
	public @NotNull ItemStack getItemBySlot(@NotNull EquipmentSlot slot) {
		if (slot != EquipmentSlot.MAINHAND) return super.getItemBySlot(slot);
		if (CCGKey.useSchematic.isDown()) return AllItems.SCHEMATIC_AND_QUILL.asStack();
		if (CCGKey.showSuperGlue.isDown()) return AllItems.SUPER_GLUE.asStack();
		if (!CCGMods.SIMULATED.isLoaded()) return super.getItemBySlot(slot);
		if (CCGKey.usePhysicsStaff.isDown()) return SimItems.PHYSICS_STAFF.asStack();
		if (CCGKey.showHoneyGlue.isDown()) return SimItems.HONEY_GLUE.asStack();
		return super.getItemBySlot(slot);
	}
}
