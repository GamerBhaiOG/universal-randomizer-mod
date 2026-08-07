package com.universalrandomizer.features;

import com.universalrandomizer.config.RandomizerMode;
import com.universalrandomizer.core.RandomizerManager;
import com.universalrandomizer.util.RandomizerLogger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * Randomizes which entity type actually spawns in place of the intended type.
 *
 * <p>Intercept strategy:
 * - Forge: {@code LivingSpawnEvent.CheckSpawn} cancels the original spawn and
 *   triggers a replacement entity of the mapped type.
 * - Fabric: {@code ServerEntityEvents.ENTITY_LOAD} or a Mixin on
 *   {@code SpawnPlacements#checkSpawnRules}.
 *
 * <p>The mapping is {@code entity_type RL → entity_type RL}.
 * Technical entities (item, XP orb, fishing bobber, player) are excluded from
 * the pool by {@link com.universalrandomizer.core.RegistryScanner}.
 */
public final class EntitySpawnRandomizer {

    private EntitySpawnRandomizer() {}

    /**
     * Returns the {@link EntityType} that should spawn in place of {@code intended}.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Entity> EntityType<T> applySpawn(EntityType<T> intended) {
        RandomizerManager mgr = RandomizerManager.getInstance();
        if (!mgr.isInitialized() || !mgr.isEnabled(RandomizerMode.ENTITY_SPAWNS)) return intended;

        ResourceLocation entityKey = BuiltInRegistries.ENTITY_TYPE.getKey(intended);
        if (entityKey == null) return intended;

        ResourceLocation targetKey = mgr.getEntitySpawn(entityKey);
        if (targetKey.equals(entityKey)) return intended;

        EntityType<?> targetType = BuiltInRegistries.ENTITY_TYPE.get(targetKey);
        RandomizerLogger.debug("EntitySpawn: {} -> {}", entityKey, targetKey);
        return (EntityType<T>) targetType;
    }
}
