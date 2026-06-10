package io.github.forgestove.create_cyber_goggles.core.util;
import com.zurrtum.create.AllItems;
import io.github.forgestove.create_cyber_goggles.core.event.CCGKey;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
public class LocalItemUtil {
	public static @Nullable ItemStack getCreate() {
		if (CCGKey.useSchematic.isDown()) return AllItems.SCHEMATIC_AND_QUILL.getDefaultInstance();
		if (CCGKey.showSuperGlue.isDown()) return AllItems.SUPER_GLUE.getDefaultInstance();
		return null;
	}
}
