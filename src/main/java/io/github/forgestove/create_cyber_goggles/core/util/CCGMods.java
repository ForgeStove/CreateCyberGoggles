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
@SuppressWarnings("unused")
public enum CCGMods {
	SIMULATED,
	OBSCURE_TOOLTIPS;
	private final String id;
	CCGMods() {
		id = Lang.asId(name());
	}
	/**
	 * @return the mod id
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
	 * @return a boolean of whether the mod is loaded or not based on mod id
	 */
	public boolean isLoaded() {
		return LoadingModList.get().getModFileById(id) != null;
	}
	/**
	 * Simple hook to run code if a mod is installed
	 *
	 * @param toRun will be run only if the mod is loaded
	 * @return Optional.empty() if the mod is not loaded, otherwise an Optional of the return value of the given supplier
	 */
	public <T> Optional<T> runIfInstalled(Supplier<Supplier<T>> toRun) {
		if (isLoaded()) return Optional.of(toRun.get().get());
		return Optional.empty();
	}
	/**
	 * Simple hook to execute code if a mod is installed
	 *
	 * @param toExecute will be executed only if the mod is loaded
	 */
	public void executeIfInstalled(Supplier<Runnable> toExecute) {
		if (isLoaded()) toExecute.get().run();
	}
}
