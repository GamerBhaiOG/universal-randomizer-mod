package com.universalrandomizer.forge.glm;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.universalrandomizer.core.RandomizerManager;
import com.universalrandomizer.features.*;
import com.universalrandomizer.config.RandomizerMode;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * Forge Global Loot Modifier that intercepts ALL loot table rolls and applies
 * the appropriate randomizer feature.
 */
public class RandomizerLootModifier extends LootModifier {

    public static final Supplier<Codec<RandomizerLootModifier>> CODEC = Suppliers.memoize(() ->
        RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, RandomizerLootModifier::new)));

    public RandomizerLootModifier(net.minecraft.world.level.storage.loot.predicates.LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @NotNull
    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        RandomizerManager mgr = RandomizerManager.getInstance();
        if (!mgr.isInitialized()) return generatedLoot;

        ResourceLocation lootTableId = context.getQueriedLootTableId();
        if (lootTableId == null) return generatedLoot;

        String path = lootTableId.getPath();

        // ── Crop Drops ────────────────────────────────────────────────────────
        if (mgr.isEnabled(RandomizerMode.CROP_DROPS) && path.startsWith("blocks/")) {
            if (context.hasParam(LootContextParams.BLOCK_STATE)) {
                var blockState = context.getParam(LootContextParams.BLOCK_STATE);
                if (CropDropRandomizer.isCropBlock(blockState.getBlock())) {
                    return transformLoot(generatedLoot,
                        stack -> CropDropRandomizer.applyDrop(blockState.getBlock(), stack));
                }
            }
        }

        // ── Mining Drops ──────────────────────────────────────────────────────
        if (mgr.isEnabled(RandomizerMode.MINING_DROPS) && path.startsWith("blocks/")) {
            if (context.hasParam(LootContextParams.BLOCK_STATE)) {
                var blockState = context.getParam(LootContextParams.BLOCK_STATE);
                return transformLoot(generatedLoot,
                    stack -> MiningDropRandomizer.applyDrop(blockState.getBlock(), stack));
            }
        }

        // ── Mob Drops ─────────────────────────────────────────────────────────
        if (mgr.isEnabled(RandomizerMode.MOB_DROPS) && path.startsWith("entities/")) {
            if (context.hasParam(LootContextParams.THIS_ENTITY)) {
                var entity = context.getParam(LootContextParams.THIS_ENTITY);
                return transformLoot(generatedLoot,
                    stack -> MobDropRandomizer.applyDrop(entity.getType(), stack));
            }
        }

        // ── Fishing ───────────────────────────────────────────────────────────
        if (mgr.isEnabled(RandomizerMode.FISHING_LOOT) && path.startsWith("gameplay/fishing")) {
            return transformLoot(generatedLoot, FishingRandomizer::randomizeFishingItem);
        }

        // ── Chest / Structure Loot ────────────────────────────────────────────
        if (mgr.isEnabled(RandomizerMode.CHEST_LOOT)
                && (path.startsWith("chests/") || path.startsWith("dispensers/") || path.startsWith("archaeology/"))) {
            return transformLoot(generatedLoot, ChestLootRandomizer::randomizeChestItem);
        }

        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }

    // ──────────────────────────────────────────────────────────────────────────

    /** Applies a per-stack transform function to the loot list. */
    private static ObjectArrayList<ItemStack> transformLoot(
            ObjectArrayList<ItemStack> loot,
            java.util.function.UnaryOperator<ItemStack> transform) {
        ObjectArrayList<ItemStack> result = new ObjectArrayList<>(loot.size());
        for (ItemStack stack : loot) {
            result.add(transform.apply(stack));
        }
        return result;
    }
}
