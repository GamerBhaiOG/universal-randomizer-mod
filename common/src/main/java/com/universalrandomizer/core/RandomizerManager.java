package com.universalrandomizer.core;

import com.universalrandomizer.config.ModeConfig;
import com.universalrandomizer.config.RandomizerConfig;
import com.universalrandomizer.config.RandomizerMode;
import com.universalrandomizer.persist.MappingSerializer;
import com.universalrandomizer.persist.PersistenceManager;
import com.universalrandomizer.util.RandomizerLogger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootDataType;

import java.util.*;

/**
 * Central orchestrator for Universal Randomizer.
 *
 * <p>Lifecycle:
 * <ol>
 *   <li>Server starts → {@link #initialize(MinecraftServer)} is called by the platform event handler.
 *   <li>On first world: settings.json is absent → the setup GUI is shown.
 *   <li>After config is confirmed → {@link #generateMappings()} is called.
 *   <li>All feature classes read from the shared {@link MappingTable}.
 *   <li>{@link #reset()} clears and regenerates; {@link #reloadDatapacks()} re-scans loot tables.
 * </ol>
 *
 * <p>This is a per-server singleton; it is reset each time a server stops.
 */
public class RandomizerManager {

    // ── Singleton ──────────────────────────────────────────────────────────────

    private static RandomizerManager INSTANCE = null;

    public static RandomizerManager getInstance() {
        if (INSTANCE == null) INSTANCE = new RandomizerManager();
        return INSTANCE;
    }

    /** Called on server stop to release all state. */
    public static void shutdown() {
        if (INSTANCE != null) {
            INSTANCE.table.clearAll();
            INSTANCE = null;
        }
    }

    // ──────────────────────────────────────────────────────────────────────────

    private RandomizerConfig config = new RandomizerConfig();
    private final MappingTable table = new MappingTable();
    private final RegistryScanner scanner = new RegistryScanner();
    private MinecraftServer server;
    private boolean initialized = false;
    private final Random sharedRng = new Random();

    private RandomizerManager() {}

    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Called once when the server/world finishes loading.
     * Loads persisted config, scans registries, and generates all mappings.
     */
    public void initialize(MinecraftServer server) {
        this.server = server;
        RandomizerLogger.info("RandomizerManager: initializing...");

        // 1. Load or create config from disk
        config = PersistenceManager.loadOrCreate(server);

        // 2. Propagate world seed to all modes
        long worldSeed = server.getWorldData().worldGenOptions().seed();
        config.applyWorldSeed(worldSeed);

        // 3. Scan all registries
        LootDataManager lootData = server.getLootData();
        scanner.scan(lootData);

        // 4. Generate and cache mappings
        generateMappings();

        initialized = true;
        RandomizerLogger.info("RandomizerManager: ready. {} total mappings generated.", table.totalGeneratedMappings());
    }

    /** Clears all generated mappings and regenerates. */
    public void reset() {
        RandomizerLogger.info("RandomizerManager: reset requested.");
        table.clearGenerated();
        generateMappings();
        PersistenceManager.save(server, config, table);
        RandomizerLogger.info("RandomizerManager: reset complete. {} mappings.", table.totalGeneratedMappings());
    }

    /** Re-scans loot tables after a datapack reload. */
    public void reloadDatapacks() {
        if (!initialized) return;
        RandomizerLogger.info("RandomizerManager: datapack reload — re-scanning loot tables.");
        scanner.scan(server.getLootData());
        generateChestLootMappings();
    }

    // ──────────────────────────────────────────────────────────────────────────

    private void generateMappings() {
        RandomizerLogger.debug("RandomizerManager: generating mappings for enabled modes...");
        table.clearGenerated();

        if (isEnabled(RandomizerMode.MINING_DROPS))     generateMiningMappings();
        if (isEnabled(RandomizerMode.MOB_DROPS))        generateMobMappings();
        if (isEnabled(RandomizerMode.CRAFTING))         generateCraftingMappings();
        if (isEnabled(RandomizerMode.SMELTING))         generateSmeltingMappings();
        if (isEnabled(RandomizerMode.FISHING_LOOT))     generateFishingMappings();
        if (isEnabled(RandomizerMode.ENTITY_SPAWNS))    generateEntitySpawnMappings();
        if (isEnabled(RandomizerMode.POTION_BREWING))   generatePotionMappings();
        if (isEnabled(RandomizerMode.CHEST_LOOT))       generateChestLootMappings();
        if (isEnabled(RandomizerMode.BLOCK_PLACEMENT))  generateBlockPlacementMappings();
        if (isEnabled(RandomizerMode.CROP_DROPS))       generateCropDropMappings();
        if (isEnabled(RandomizerMode.STRUCTURE_SPAWNS)) generateStructureSpawnMappings();
        if (isEnabled(RandomizerMode.WORLD_GEN))        generateWorldGenMappings();
    }

