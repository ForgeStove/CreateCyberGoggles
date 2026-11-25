package io.github.forgestove.create_cyber_goggles.mixin;
import com.mojang.logging.LogUtils;
import net.neoforged.fml.loading.LoadingModList;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.extensibility.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
public class CCGMixinPlugin implements IMixinConfigPlugin {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final String MOD_CONDITION = Type.getDescriptor(ModCondition.class);
	private static final Map<String, Boolean> CACHE = new ConcurrentHashMap<>();
	private static boolean satisfiesModCondition(String mixinClassName) {
		ModConditions condition = readModCondition(mixinClassName);
		if (condition == null) return true;
		boolean applied =
			condition.require().stream().allMatch(id -> LoadingModList.get().getModFileById(id) != null) && condition.conflict()
			.stream()
			.noneMatch(id -> LoadingModList.get().getModFileById(id) != null);
		LOGGER.info(
			"Conditional Mixin {} {}: require={} conflict={}",
			mixinClassName,
			applied ? "applied" : "skipped",
			condition.require(),
			condition.conflict()
		);
		return applied;
	}
	private static @Nullable ModConditions readModCondition(String mixinClassName) {
		String path = "/" + mixinClassName.replace('.', '/') + ".class";
		try (InputStream in = CCGMixinPlugin.class.getResourceAsStream(path)) {
			if (in == null) {
				LOGGER.warn("Failed to read mixin class bytes: {}", mixinClassName);
				return null;
			}
			var node = new ClassNode();
			new ClassReader(in).accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
			if (node.visibleAnnotations == null) return null;
			for (AnnotationNode annotation : node.visibleAnnotations) {
				if (!MOD_CONDITION.equals(annotation.desc) || annotation.values == null) continue;
				List<String> require = List.of();
				List<String> conflict = List.of();
				for (var i = 0; i < annotation.values.size(); i += 2) {
					Object value = annotation.values.get(i + 1);
					if ("require".equals(annotation.values.get(i))) require = enumConstants(value);
					else if ("conflict".equals(annotation.values.get(i))) conflict = enumConstants(value);
				}
				return new ModConditions(require, conflict);
			}
			return null;
		} catch (IOException e) {
			LOGGER.warn("Failed to read mixin class bytes: {}", mixinClassName, e);
			return null;
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
	private record ModConditions(List<String> require, List<String> conflict) {}
}
