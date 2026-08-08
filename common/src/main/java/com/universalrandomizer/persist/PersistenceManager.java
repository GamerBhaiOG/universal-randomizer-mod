package com.universalrandomizer.persist;

import com.universalrandomizer.config.RandomizerConfig;
import com.universalrandomizer.core.MappingTable;
import com.universalrandomizer.platform.PlatformHelper;
import com.universalrandomizer.util.RandomizerLogger;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/**
 * Manages reading and writing {@link RandomizerConfig} and {@link MappingTable}
 * to the world's {@code randomizer/} directory.
 *
 * <p>Files:
 * <ul>
 *   <li>{@code world/randomizer/settings.json} — mode config, seeds, custom mappings</li>
 *   <li>{@code world/randomizer/mapping.json}  — full generated mapping snapshot</li>
 * </ul>
 */
public final class PersistenceManager {

    private PersistenceManager() {}

    private static final String DIR     = "randomizer";
    private static final String SETTINGS = "settings.json";
    private static final String MAPPING  = "mapping.json";

    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Returns true if this world has never had the randomizer configured
     * (settings.json is absent — triggers the first-launch GUI).
     */
    public static boolean isFirstLaunch(MinecraftServer server) {
        return !getFile(server, SETTINGS).exists();
    }

    /**
     * Loads the config from disk, or returns a fresh default config if not found.
     */
    public static RandomizerConfig loadOrCreate(MinecraftServer server) {
        java.io.File file = getFile(server, SETTINGS);
        if (!file.exists()) {
            RandomizerLogger.info("PersistenceManager: no settings.json found — returning defaults.");
            return new RandomizerConfig();
        }
        try {
            String json = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            RandomizerConfig config = MappingSerializer.deserializeConfig(json);
            RandomizerLogger.info("PersistenceManager: loaded settings from {}", file.getAbsolutePath());
            return config;
        } catch (IOException e) {
            RandomizerLogger.error("PersistenceManager: failed to read settings.json — using defaults.", e);
            return new RandomizerConfig();
        }
    }

    /**
     * Loads a previously generated mapping into the table from {@code mapping.json}.
     * Returns false if no mapping file exists.
     */
    public static boolean loadMapping(MinecraftServer server, MappingTable table) {
        java.io.File file = getFile(server, MAPPING);
        if (!file.exists()) return false;
        try {
            String json = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            MappingSerializer.deserializeMappingInto(json, table);
            RandomizerLogger.info("PersistenceManager: loaded mapping.json ({} chars)", json.length());
            return true;
        } catch (IOException e) {
            RandomizerLogger.error("PersistenceManager: failed to read mapping.json.", e);
            return false;
        }
    }

    /**
     * Saves both config and mapping table to disk.
     */
    public static void save(MinecraftServer server, RandomizerConfig config, MappingTable table) {
        try {
            java.io.File dir = getDir(server);
            dir.mkdirs();

            // Write settings.json
            java.io.File settingsFile = new java.io.File(dir, SETTINGS);
            Files.writeString(settingsFile.toPath(),
                MappingSerializer.serializeConfig(config), StandardCharsets.UTF_8);

            // Write mapping.json
            java.io.File mappingFile = new java.io.File(dir, MAPPING);
            Files.writeString(mappingFile.toPath(),
                MappingSerializer.serializeMapping(table), StandardCharsets.UTF_8);

            RandomizerLogger.info("PersistenceManager: saved settings + mapping to {}", dir.getAbsolutePath());
        } catch (IOException e) {
            RandomizerLogger.error("PersistenceManager: failed to save.", e);
        }
    }

    /** Saves only the config (for live command updates). */
    public static void saveConfig(MinecraftServer server, RandomizerConfig config) {
        try {
            java.io.File dir = getDir(server);
            dir.mkdirs();
            Files.writeString(new java.io.File(dir, SETTINGS).toPath(),
                MappingSerializer.serializeConfig(config), StandardCharsets.UTF_8);
        } catch (IOException e) {
            RandomizerLogger.error("PersistenceManager: failed to save config.", e);
        }
    }

    // ──────────────────────────────────────────────────────────────────────────

    private static java.io.File getDir(MinecraftServer server) {
        return PlatformHelper.getWorldSaveDir(server).resolve(DIR).toFile();
    }

    private static java.io.File getFile(MinecraftServer server, String name) {
        return new java.io.File(getDir(server), name);
    }
}
