package io.github.forgestove.create_cyber_goggles.core.util;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jetbrains.annotations.NotNull;

import java.util.*;
public final class SequencedAssemblyUtil {
	public static final int JUNK_X = 143;
	public static final int JUNK_Y = 90;
	public static final int JUNK_SIZE = 18;
	public static final long AUTO_ROTATE_INTERVAL_MS = 1200L;
	private static final Map<SequencedAssemblyRecipe, long[]> STATES = new WeakHashMap<>();
	public static boolean shouldEnable(SequencedAssemblyRecipe recipe) {
		return recipe.getOutputChance() != 1 && getJunkCount(recipe) > 0;
	}
	public static int getJunkCount(SequencedAssemblyRecipe recipe) {
		return Math.max(recipe.resultPool.size() - 1, 0);
	}
	public static boolean isOverJunkSlot(double mouseX, double mouseY) {
		return mouseX >= JUNK_X && mouseX < JUNK_X + JUNK_SIZE && mouseY >= JUNK_Y && mouseY < JUNK_Y + JUNK_SIZE;
	}
	public static IJeiInputHandler createInputHandler(SequencedAssemblyRecipe recipe) {
		return new IJeiInputHandler() {
			@Override
			@NotNull
			public ScreenRectangle getArea() {
				return new ScreenRectangle(JUNK_X, JUNK_Y, JUNK_SIZE, JUNK_SIZE);
			}
			@Override
			public boolean handleMouseScrolled(double mouseX, double mouseY, double scrollDeltaX, double scrollDeltaY) {
				if (scrollDeltaY == 0) return false;
				var state = getState(recipe);
				var junkCount = getJunkCount(recipe);
				if (junkCount <= 0) return false;
				var delta = scrollDeltaY > 0 ? -1 : 1;
				state[0] = Math.floorMod((int) state[0] + delta, junkCount);
				state[1] = System.currentTimeMillis();
				return true;
			}
		};
	}
	public static long[] getState(SequencedAssemblyRecipe recipe) {
		return STATES.computeIfAbsent(recipe, r -> new long[2]);
	}
}