    // ── Per-mode generators ───────────────────────────────────────────────────

    private void generateMiningMappings() {
        List<ResourceLocation> itemPool = scanner.getItemPool();
        List<ResourceLocation> blockPool = scanner.getBlockPool();
        long seed = config.getEffectiveSeed(RandomizerMode.MINING_DROPS);
        Map<ResourceLocation, ResourceLocation> generated = generateCrossPool(blockPool, itemPool, seed);
        table.loadGenerated(table.getMiningDrops(), generated);
        RandomizerLogger.debug("Mining drops: {} mappings", generated.size());
    }

    private void generateMobMappings() {
        List<ResourceLocation> itemPool = scanner.getItemPool();
        List<ResourceLocation> entityPool = scanner.getEntityPool();
        long seed = config.getEffectiveSeed(RandomizerMode.MOB_DROPS);
        Map<ResourceLocation, ResourceLocation> generated = generateCrossPool(entityPool, itemPool, seed);
        table.loadGenerated(table.getMobDrops(), generated);
        RandomizerLogger.debug("Mob drops: {} mappings", generated.size());
    }

    private void generateCraftingMappings() {
        List<ResourceLocation> itemPool = scanner.getItemPool();
        long seed = config.getEffectiveSeed(RandomizerMode.CRAFTING);
        List<ResourceLocation> recipeIds = new ArrayList<>(
            server.getRecipeManager().getRecipes().stream()
                .map(r -> r.getId())
                .toList()
        );
        Collections.sort(recipeIds);
        Map<ResourceLocation, ResourceLocation> generated = generateCrossPool(recipeIds, itemPool, seed);
        table.loadGenerated(table.getCraftingOutputs(), generated);
        RandomizerLogger.debug("Crafting: {} recipe mappings", generated.size());
    }

    private void generateSmeltingMappings() {
        List<ResourceLocation> itemPool = scanner.getItemPool();
        long seed = config.getEffectiveSeed(RandomizerMode.SMELTING);
        Map<ResourceLocation, ResourceLocation> generated = MappingGenerator.generate(itemPool, seed);
        table.loadGenerated(table.getSmeltingOutputs(), generated);
        RandomizerLogger.debug("Smelting: {} mappings", generated.size());
    }

    private void generateFishingMappings() {
        List<ResourceLocation> itemPool = scanner.getItemPool();
        long seed = config.getEffectiveSeed(RandomizerMode.FISHING_LOOT);
        Map<ResourceLocation, ResourceLocation> generated = MappingGenerator.generate(itemPool, seed);
        table.loadGenerated(table.getFishingLoot(), generated);
        RandomizerLogger.debug("Fishing: {} mappings", generated.size());
    }

    private void generateEntitySpawnMappings() {
        List<ResourceLocation> entityPool = scanner.getEntityPool();
        long seed = config.getEffectiveSeed(RandomizerMode.ENTITY_SPAWNS);
        Map<ResourceLocation, ResourceLocation> generated = MappingGenerator.generate(entityPool, seed);
        table.loadGenerated(table.getEntitySpawns(), generated);
        RandomizerLogger.debug("Entity spawns: {} mappings", generated.size());
    }

    private void generatePotionMappings() {
        List<ResourceLocation> potionPool = scanner.getPotionPool();
        long seed = config.getEffectiveSeed(RandomizerMode.POTION_BREWING);
        Map<ResourceLocation, ResourceLocation> generated = MappingGenerator.generate(potionPool, seed);
        table.loadGenerated(table.getPotions(), generated);
        RandomizerLogger.debug("Potions: {} mappings", generated.size());
    }

    private void generateChestLootMappings() {
        List<ResourceLocation> chestTables = getLootTablesByPrefix("chests/");
        List<ResourceLocation> itemPool = scanner.getItemPool();
        long seed = config.getEffectiveSeed(RandomizerMode.CHEST_LOOT);
        Map<ResourceLocation, ResourceLocation> generated = generateCrossPool(chestTables, itemPool, seed);
        table.loadGenerated(table.getChestLoot(), generated);
        RandomizerLogger.debug("Chest loot: {} mappings", generated.size());
    }

