package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.forgestove.create_cyber_goggles.CCG;
import net.minecraft.nbt.NbtAccounter;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(NbtAccounter.class)
public abstract class NbtAccounterMixin {
	@WrapMethod(method = "accountBytes(J)V")
	public void accountBytes(long bytes, Operation<Void> original) {
		try {
			original.call(bytes);
		} catch (Throwable throwable) {
			if (!CCG.CONFIG.misc.nbtFix) throw throwable;
		}
	}
}
