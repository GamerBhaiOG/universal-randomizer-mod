package com.universalrandomizer.mixin;

import com.universalrandomizer.config.RandomizerMode;
import com.universalrandomizer.core.RandomizerManager;
import com.universalrandomizer.features.MobDropRandomizer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Universal Mixin on {@link Entity} to intercept item drops on mob death across Fabric and Forge.
 */
@Mixin(Entity.class)
public class LivingEntityDropMixin {

    @Unique
    private static final ThreadLocal<Boolean> IS_SPAWNING_ITEM = ThreadLocal.withInitial(() -> false);

    @Inject(
        method = "spawnAtLocation(Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;",
        at = @At("HEAD"),
        cancellable = true,
        remap = true,
        require = 0
    )
    private void universalRandomizer$onSpawnAtLocation(ItemStack stack, float offsetY, CallbackInfoReturnable<ItemEntity> cir) {
        if (IS_SPAWNING_ITEM.get()) return;

        Entity entity = (Entity)(Object)this;
        if (entity.level().isClientSide() || stack == null || stack.isEmpty()) return;
        if (!(entity instanceof LivingEntity living)) return;

        RandomizerManager mgr = RandomizerManager.getInstance();
        if (!mgr.isInitialized() || !mgr.isEnabled(RandomizerMode.MOB_DROPS)) return;

        ItemStack randomized = MobDropRandomizer.applyDrop(living.getType(), stack);
        if (!randomized.equals(stack)) {
            try {
                IS_SPAWNING_ITEM.set(true);
                cir.setReturnValue(entity.spawnAtLocation(randomized, offsetY));
            } finally {
                IS_SPAWNING_ITEM.set(false);
            }
        }
    }
}
