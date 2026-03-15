package io.github.forgestove.create_cyber_goggles.mixin;
import io.github.forgestove.create_cyber_goggles.core.util.IHaveGoggleInformationHook;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.*;

import java.util.*;
public final class CCGMixinPlugin implements IMixinConfigPlugin {
	//region
	@Override
	public void onLoad(String mixinPackage) {}
	@Override
	public String getRefMapperConfig() {return null;}
	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {return true;}
	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
	@Override
	public List<String> getMixins() {return null;}
	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
	//endregion
	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		IHaveGoggleInformationHook.hook(targetClassName, targetClass, mixinClassName);
	}
}
