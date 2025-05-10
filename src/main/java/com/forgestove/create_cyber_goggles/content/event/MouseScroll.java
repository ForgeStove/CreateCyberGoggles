package com.forgestove.create_cyber_goggles.content.event;
import com.forgestove.create_cyber_goggles.content.config.CCGConfig;
import com.forgestove.create_cyber_goggles.content.util.StaticManager;
import com.simibubi.create.content.logistics.tableCloth.TableClothBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.neoforge.client.event.InputEvent.MouseScrollingEvent;
public class MouseScroll {
	public static void onMouseScroll(MouseScrollingEvent event) {
		if (!CCGConfig.get().goggles.enhancedStoreRender) return;
		var mc = Minecraft.getInstance();
		var level = mc.level;
		if (mc.isPaused()) return;
		if (level == null
			|| !(mc.hitResult instanceof BlockHitResult blockHitResult)
			|| blockHitResult.getType() == Type.MISS
			|| !(level.getBlockEntity(blockHitResult.getBlockPos()) instanceof TableClothBlockEntity tableClothBlockEntity)
			|| !tableClothBlockEntity.isShop()) {
			StaticManager.index = 1;
			return;
		}
		if (event.getScrollDeltaY() == 0) StaticManager.scrollDeltaY = 0;
		else StaticManager.scrollDeltaY = event.getScrollDeltaY() > 0 ? -1 : 1;
		event.setCanceled(true);
	}
}
