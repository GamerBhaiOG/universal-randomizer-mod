package com.universalrandomizer.features;

import com.universalrandomizer.config.RandomizerMode;
import com.universalrandomizer.core.RandomizerManager;
import com.universalrandomizer.util.RandomizerLogger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;

/**
 * Randomizes items dropped by living entities on death.
 *
 * <p>Intercepted via GLM (Forge) / {@code LootTableEvents.MODIFY} (Fabric)
 * targeting entity loot tables.  The mapping is {@code entity_type → item}.
 */
public final class MobDropRandomizer {

    private MobDropRandomizer() {}

    /**
     * Returns the randomized drop item for the given entity type.
     * Preserves the original stack count.
     */
    public static ItemStack applyDrop(EntityType<?> entityType, ItemStack originalStack) {
        RandomizerManager mgr = RandomizerManager.getInstance();
        if (!mgr.isInitialized() || !mgr.isEnabled(RandomizerMode.MOB_DROPS)) return originalStack;

        ResourceLocation entityKey = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        if (entityKey == null) return originalStack;

        ResourceLocation targetKey = mgr.getMobDrop(entityKey);
        if (targetKey.equals(entityKey)) return originalStack;

        return BuiltInRegistries.ITEM.getOptional(targetKey)
            .map(item -> {
                ItemStack result = new ItemStack(item, originalStack.getCount());
                RandomizerLogger.debug("MobDrop: {} -> {} (x{})", entityKey, targetKey, result.getCount());
                return result;
            })
            .orElse(originalStack);
    }

    public static ResourceLocation getMappedKey(ResourceLocation entityKey) {
        RandomizerManager mgr = RandomizerManager.getInstance();
        if (!mgr.isInitialized() || !mgr.isEnabled(RandomizerMode.MOB_DROPS)) return entityKey;
        return mgr.getMobDrop(entityKey);
    }
}
