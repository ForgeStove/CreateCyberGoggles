package io.github.forgestove.create_cyber_goggles.core.util;
import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.util.List;
public final class IHaveGoggleInformationHook {
	private static final String GOGGLE_INFO_MIXIN = "io.github.forgestove.create_cyber_goggles.mixin.goggles.IHaveGoggleInformationMixin";
	private static final String TARGET_INTERFACE = "com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation";
	private static final String METHOD_NAME = "containedFluidTooltip";
	private static final String METHOD_DESC = "(Ljava/util/List;ZLnet/minecraftforge/common/util/LazyOptional;)Z";
	private static final String HOOK_OWNER = Type.getInternalName(IHaveGoggleInformationHook.class);
	@SuppressWarnings("unused")
	public static boolean containedFluidTooltip(List<Component> tooltip, boolean isPlayerSneaking, LazyOptional<IFluidHandler> optional) {
		var resolve = optional.resolve();
		if (resolve.isEmpty()) return false;
		var handler = resolve.get();
		if (handler.getTanks() == 0) return false;
		CreateLang.translate("gui.goggles.fluid_container").forGoggles(tooltip);
		var isEmpty = true;
		for (var i = 0; i < handler.getTanks(); i++) {
			var fluidStack = handler.getFluidInTank(i);
			if (fluidStack.isEmpty()) continue;
			CCGLang.fluid(fluidStack, handler.getTankCapacity(i)).forGoggles(tooltip);
			isEmpty = false;
		}
		if (handler.getTanks() > 1) {
			if (isEmpty && !tooltip.isEmpty()) tooltip.remove(tooltip.size() - 1);
			return true;
		}
		if (isEmpty) for (var i = 0; i < handler.getTanks(); i++)
			CCGLang.fluid(FluidStack.EMPTY, handler.getTankCapacity(i)).forGoggles(tooltip);
		return true;
	}
	public static void hook(String targetClassName, ClassNode targetClass, String mixinClassName) {
		if (!GOGGLE_INFO_MIXIN.equals(mixinClassName) || !TARGET_INTERFACE.equals(targetClassName)) return;
		var methodNode = targetClass.methods.stream().filter(node -> METHOD_NAME.equals(node.name)).findAny().orElse(null);
		if (methodNode == null) return;
		if ((methodNode.access & Opcodes.ACC_ABSTRACT) != 0) return;
		var insns = new InsnList();
		insns.add(new VarInsnNode(Opcodes.ALOAD, 1));
		insns.add(new VarInsnNode(Opcodes.ILOAD, 2));
		insns.add(new VarInsnNode(Opcodes.ALOAD, 3));
		insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, HOOK_OWNER, METHOD_NAME, METHOD_DESC, false));
		insns.add(new InsnNode(Opcodes.IRETURN));
		methodNode.instructions.clear();
		methodNode.instructions.add(insns);
		methodNode.tryCatchBlocks.clear();
		methodNode.maxStack = 3;
		methodNode.maxLocals = Math.max(methodNode.maxLocals, 4);
	}
}
