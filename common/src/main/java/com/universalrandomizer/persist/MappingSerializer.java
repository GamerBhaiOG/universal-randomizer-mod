package com.universalrandomizer.persist;

import com.google.gson.*;
import com.universalrandomizer.config.ModeConfig;
import com.universalrandomizer.config.RandomizerConfig;
import com.universalrandomizer.config.RandomizerMode;
import com.universalrandomizer.core.MappingTable;
import com.universalrandomizer.core.WeightedEntry;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

/**
 * Serializes and deserializes {@link RandomizerConfig} and {@link MappingTable}
 * to/from Gson-based JSON.
 *
 * <p>Schema version {@code 1} format is documented in the implementation plan.
 */
public final class MappingSerializer {

    private MappingSerializer() {}

    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .create();

    // ── Config serialization ───────────────────────────────────────────────────

    public static String serializeConfig(RandomizerConfig config) {
        JsonObject root = new JsonObject();
        root.addProperty("schemaVersion", config.getSchemaVersion());
        root.addProperty("worldSeed", config.getWorldSeed());
        root.addProperty("debugMode", config.isDebugMode());

        JsonObject modes = new JsonObject();
        for (Map.Entry<RandomizerMode, ModeConfig> entry : config.getModes().entrySet()) {
            JsonObject modeObj = new JsonObject();
            modeObj.addProperty("enabled", entry.getValue().isEnabled());
            modeObj.addProperty("type", entry.getValue().getRandomType().name());
            modeObj.addProperty("seed", entry.getValue().getSeed());
            modes.add(entry.getKey().getId(), modeObj);
        }
        root.add("modes", modes);

        return GSON.toJson(root);
    }

    public static RandomizerConfig deserializeConfig(String json) {
        RandomizerConfig config = new RandomizerConfig();
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            if (root.has("worldSeed"))  config.setWorldSeed(root.get("worldSeed").getAsLong());
            if (root.has("debugMode")) config.setDebugMode(root.get("debugMode").getAsBoolean());

            if (root.has("modes")) {
                JsonObject modes = root.getAsJsonObject("modes");
                for (Map.Entry<String, JsonElement> entry : modes.entrySet()) {
                    RandomizerMode mode = RandomizerMode.fromId(entry.getKey());
                    if (mode == null) continue;
                    JsonObject modeObj = entry.getValue().getAsJsonObject();
                    ModeConfig mc = config.getMode(mode);
                    if (modeObj.has("enabled")) mc.setEnabled(modeObj.get("enabled").getAsBoolean());
                    if (modeObj.has("type")) {
                        try { mc.setRandomType(ModeConfig.RandomType.valueOf(modeObj.get("type").getAsString())); }
                        catch (IllegalArgumentException ignored) {}
                    }
                    if (modeObj.has("seed")) mc.setSeed(modeObj.get("seed").getAsLong());
                }
            }
        } catch (Exception e) {
            // Corrupt JSON — return defaults
        }
        return config;
    }

    // ── Mapping table serialization ────────────────────────────────────────────

    public static String serializeMapping(MappingTable table) {
        JsonObject root = new JsonObject();

        root.add("miningDrops",      serializeMap(table.getMiningDrops()));
        root.add("mobDrops",         serializeMap(table.getMobDrops()));
        root.add("craftingOutputs",  serializeMap(table.getCraftingOutputs()));
        root.add("smeltingOutputs",  serializeMap(table.getSmeltingOutputs()));
        root.add("potions",          serializeMap(table.getPotions()));
        root.add("entitySpawns",     serializeMap(table.getEntitySpawns()));
        root.add("chestLoot",        serializeMap(table.getChestLoot()));
        root.add("blockPlacements",  serializeMap(table.getBlockPlacements()));
        root.add("cropDrops",        serializeMap(table.getCropDrops()));
        root.add("structureSpawns",  serializeMap(table.getStructureSpawns()));
        root.add("customMappings",   serializeMap(table.getCustomMappings()));

        // Weighted mappings
        JsonObject weighted = new JsonObject();
        for (Map.Entry<ResourceLocation, List<WeightedEntry<ResourceLocation>>> entry :
                table.getWeightedMappings().entrySet()) {
            JsonArray arr = new JsonArray();
            for (WeightedEntry<ResourceLocation> we : entry.getValue()) {
                JsonObject obj = new JsonObject();
                obj.addProperty("item", we.key().toString());
                obj.addProperty("weight", we.weight());
                arr.add(obj);
            }
            weighted.add(entry.getKey().toString(), arr);
        }
        root.add("weightedMappings", weighted);

        return GSON.toJson(root);
    }

    public static void deserializeMappingInto(String json, MappingTable table) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            loadMap(root, "miningDrops",     table.getMiningDrops());
            loadMap(root, "mobDrops",        table.getMobDrops());
            loadMap(root, "craftingOutputs", table.getCraftingOutputs());
            loadMap(root, "smeltingOutputs", table.getSmeltingOutputs());
            loadMap(root, "potions",         table.getPotions());
            loadMap(root, "entitySpawns",    table.getEntitySpawns());
            loadMap(root, "chestLoot",       table.getChestLoot());
            loadMap(root, "blockPlacements", table.getBlockPlacements());
            loadMap(root, "cropDrops",       table.getCropDrops());
            loadMap(root, "structureSpawns", table.getStructureSpawns());
            loadMap(root, "customMappings",  table.getCustomMappings());

            if (root.has("weightedMappings")) {
                JsonObject weighted = root.getAsJsonObject("weightedMappings");
                for (Map.Entry<String, JsonElement> entry : weighted.entrySet()) {
                    ResourceLocation source = new ResourceLocation(entry.getKey());
                    List<WeightedEntry<ResourceLocation>> entries = new ArrayList<>();
                    for (JsonElement el : entry.getValue().getAsJsonArray()) {
                        JsonObject obj = el.getAsJsonObject();
                        entries.add(new WeightedEntry<>(
                            new ResourceLocation(obj.get("item").getAsString()),
                            obj.get("weight").getAsInt()
                        ));
                    }
                    table.putWeighted(source, entries);
                }
            }
        } catch (Exception e) {
            // Partial load OK — just skip corrupt entries
        }
    }

    // ──────────────────────────────────────────────────────────────────────────

    private static JsonObject serializeMap(Map<ResourceLocation, ResourceLocation> map) {
        JsonObject obj = new JsonObject();
        for (Map.Entry<ResourceLocation, ResourceLocation> entry : map.entrySet()) {
            obj.addProperty(entry.getKey().toString(), entry.getValue().toString());
        }
        return obj;
    }

    private static void loadMap(JsonObject root, String key, Map<ResourceLocation, ResourceLocation> target) {
        if (!root.has(key)) return;
        JsonObject obj = root.getAsJsonObject(key);
        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            try {
                target.put(new ResourceLocation(entry.getKey()),
                           new ResourceLocation(entry.getValue().getAsString()));
            } catch (Exception ignored) {}
        }
    }
}
