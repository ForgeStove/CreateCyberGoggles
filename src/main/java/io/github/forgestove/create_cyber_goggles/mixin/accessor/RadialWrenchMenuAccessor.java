package io.github.forgestove.create_cyber_goggles.mixin.accessor;
import com.simibubi.create.content.contraptions.wrench.RadialWrenchMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
import java.util.Map.Entry;
@Mixin(RadialWrenchMenu.class)
public interface RadialWrenchMenuAccessor {
	@Invoker("<init>")
	static RadialWrenchMenu create(BlockState state, BlockPos pos, Level level, List<Entry<Property<?>, String>> properties) {
		throw new AssertionError();
	}
}
