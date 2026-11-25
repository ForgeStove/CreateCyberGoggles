package io.github.forgestove.create_cyber_goggles.compat.sable;
import dev.ryanhcode.sable.Sable;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
public class SubLevelHelper {
	public static Vec3 projectOutOfSubLevel(Level level, Vec3 pos) {
		return Sable.HELPER.projectOutOfSubLevel(level, pos);
	}
}
