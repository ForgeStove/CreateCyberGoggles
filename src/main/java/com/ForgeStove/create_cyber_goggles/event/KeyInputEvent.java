package com.ForgeStove.create_cyber_goggles.event;
import com.ForgeStove.create_cyber_goggles.Config;
import com.simibubi.create.content.logistics.tableCloth.TableClothBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.client.event.InputEvent.MouseScrollingEvent;
public class KeyInputEvent {
	public static int index = 1;
	public static int scrollDeltaY = 0;
	public static void onMouseScroll(MouseScrollingEvent event) {
		if (!Config.enhancedInfo.get()) return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.level != null && mc.hitResult instanceof BlockHitResult blockHitResult) {
			if (blockHitResult.getType() == HitResult.Type.MISS) return;
			BlockEntity blockEntity = mc.level.getBlockEntity(blockHitResult.getBlockPos());
//			if (blockEntity instanceof SmartBlockEntity smartBlockEntity) {
//				try {
//					if (mc.player == null) return;
//					Collection<BlockEntityBehaviour> behavior = Collections.singleton(smartBlockEntity.getBehaviour(
//							FilteringBehaviour.TYPE));
//					BlockEntityBehaviour first = behavior.iterator().next();
//					if (first instanceof FilteringBehaviour filteringBehaviour) {
//						ItemStack filter = filteringBehaviour.getFilter();
//						Inventory inventory = mc.player.getInventory();
//						mc.forceSetScreen(new FilterScreen(
//								FilterMenu.create(0, inventory, filter),
//								inventory,
//								filter.getHoverName()
//						));
//					}
//				} catch (Exception error) {
//					mc.player.sendSystemMessage(Component.literal(error.getMessage()));
//				}
//			}
			if (!(blockEntity instanceof TableClothBlockEntity)) return;
			event.setCanceled(true);
			if (event.getScrollDeltaY() == 0) scrollDeltaY = 0;
			else scrollDeltaY = event.getScrollDeltaY() > 0 ? -1 : 1;
		}
	}
}
