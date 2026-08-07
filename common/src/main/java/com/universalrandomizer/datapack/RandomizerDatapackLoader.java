package com.universalrandomizer.datapack;

import com.google.gson.*;
import com.universalrandomizer.core.MappingTable;
import com.universalrandomizer.core.RandomizerManager;
import com.universalrandomizer.core.WeightedEntry;
import com.universalrandomizer.util.RandomizerLogger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.*;

/**
 * Loads custom randomizer mappings from datapacks.
 *
 * <p>Files are located at:
 * {@code data/<namespace>/randomizer/mappings/<name>.json}
 *
 * <p>File schema:
 * <pre>{@code
 * {
 *   "type": "block",          // or "item", "entity", "loot_table"
 *   "source": "minecraft:stone",
 *   "target": "minecraft:diamond",
 *   "weight": 100             // optional; if present, creates a weighted entry
 * }
 * }</pre>
 *
 * <p>Custom datapack mappings take priority over generated mappings (same as
 * command-set custom mappings). Loaded/reloaded on datapack reload.
 */
public class RandomizerDatapackLoader extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().create();
    private static final String FOLDER = "randomizer/mappings";

    public RandomizerDatapackLoader() {
        super(GSON, FOLDER);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> objects,
                         ResourceManager resourceManager,
                         ProfilerFiller profiler) {
        RandomizerManager mgr = RandomizerManager.getInstance();
        if (!mgr.isInitialized()) return;

        MappingTable table = mgr.getTable();
        int loaded = 0;

        for (Map.Entry<ResourceLocation, JsonElement> entry : objects.entrySet()) {
            try {
                JsonObject obj = entry.getValue().getAsJsonObject();
                String type   = obj.has("type")   ? obj.get("type").getAsString()   : "item";
                String source = obj.has("source")  ? obj.get("source").getAsString() : null;
                String target = obj.has("target")  ? obj.get("target").getAsString() : null;
                int    weight = obj.has("weight")  ? obj.get("weight").getAsInt()    : 0;

                if (source == null || target == null) continue;

                ResourceLocation srcRL = new ResourceLocation(source);
                ResourceLocation tgtRL = new ResourceLocation(target);

                if (weight > 0) {
                    // Weighted entry — add to weighted mappings
                    List<WeightedEntry<ResourceLocation>> entries = new ArrayList<>(
                        table.getWeightedMappings().getOrDefault(srcRL, List.of()));
                    entries.removeIf(e -> e.key().equals(tgtRL));
                    entries.add(new WeightedEntry<>(tgtRL, weight));
                    table.putWeighted(srcRL, entries);
                } else {
                    // Direct custom override
                    table.putCustom(srcRL, tgtRL);
                }
                loaded++;
                RandomizerLogger.debug("Datapack mapping loaded: {} {} -> {}", type, srcRL, tgtRL);
            } catch (Exception e) {
                RandomizerLogger.warn("RandomizerDatapackLoader: failed to parse {}: {}",
                    entry.getKey(), e.getMessage());
            }
        }

        RandomizerLogger.info("RandomizerDatapackLoader: loaded {} mappings from datapacks.", loaded);
    }
}
