package io.github.forgestove.create_cyber_goggles.compat.simulated;
import com.simibubi.create.content.kinetics.base.*;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import dev.simulated_team.simulated.util.extra_kinetics.ExtraKinetics;
import dev.simulated_team.simulated.util.extra_kinetics.ExtraKinetics.ExtraKineticsBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
public class ExtraKineticsHelper {
	/**
	 * @return 该朝向面所属的额外动能单元；朝向主传动轴或方块没有额外动能结构时返回 {@code null}
	 */
	public static @Nullable KineticBlockEntity forFace(KineticBlockEntity be, Direction face) {
		if (!(be instanceof ExtraKinetics kinetics)) return null;
		KineticBlockEntity extra = kinetics.getExtraKinetics();
		if (extra == null) return null;
		if (!(be.getBlockState().getBlock() instanceof ExtraKineticsBlock block)) return null;
		IRotate config = block.getExtraKineticsRotationConfiguration();
		BlockState state = be.getBlockState();
		if (config.hasShaftTowards(be.getLevel(), be.getBlockPos(), state, face)) return extra;
		if (config instanceof ICogWheel cog && cog.isSmallCog() && face.getAxis() != config.getRotationAxis(state)) return extra;
		return null;
	}
}
