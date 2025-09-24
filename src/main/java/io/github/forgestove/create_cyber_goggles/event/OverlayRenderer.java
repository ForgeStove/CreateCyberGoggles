package io.github.forgestove.create_cyber_goggles.event;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.crafter.MechanicalCrafterBlockEntity;
import com.simibubi.create.content.kinetics.crusher.CrushingWheelControllerBlockEntity;
import com.simibubi.create.content.kinetics.millstone.MillstoneBlockEntity;
import com.simibubi.create.content.logistics.box.PackageEntity;
import com.simibubi.create.content.logistics.chute.ChuteBlockEntity;
import com.simibubi.create.content.logistics.depot.*;
import com.simibubi.create.content.logistics.packager.PackagerBlockEntity;
import io.github.forgestove.create_cyber_goggles.*;
import net.minecraft.client.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.*;
import org.jetbrains.annotations.*;
public class OverlayRenderer {
	public static void register(@NotNull RegisterGuiOverlaysEvent event) {
		event.registerAbove(VanillaGuiOverlay.HOTBAR.id(), "goggle_overlay", OverlayRenderer::renderOverlay);
	}
	public static void renderOverlay(ForgeGui forgeGui, GuiGraphics guiGraphics, float v, int i, int i1) {
		if (!CCG.CONFIG.goggles.renderExtraItems) return;
		var mc = Minecraft.getInstance();
		if (mc.isPaused() || mc.screen != null || mc.options.hideGui) return;
		var itemStack = toRenderItemStack();
		if (itemStack == null) return;
		Common.renderItemStack(guiGraphics, itemStack);
	}
	public static @Nullable ItemStack toRenderItemStack() {
		var be = Common.getBE();
		if (be instanceof DepotBlockEntity dbe) return dbe.getHeldItem();
		else if (be instanceof EjectorBlockEntity ebe) return ebe.getBehaviour(DepotBehaviour.TYPE).getHeldItemStack();
		else if (be instanceof PackagerBlockEntity pbe) return pbe.heldBox;
		else if (be instanceof ChuteBlockEntity cbe) return cbe.getItem();
		else if (be instanceof MechanicalCrafterBlockEntity mcbe) return mcbe.getInventory().getItem(0);
		else if (be instanceof CrushingWheelControllerBlockEntity cwcb) return cwcb.inventory.getStackInSlot(0);
		else if (be instanceof MillstoneBlockEntity mbe) return mbe.inputInv.getStackInSlot(0);
		else if (be instanceof BeltBlockEntity bbe) {
			var inventory = bbe.getInventory();
			if (inventory == null) return null;
			var stackAtOffset = inventory.getStackAtOffset(bbe.index);
			return stackAtOffset == null ? null : stackAtOffset.stack;
		}
		var e = Common.getE();
		if (e instanceof PackageEntity pe) return pe.getBox();
		return null;
	}
}
