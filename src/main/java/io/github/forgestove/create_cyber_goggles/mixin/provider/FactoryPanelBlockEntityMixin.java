package io.github.forgestove.create_cyber_goggles.mixin.provider;
import com.zurrtum.create.client.AllSpecialTextures;
import com.zurrtum.create.client.content.logistics.factoryBoard.FactoryPanelConnectionHandler;
import com.zurrtum.create.content.logistics.factoryBoard.*;
import io.github.forgestove.create_cyber_goggles.CCG;
import io.github.forgestove.create_cyber_goggles.core.api.*;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;

import static io.github.forgestove.create_cyber_goggles.core.util.CCGUtil.*;
@Mixin(FactoryPanelBlockEntity.class)
public abstract class FactoryPanelBlockEntityMixin implements OutlineRenderable, Self<FactoryPanelBlockEntity> {
	@Override
	public int ccg$getRenderDelay() {
		return 0;
	}
	@Override
	public void ccg$render() {
		var thiz = thiz();
		var level = thiz.getLevel();
		if (level == null) return;
		var panels = thiz.panels;
		if (panels.isEmpty()) return;
		var hitResult = getBlockHitResult();
		if (hitResult == null) return;
		var panelSlot = FactoryPanelBlock.getTargetedSlot(thiz.getBlockPos(), thiz.getBlockState(), hitResult.getLocation());
		var behaviour = panels.get(panelSlot);
		if (behaviour == null || !behaviour.isActive()) return;
		var hashMap = new Object2IntOpenHashMap<AABB>();
		for (var targetedBy : behaviour.targetedBy.values()) {
			var panelPosition = targetedBy.from;
			var pos = panelPosition.pos();
			var box = FactoryPanelConnectionHandler.getBB(level.getBlockState(pos), panelPosition);
			hashMap.put(box, CCG.config.outliner.inColor);
		}
		for (var panelPosition : behaviour.targeting) {
			var pos = panelPosition.pos();
			var box = FactoryPanelConnectionHandler.getBB(level.getBlockState(pos), panelPosition);
			var newColor = CCG.config.outliner.outColor;
			hashMap.compute(box, (_, old) -> old == null ? newColor : blendColors(old, newColor));
		}
		hashMap.object2IntEntrySet().forEach(entry -> {
			var color = entry.getIntValue();
			var box = entry.getKey();
			outliner.showAABB("FactoryPanelIOBox" + box.getCenter(), box)
				.withFaceTextures(AllSpecialTextures.CHECKERED, AllSpecialTextures.HIGHLIGHT_CHECKERED)
				.lineWidth(1 / 16f)
				.colored(color);
		});
	}
}
