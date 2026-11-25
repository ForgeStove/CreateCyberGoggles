package io.github.forgestove.create_cyber_goggles.mixin;
import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.extensibility.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
public class CCGMixinPlugin implements IMixinConfigPlugin {
	private static final String MOD_CONDITION = Type.getDescriptor(ModCondition.class);
	private static final Map<String, Boolean> CACHE = new ConcurrentHashMap<>();
	private static boolean satisfiesModCondition(String mixinClassName) {
		var condition = readModCondition(mixinClassName);
		for (String modId : condition.present())
			if (LoadingModList.get().getModFileById(modId) == null) return false;
		for (String modId : condition.absent())
			if (LoadingModList.get().getModFileById(modId) != null) return false;
		return true;
	}
	private static ModConditions readModCondition(String mixinClassName) {
		String path = "/" + mixinClassName.replace('.', '/') + ".class";
		try (InputStream in = CCGMixinPlugin.class.getResourceAsStream(path)) {
			if (in == null) return ModConditions.EMPTY;
			var node = new ClassNode();
			new ClassReader(in).accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
			if (node.visibleAnnotations == null) return ModConditions.EMPTY;
			for (AnnotationNode annotation : node.visibleAnnotations) {
				if (!MOD_CONDITION.equals(annotation.desc) || annotation.values == null) continue;
				List<String> present = List.of();
				List<String> absent = List.of();
				for (var i = 0; i < annotation.values.size(); i += 2) {
					Object value = annotation.values.get(i + 1);
					if ("value".equals(annotation.values.get(i))) present = enumConstants(value);
					else if ("absent".equals(annotation.values.get(i))) absent = enumConstants(value);
				}
				return new ModConditions(present, absent);
			}
			return ModConditions.EMPTY;
		} catch (IOException e) {
			return ModConditions.EMPTY;
		}
	}
	private static List<String> enumConstants(Object value) {
		if (value instanceof String[] constant) return List.of(constant[1]);
		if (!(value instanceof List<?> array)) return List.of();
		List<String> constants = new ArrayList<>(array.size());
		for (Object element : array)
			if (element instanceof String[] constant) constants.add(constant[1]);
		return constants;
	}
	@Override
	public void onLoad(String mixinPackage) {}
	@Override
	public String getRefMapperConfig() {
		return null;
	}
	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return CACHE.computeIfAbsent(mixinClassName, CCGMixinPlugin::satisfiesModCondition);
	}
	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
	@Override
	public List<String> getMixins() {
		return null;
	}
	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
	private record ModConditions(List<String> present, List<String> absent) {
		private static final ModConditions EMPTY = new ModConditions(List.of(), List.of());
	}
}