    private void generateStructureSpawnMappings() {
        List<ResourceLocation> structures = new ArrayList<>(
            server.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE).keySet()
        );
        Collections.sort(structures);
        long seed = config.getEffectiveSeed(RandomizerMode.STRUCTURE_SPAWNS);
        Map<ResourceLocation, ResourceLocation> generated = MappingGenerator.generate(structures, seed);
        table.loadGenerated(table.getStructureSpawns(), generated);
        RandomizerLogger.debug("Structure spawns: {} mappings", generated.size());
    }

    private void generateWorldGenMappings() {
        List<ResourceLocation> blockPool = scanner.getBlockPool();
        long seed = config.getEffectiveSeed(RandomizerMode.WORLD_GEN);
        Map<ResourceLocation, ResourceLocation> generated = MappingGenerator.generate(blockPool, seed);
        table.loadGenerated(table.getWorldGen(), generated);
        RandomizerLogger.debug("World gen: {} mappings", generated.size());
    }

    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Creates a cross-pool mapping: sourcePool[i] → pick from targetPool using shuffled index.
     * When pools have different sizes, targets wrap or are trimmed.
     */
    private static Map<ResourceLocation, ResourceLocation> generateCrossPool(
            List<ResourceLocation> sourcePool,
            List<ResourceLocation> targetPool,
            long seed) {
        if (sourcePool.isEmpty() || targetPool.isEmpty()) return Collections.emptyMap();

        List<ResourceLocation> targets = new ArrayList<>(targetPool);
        Collections.shuffle(targets, new Random(seed));

        Map<ResourceLocation, ResourceLocation> result = new LinkedHashMap<>();
        for (int i = 0; i < sourcePool.size(); i++) {
            result.put(sourcePool.get(i), targets.get(i % targets.size()));
        }
        return result;
    }

    private List<ResourceLocation> getAllLootTableKeys() {
        LootDataManager loot = server.getLootData();
        List<ResourceLocation> keys = new ArrayList<>(loot.getKeys(LootDataType.TABLE));
        Collections.sort(keys);
        return keys;
    }

    private List<ResourceLocation> getLootTablesByPrefix(String prefix) {
        return getAllLootTableKeys().stream()
            .filter(rl -> rl.getPath().startsWith(prefix))
            .toList();
    }

    private void generateBlockPlacementMappings() {
        List<ResourceLocation> blockPool = scanner.getBlockPool();
        long seed = config.getEffectiveSeed(RandomizerMode.BLOCK_PLACEMENT);
        Map<ResourceLocation, ResourceLocation> generated = MappingGenerator.generate(blockPool, seed);
        table.loadGenerated(table.getBlockPlacements(), generated);
        RandomizerLogger.debug("Block placement: {} mappings", generated.size());
    }

    private void generateCropDropMappings() {
        List<ResourceLocation> itemPool = scanner.getItemPool();
        List<ResourceLocation> blockPool = scanner.getBlockPool();
        long seed = config.getEffectiveSeed(RandomizerMode.CROP_DROPS);
        Map<ResourceLocation, ResourceLocation> generated = generateCrossPool(blockPool, itemPool, seed);
        table.loadGenerated(table.getCropDrops(), generated);
        RandomizerLogger.debug("Crop drops: {} mappings", generated.size());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Public lookup API (used by features and RandomizerAPI)
    // ──────────────────────────────────────────────────────────────────────────

    public ResourceLocation getMiningDrop(ResourceLocation blockKey) {
        return table.lookup(table.getMiningDrops(), blockKey, sharedRng);
    }

    public ResourceLocation getMobDrop(ResourceLocation entityKey) {
        return table.lookup(table.getMobDrops(), entityKey, sharedRng);
    }

    public ResourceLocation getCraftingOutput(ResourceLocation recipeId) {
        return table.lookup(table.getCraftingOutputs(), recipeId, sharedRng);
    }

    public ResourceLocation getSmeltingOutput(ResourceLocation itemKey) {
        return table.lookup(table.getSmeltingOutputs(), itemKey, sharedRng);
    }

    public ResourceLocation getFishingLoot(ResourceLocation key) {
        return table.lookup(table.getFishingLoot(), key, sharedRng);
    }

    public ResourceLocation getEntitySpawn(ResourceLocation entityKey) {
        return table.lookup(table.getEntitySpawns(), entityKey, sharedRng);
    }

    public ResourceLocation getPotion(ResourceLocation potionKey) {
        return table.lookup(table.getPotions(), potionKey, sharedRng);
    }

    public ResourceLocation getChestLoot(ResourceLocation tableKey) {
        return table.lookup(table.getChestLoot(), tableKey, sharedRng);
    }

    public ResourceLocation getBlockPlacement(ResourceLocation blockKey) {
        return table.lookup(table.getBlockPlacements(), blockKey, sharedRng);
    }

    public ResourceLocation getCropDrop(ResourceLocation blockKey) {
        return table.lookup(table.getCropDrops(), blockKey, sharedRng);
    }

    public ResourceLocation getStructureSpawn(ResourceLocation key) {
        return table.lookup(table.getStructureSpawns(), key, sharedRng);
    }

    public ResourceLocation getWorldGen(ResourceLocation key) {
        return table.lookup(table.getWorldGen(), key, sharedRng);
    }

    // ──────────────────────────────────────────────────────────────────────────

    public boolean isEnabled(RandomizerMode mode) {
        return config.isEnabled(mode);
    }

    public RandomizerConfig getConfig()  { return config; }
    public MappingTable getTable()       { return table; }
    public RegistryScanner getScanner()  { return scanner; }
    public boolean isInitialized()       { return initialized; }
    public MinecraftServer getServer()   { return server; }
}
