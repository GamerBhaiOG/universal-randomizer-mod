package com.universalrandomizer.mixin;

import com.universalrandomizer.config.RandomizerMode;
import com.universalrandomizer.core.RandomizerManager;
import com.universalrandomizer.features.EntitySpawnRandomizer;
import com.universalrandomizer.util.RandomizerLogger;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Universal Mixin on {@link ServerLevel} to intercept entity spawning safely on Fabric and Forge.
 */
@Mixin(ServerLevel.class)
public class ServerLevelSpawnMixin {

    @Unique
    private static final ThreadLocal<Boolean> IS_SPAWNING_ENTITY = ThreadLocal.withInitial(() -> false);

    @Unique
    private static boolean universalRandomizer$isWaterCapable(EntityType<?> type) {
        MobCategory category = type.getCategory();
        if (category == MobCategory.WATER_CREATURE
                || category == MobCategory.UNDERGROUND_WATER_CREATURE
                || category == MobCategory.WATER_AMBIENT
                || category == MobCategory.AXOLOTLS) {
            return true;
        }
        return type == EntityType.GUARDIAN
            || type == EntityType.ELDER_GUARDIAN
            || type == EntityType.TURTLE
            || type == EntityType.FROG
            || type == EntityType.DROWNED;
    }

    @Inject(
        method = "addFreshEntity",
        at = @At("HEAD"),
        cancellable = true,
        remap = true,
        require = 0
    )
    private void universalRandomizer$onAddFreshEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (IS_SPAWNING_ENTITY.get()) return;
        if (entity == null || !(entity instanceof Mob) || entity instanceof Projectile) return;

        String className = entity.getClass().getName().toLowerCase();
        if (className.contains("projectile") || className.contains("bullet") || className.contains("item")) return;

        ServerLevel level = (ServerLevel)(Object)this;
        if (level.isClientSide()) return;

        RandomizerManager mgr = RandomizerManager.getInstance();
        if (!mgr.isInitialized() || !mgr.isEnabled(RandomizerMode.ENTITY_SPAWNS)) return;

        // 1. Proximity Density Hard Cap: prevent lag/crash from entity overcrowding
        AABB area = entity.getBoundingBox().inflate(12.0);
        int nearbyCount = level.getEntitiesOfClass(Mob.class, area, e -> e != null && e.isAlive()).size();
        if (nearbyCount > 16) return;

        EntityType<?> intended = entity.getType();
        EntityType<?> randomized = EntitySpawnRandomizer.applySpawn(intended);

        // 2. Underwater Safeguard: prevent non-aquatic mobs from drowning underwater and looping NaturalSpawner
        BlockPos pos = entity.blockPosition();
        boolean isUnderwater = level.getFluidState(pos).is(FluidTags.WATER) || level.getBlockState(pos).is(Blocks.WATER);
        if (isUnderwater && !universalRandomizer$isWaterCapable(randomized)) return;

        if (!randomized.equals(intended)) {
            try {
                IS_SPAWNING_ENTITY.set(true);
                Entity replacement = randomized.create(level);
                if (replacement != null && replacement instanceof Mob) {
                    replacement.setPos(entity.getX(), entity.getY(), entity.getZ());
                    replacement.setYRot(entity.getYRot());
                    replacement.setXRot(entity.getXRot());
                    if (level.addFreshEntity(replacement)) {
                        cir.cancel();
                    }
                }
            } catch (Throwable t) {
                RandomizerLogger.debug("Entity spawn replacement skipped for {}: {}", intended, t.getMessage());
            } finally {
                IS_SPAWNING_ENTITY.set(false);
            }
        }
    }
}

