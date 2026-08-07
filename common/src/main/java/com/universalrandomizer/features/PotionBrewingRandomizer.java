package com.universalrandomizer.features;

import com.universalrandomizer.config.RandomizerMode;
import com.universalrandomizer.core.RandomizerManager;
import com.universalrandomizer.util.RandomizerLogger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.alchemy.Potion;

/**
 * Randomizes potion brewing results.
 *
 * <p>Intercept strategy: a Mixin on {@code BrewingRecipeRegistry#getOutput}
 * intercepts the potion calculation and swaps the resulting {@link Potion}
 * type via the potion mapping table.
 *
 * <p>The mapping is {@code potion RL → potion RL}.
 * Empty and Water potions are excluded from the target pool but can be sources.
 */
public final class PotionBrewingRandomizer {

    private PotionBrewingRandomizer() {}

    /**
     * Returns the randomized {@link Potion} to use in place of {@code original}.
     */
    public static Potion applyPotion(Potion original) {
        RandomizerManager mgr = RandomizerManager.getInstance();
        if (!mgr.isInitialized() || !mgr.isEnabled(RandomizerMode.POTION_BREWING)) return original;

        ResourceLocation potionKey = BuiltInRegistries.POTION.getKey(original);
        if (potionKey == null) return original;

        ResourceLocation targetKey = mgr.getPotion(potionKey);
        if (targetKey.equals(potionKey)) return original;

        Potion targetPotion = BuiltInRegistries.POTION.get(targetKey);
        if (targetPotion == null) return original;

        RandomizerLogger.debug("PotionBrewing: {} -> {}", potionKey, targetKey);
        return targetPotion;
    }
}
