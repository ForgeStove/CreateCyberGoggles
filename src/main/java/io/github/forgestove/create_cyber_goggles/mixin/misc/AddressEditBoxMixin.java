package io.github.forgestove.create_cyber_goggles.mixin.misc;
import com.simibubi.create.content.logistics.AddressEditBox;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value = AddressEditBox.class, remap = false)
public abstract class AddressEditBoxMixin extends EditBox {
	public AddressEditBoxMixin(Font font, int x, int y, int width, int height, Component message) {
		super(font, x, y, width, height, message);
	}
	@Inject(
		method = "<init>(Lnet/minecraft/client/gui/screens/Screen;Lnet/minecraft/client/gui/Font;IIIIZLjava/lang/String;)V",
		at = @At("TAIL")
	)
	private void init(
		Screen screen,
		Font pFont,
		int pX,
		int pY,
		int pWidth,
		int pHeight,
		boolean anchorToBottom,
		String localAddress,
		CallbackInfo ci
	) {
		setMaxLength(Integer.MAX_VALUE);
	}
}
