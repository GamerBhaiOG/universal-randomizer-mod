package com.universalrandomizer.mixin;

import com.universalrandomizer.config.RandomizerMode;
import com.universalrandomizer.core.RandomizerManager;
import com.universalrandomizer.features.EntitySpawnRandomizer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Universal Mixin on {@link ServerLevel} to intercept entity spawning on Fabric and Forge.
 */
@Mixin(ServerLevel.class)
public class ServerLevelSpawnMixin {

    @Unique
    private static final ThreadLocal<Boolean> IS_SPAWNING_ENTITY = ThreadLocal.withInitial(() -> false);

    @Inject(
        method = "addFreshEntity",
        at = @At("HEAD"),
        cancellable = true,
        remap = true,
        require = 0
    )
    private void universalRandomizer$onAddFreshEntity(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (IS_SPAWNING_ENTITY.get()) return;
        if (entity == null || !(entity instanceof Mob)) return;

        ServerLevel level = (ServerLevel)(Object)this;
        if (level.isClientSide()) return;

        RandomizerManager mgr = RandomizerManager.getInstance();
        if (!mgr.isInitialized() || !mgr.isEnabled(RandomizerMode.ENTITY_SPAWNS)) return;

        EntityType<?> intended = entity.getType();
        EntityType<?> randomized = EntitySpawnRandomizer.applySpawn(intended);

        if (!randomized.equals(intended)) {
            Entity replacement = randomized.create(level);
            if (replacement != null) {
                replacement.setPos(entity.getX(), entity.getY(), entity.getZ());
                replacement.setYRot(entity.getYRot());
                replacement.setXRot(entity.getXRot());
                cir.cancel();
                try {
                    IS_SPAWNING_ENTITY.set(true);
                    level.addFreshEntity(replacement);
                } finally {
                    IS_SPAWNING_ENTITY.set(false);
                }
            }
        }
    }
}
