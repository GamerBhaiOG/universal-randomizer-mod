package com.universalrandomizer.fabric;

import com.universalrandomizer.config.RandomizerMode;
import com.universalrandomizer.core.RandomizerManager;
import com.universalrandomizer.features.*;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

/**
 * Registers all Fabric-specific event callbacks for the randomizer.
 */
public class FabricEventHandler {

    public static void registerEvents() {
        registerLootTableEvents();
    }

    private static void registerLootTableEvents() {
        LootTableEvents.MODIFY.register((resourceManager, lootManager, id, tableBuilder, source) -> {
            RandomizerManager mgr = RandomizerManager.getInstance();
            if (!mgr.isInitialized()) return;

            String path = id.getPath();
            String namespace = id.getNamespace();

            // ── Crop Drops (blocks/<name>) ────────────────────────────────────
            if (mgr.isEnabled(RandomizerMode.CROP_DROPS) && path.startsWith("blocks/")) {
                String blockPath = path.substring("blocks/".length());
                ResourceLocation blockKey = new ResourceLocation(namespace, blockPath);
                ResourceLocation mappedItem = mgr.getCropDrop(blockKey);
                if (mappedItem == null || mappedItem.equals(blockKey)) {
                    mappedItem = mgr.getTable().lookup(
                        mgr.getTable().getMiningDrops(), blockKey, new java.util.Random());
                }

                if (mappedItem != null) {
                    tableBuilder.withPool(
                        LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1))
                            .add(LootItem.lootTableItem(BuiltInRegistries.ITEM.get(mappedItem)))
                    );
                }
            }

            // ── Mining Drops (blocks/<name>) ───────────────────────────────────
            if (mgr.isEnabled(RandomizerMode.MINING_DROPS) && path.startsWith("blocks/")) {
                String blockPath = path.substring("blocks/".length());
                ResourceLocation blockKey = new ResourceLocation(namespace, blockPath);
                ResourceLocation mappedItem = MiningDropRandomizer.getMappedKey(blockKey);

                if (mappedItem != null && !mappedItem.equals(blockKey)) {
                    tableBuilder.withPool(
                        LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1))
                            .add(LootItem.lootTableItem(BuiltInRegistries.ITEM.get(mappedItem)))
                    );
                }
            }

            // ── Mob Drops (entities/<name>) ────────────────────────────────────
            if (mgr.isEnabled(RandomizerMode.MOB_DROPS) && path.startsWith("entities/")) {
                String entityPath = path.substring("entities/".length());
                ResourceLocation entityKey = new ResourceLocation(namespace, entityPath);
                ResourceLocation mappedItem = MobDropRandomizer.getMappedKey(entityKey);

                if (mappedItem != null && !mappedItem.equals(entityKey)) {
                    tableBuilder.withPool(
                        LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1))
                            .add(LootItem.lootTableItem(BuiltInRegistries.ITEM.get(mappedItem)))
                    );
                }
            }

            // ── Fishing Loot (gameplay/fishing...) ────────────────────────────
            if (mgr.isEnabled(RandomizerMode.FISHING_LOOT) && path.startsWith("gameplay/fishing")) {
                ResourceLocation mappedItem = mgr.getTable().lookup(
                    mgr.getTable().getMiningDrops(), id, new java.util.Random());
                if (mappedItem != null) {
                    tableBuilder.withPool(
                        LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1))
                            .add(LootItem.lootTableItem(BuiltInRegistries.ITEM.get(mappedItem)))
                    );
                }
            }

            // ── Random Chest Loot (chests/<name>) ──────────────────────────────
            if (mgr.isEnabled(RandomizerMode.CHEST_LOOT) && (path.startsWith("chests/") || path.startsWith("dispensers/"))) {
                ResourceLocation mappedItem = mgr.getTable().lookup(
                    mgr.getTable().getMiningDrops(), id, new java.util.Random());
                if (mappedItem != null) {
                    tableBuilder.withPool(
                        LootPool.lootPool()
                            .setRolls(ConstantValue.exactly(1))
                            .add(LootItem.lootTableItem(BuiltInRegistries.ITEM.get(mappedItem)))
                    );
                }
            }
        });
    }
}
