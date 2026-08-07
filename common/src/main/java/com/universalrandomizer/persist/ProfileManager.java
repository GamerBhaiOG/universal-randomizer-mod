package com.universalrandomizer.persist;

import com.universalrandomizer.config.RandomizerConfig;
import com.universalrandomizer.platform.PlatformHelper;
import com.universalrandomizer.util.RandomizerLogger;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Manages named configuration profiles stored in {@code world/randomizer/profiles/}.
 *
 * <p>Each profile is a {@code settings.json}-compatible file:
 * {@code world/randomizer/profiles/<name>.json}
 *
 * <p>Built-in read-only profiles (Speedrun, Chaos, Lucky Block, Vanilla+, Hardcore, Streamer)
 * are baked in as static config factories.
 */
public final class ProfileManager {

    private ProfileManager() {}

    private static final String PROFILES_DIR = "randomizer/profiles";

    // ──────────────────────────────────────────────────────────────────────────

    /** Saves the current config as a named profile. */
    public static void save(MinecraftServer server, String name, RandomizerConfig config) {
        try {
            java.io.File dir = getDir(server);
            dir.mkdirs();
            java.io.File file = new java.io.File(dir, sanitize(name) + ".json");
            Files.writeString(file.toPath(), MappingSerializer.serializeConfig(config), StandardCharsets.UTF_8);
            RandomizerLogger.info("Profile saved: {}", name);
        } catch (IOException e) {
            RandomizerLogger.error("ProfileManager: failed to save profile '{}'.", name, e);
        }
    }

    /** Loads a named profile. Returns null if not found. */
    public static RandomizerConfig load(MinecraftServer server, String name) {
        // Check built-ins first
        RandomizerConfig builtin = loadBuiltin(name);
        if (builtin != null) return builtin;

        java.io.File file = new java.io.File(getDir(server), sanitize(name) + ".json");
        if (!file.exists()) {
            RandomizerLogger.warn("ProfileManager: profile '{}' not found.", name);
            return null;
        }
        try {
            String json = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            RandomizerLogger.info("Profile loaded: {}", name);
            return MappingSerializer.deserializeConfig(json);
        } catch (IOException e) {
            RandomizerLogger.error("ProfileManager: failed to load profile '{}'.", name, e);
            return null;
        }
    }

    /** Lists all available profiles (built-in + saved). */
    public static List<String> list(MinecraftServer server) {
        List<String> names = new ArrayList<>(BUILTIN_PROFILES);
        java.io.File dir = getDir(server);
        if (dir.exists()) {
            for (java.io.File f : Objects.requireNonNull(dir.listFiles())) {
                if (f.getName().endsWith(".json")) {
                    String n = f.getName().replace(".json", "");
                    if (!names.contains(n)) names.add(n);
                }
            }
        }
        return Collections.unmodifiableList(names);
    }

    /** Deletes a saved profile. Returns false if not found or is built-in. */
    public static boolean delete(MinecraftServer server, String name) {
        if (BUILTIN_PROFILES.contains(name)) return false;
        java.io.File file = new java.io.File(getDir(server), sanitize(name) + ".json");
        return file.exists() && file.delete();
    }

    /** Exports a profile as its raw JSON string (for /randomizer profile export). */
    public static String exportJson(MinecraftServer server, String name) {
        java.io.File file = new java.io.File(getDir(server), sanitize(name) + ".json");
        if (!file.exists()) return null;
        try {
            return Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    // ── Built-in profiles ──────────────────────────────────────────────────────

    private static final List<String> BUILTIN_PROFILES = List.of(
        "Default", "Chaos Mode", "Speedrun", "Lucky Block", "Survival Friendly", "World Craze");

    private static RandomizerConfig loadBuiltin(String name) {
        RandomizerConfig cfg = new RandomizerConfig();
        // Disable all modes first for explicit config mapping
        for (var mode : com.universalrandomizer.config.RandomizerMode.values()) {
            cfg.setEnabled(mode, false);
        }

        switch (name) {
            case "Default" -> {
                cfg.setEnabled(com.universalrandomizer.config.RandomizerMode.MINING_DROPS, true);
                cfg.setEnabled(com.universalrandomizer.config.RandomizerMode.MOB_DROPS, true);
                cfg.setEnabled(com.universalrandomizer.config.RandomizerMode.CHEST_LOOT, true);
                return cfg;
            }
            case "Chaos Mode", "Chaos" -> {
                for (var mode : com.universalrandomizer.config.RandomizerMode.values()) {
                    cfg.setEnabled(mode, true);
                }
                return cfg;
            }
            case "Speedrun" -> {
                cfg.setEnabled(com.universalrandomizer.config.RandomizerMode.MINING_DROPS, true);
                cfg.setEnabled(com.universalrandomizer.config.RandomizerMode.MOB_DROPS, true);
                cfg.setEnabled(com.universalrandomizer.config.RandomizerMode.CHEST_LOOT, true);
                cfg.setEnabled(com.universalrandomizer.config.RandomizerMode.CRAFTING, true);
                return cfg;
            }
            case "Lucky Block" -> {
                cfg.setEnabled(com.universalrandomizer.config.RandomizerMode.MINING_DROPS, true);
                cfg.setEnabled(com.universalrandomizer.config.RandomizerMode.CHEST_LOOT, true);
                return cfg;
            }
            case "Survival Friendly" -> {
                cfg.setEnabled(com.universalrandomizer.config.RandomizerMode.CROP_DROPS, true);
                cfg.setEnabled(com.universalrandomizer.config.RandomizerMode.FISHING_LOOT, true);
                cfg.setEnabled(com.universalrandomizer.config.RandomizerMode.SMELTING, true);
                return cfg;
            }
            case "World Craze" -> {
                cfg.setEnabled(com.universalrandomizer.config.RandomizerMode.BLOCK_PLACEMENT, true);
                cfg.setEnabled(com.universalrandomizer.config.RandomizerMode.ENTITY_SPAWNS, true);
                cfg.setEnabled(com.universalrandomizer.config.RandomizerMode.STRUCTURE_SPAWNS, true);
                cfg.setEnabled(com.universalrandomizer.config.RandomizerMode.WORLD_GEN, true);
                return cfg;
            }
            default -> { return null; }
        }
    }

    // ──────────────────────────────────────────────────────────────────────────

    private static java.io.File getDir(MinecraftServer server) {
        return PlatformHelper.getWorldSaveDir(server).resolve(PROFILES_DIR).toFile();
    }

    private static String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9_\\-\\+]", "_");
    }
}
