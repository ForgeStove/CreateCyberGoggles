package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.simibubi.create.content.contraptions.ContraptionCollider;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.createmod.catnip.platform.services.NetworkHelper;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(ContraptionCollider.class)
public abstract class ContraptionColliderMixin {
	@WrapWithCondition(
		method = "handleDamageFromTrain",
		at = @At(
			value = "INVOKE",
			target = "Lnet/createmod/catnip/platform/services/NetworkHelper;sendToServer"
				+ "(Lnet/minecraft/network/protocol/common/custom/CustomPacketPayload;)V"
		)
	)
	private static boolean handleDamageFromTrain(NetworkHelper instance, CustomPacketPayload customPacketPayload) {
		return !CCG.config.misc.removeTrainDamage;
	}
}
