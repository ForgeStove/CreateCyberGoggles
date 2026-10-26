package io.github.forgestove.create_cyber_goggles.mixin.accessor;
import com.simibubi.create.foundation.gui.widget.IconButton;
import net.createmod.catnip.gui.element.ScreenElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(IconButton.class)
public interface IconButtonAccessor {
	@Accessor
	ScreenElement getIcon();
}
