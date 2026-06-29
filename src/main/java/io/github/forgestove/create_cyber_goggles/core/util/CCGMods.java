package io.github.forgestove.create_cyber_goggles.core.util;
import net.createmod.catnip.lang.Lang;
import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.loading.LoadingModList;

import java.util.Optional;
import java.util.function.Supplier;
public enum CCGMods {
	SIMULATED,
	OBSCURE_TOOLTIPS;
	private final String id;
	CCGMods() {
		id = Lang.asId(name());
	}
	/**
	 * @return 模组 id
	 */
	public String id() {
		return id;
	}
	public Block getBlock(String id) {
		return BuiltInRegistries.BLOCK.get(rl(id));
	}
	public ResourceLocation rl(String path) {
		return ResourceLocation.fromNamespaceAndPath(id, path);
	}
	public Item getItem(String id) {
		return BuiltInRegistries.ITEM.get(rl(id));
	}
	public boolean contains(ItemLike entry) {
		if (!isLoaded()) return false;
		var asItem = entry.asItem();
		return RegisteredObjectsHelper.getKeyOrThrow(asItem).getNamespace().equals(id);
	}
	/**
	 * @return 该模组是否已加载，基于模组 id 判断
	 */
	public boolean isLoaded() {
		return LoadingModList.get().getModFileById(id) != null;
	}
	/**
	 * 模组加载时执行代码并返回结果
	 *
	 * @param toRun 仅在模组已加载时执行
	 * @return 模组未加载返回 {@code Optional.empty()}，否则返回 supplier 的值
	 */
	@SuppressWarnings("unused")
	public <T> Optional<T> runIfInstalled(Supplier<T> toRun) {
		if (isLoaded()) return Optional.of(toRun.get());
		return Optional.empty();
	}
	/**
	 * 模组加载时执行代码（无返回值）
	 *
	 * @param toExecute 仅在模组已加载时执行
	 */
	public void executeIfInstalled(Runnable toExecute) {
		if (isLoaded()) toExecute.run();
	}
}
