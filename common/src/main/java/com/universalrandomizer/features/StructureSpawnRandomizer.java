package com.universalrandomizer.features;

import com.universalrandomizer.config.RandomizerMode;
import com.universalrandomizer.core.RandomizerManager;
import com.universalrandomizer.util.RandomizerLogger;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Randomizes structure spawn generation across world generation.
 */
public final class StructureSpawnRandomizer {

    private StructureSpawnRandomizer() {}

    /**
     * Randomizes a structure type RL during world structure placement.
     */
    public static ResourceLocation getMappedStructure(ResourceLocation originalStructureKey) {
        if (originalStructureKey == null) return null;

        RandomizerManager mgr = RandomizerManager.getInstance();
        if (!mgr.isInitialized() || !mgr.isEnabled(RandomizerMode.STRUCTURE_SPAWNS)) return originalStructureKey;

        ResourceLocation targetKey = mgr.getTable().lookup(
            mgr.getTable().getStructureSpawns(), originalStructureKey, new java.util.Random());

        if (targetKey != null && !targetKey.equals(originalStructureKey)) {
            RandomizerLogger.debug("Structure Spawn: {} -> {}", originalStructureKey, targetKey);
            return targetKey;
        }
        return originalStructureKey;
    }
}
