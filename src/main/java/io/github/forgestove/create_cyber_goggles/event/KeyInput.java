package io.github.forgestove.create_cyber_goggles.event;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import io.github.forgestove.create_cyber_goggles.*;
import io.github.forgestove.create_cyber_goggles.util.Common;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;

import java.util.Collections;
public class KeyInput {
	public static void register() {
		ClientTickEvents.END_CLIENT_TICK.register(mc -> {
			toggleDiving();
			openConfigScreen();
			previewFilterScreen();
		});
	}
	public static void toggleDiving() {
		if (!CCGKeyMapping.toggleDiving.isDown()) return;
		var armor = CCG.CONFIG.armor;
		armor.removeDivingBootsAffect = !armor.removeDivingBootsAffect;
		Common.displayClientMessage(armor.removeDivingBootsAffect, "DivingAffect");
	}
	public static void openConfigScreen() {
		if (!CCGKeyMapping.openConfig.consumeClick()) return;
		var mc = Minecraft.getInstance();
		if (mc.screen != null) return;
		mc.setScreen(AutoConfig.getConfigScreen(CCGConfig.class, null).get());
	}
	public static void previewFilterScreen() {
		if (!CCGKeyMapping.previewFilter.isDown()) return;
		var mc = Minecraft.getInstance();
		if (mc.screen != null) {
			if (!(mc.screen instanceof AbstractContainerScreen<?> screen)) return;
			var slot = screen.hoveredSlot;
			if (slot == null) return;
			Common.openFilterScreen(slot.getItem());
		} else {
			if (mc.level == null || !(mc.hitResult instanceof BlockHitResult blockHitResult)) return;
			if (blockHitResult.getType() == Type.MISS) return;
			var be = mc.level.getBlockEntity(blockHitResult.getBlockPos());
			if (!(be instanceof SmartBlockEntity sbe)) return;
			var first = Collections.singleton(sbe.getBehaviour(FilteringBehaviour.TYPE)).iterator().next();
			if (!(first instanceof FilteringBehaviour)) return;
			Common.openFilterScreen(first.getFilter(blockHitResult.getDirection()));
		}
	}
}
