package com.forgestove.create_cyber_goggles.content.event;
import com.forgestove.create_cyber_goggles.Config;
import com.simibubi.create.content.logistics.tableCloth.TableClothBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.client.event.InputEvent;
public class MoseScroll {
	public static int index = 1;
	public static int scrollDeltaY = 0;
	public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
		if (!Config.enhancedStoreRender.get()) return;
		var mc = Minecraft.getInstance();
		if (mc.level == null || !(mc.hitResult instanceof BlockHitResult blockHitResult)) return;
		if (blockHitResult.getType() == HitResult.Type.MISS) return;
		var blockEntity = mc.level.getBlockEntity(blockHitResult.getBlockPos());
		if (!(blockEntity instanceof TableClothBlockEntity)) return;
		if (event.getScrollDeltaY() == 0) scrollDeltaY = 0;
		else scrollDeltaY = event.getScrollDeltaY() > 0 ? -1 : 1;
		event.setCanceled(true);
	}
}
