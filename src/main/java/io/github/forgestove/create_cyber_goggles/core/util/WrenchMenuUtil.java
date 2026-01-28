package io.github.forgestove.create_cyber_goggles.core.util;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.List;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.*;
public class WrenchMenuUtil {
	public static final List<Property<?>> PROPERTIES_BLACKLIST = List.of(
		EYE,
		VAULT_STATE,
		DISTANCE,
		SNOWY,
		HAS_BOOK,
		HAS_RECORD,
		LEVEL_CAULDRON,
		LEVEL_COMPOSTER,
		LEVEL_FLOWING,
		LEVEL_HONEY,
		LEVEL,
		LAYERS,
		BITES,
		CANDLES,
		EGGS,
		STAGE,
		PICKLES,
		MOISTURE,
		FLOWER_AMOUNT,
		BED_PART,
		AGE_1,
		AGE_2,
		AGE_3,
		AGE_4,
		AGE_5,
		AGE_7,
		AGE_15,
		AGE_25,
		NOTEBLOCK_INSTRUMENT,
		SIGNAL_FIRE,
		SLAB_TYPE,
		BERRIES,
		BlazeBurnerBlock.HEAT_LEVEL
	);
}
