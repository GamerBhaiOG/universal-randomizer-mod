package com.universalrandomizer.config;

import java.util.EnumMap;
import java.util.Map;

/**
 * Global configuration model for Universal Randomizer.
 * Controls enabled state, seeds, and randomization type for every mode.
 */
public class RandomizerConfig {

    private int schemaVersion = 1;
    private long globalSeed = 0L;
    private boolean debugMode = false;

    /** Per-mode settings. Populated with defaults for every mode on construction. */
    private final Map<RandomizerMode, ModeConfig> modes = new EnumMap<>(RandomizerMode.class);

    /**
     * Creates a config with all modes enabled by default for maximum fun.
     */
    public RandomizerConfig() {
        for (RandomizerMode mode : RandomizerMode.values()) {
            modes.put(mode, new ModeConfig());
            setEnabled(mode, mode == RandomizerMode.MINING_DROPS);
        }
    }

    public ModeConfig getMode(RandomizerMode mode) {
        return modes.computeIfAbsent(mode, k -> new ModeConfig());
    }

    public boolean isEnabled(RandomizerMode mode) {
        return getMode(mode).isEnabled();
    }

    public void setEnabled(RandomizerMode mode, boolean enabled) {
        getMode(mode).setEnabled(enabled);
    }

    public int getSchemaVersion() { return schemaVersion; }
    public void setSchemaVersion(int schemaVersion) { this.schemaVersion = schemaVersion; }

    public long getGlobalSeed() { return globalSeed; }
    public void setGlobalSeed(long globalSeed) { this.globalSeed = globalSeed; }
    public long getWorldSeed() { return globalSeed; }
    public void setWorldSeed(long seed) { this.globalSeed = seed; }

    public boolean isDebugMode() { return debugMode; }
    public boolean isDebug() { return debugMode; }
    public void setDebugMode(boolean debug) {
        this.debugMode = debug;
        com.universalrandomizer.util.RandomizerLogger.setDebugEnabled(debug);
    }
    public void setDebug(boolean debug) { setDebugMode(debug); }

    public void applyWorldSeed(long seed) {
        if (this.globalSeed == 0L) {
            this.globalSeed = seed;
        }
        for (ModeConfig mc : modes.values()) {
            if (mc.getSeed() == 0L) {
                mc.setSeed(seed);
            }
        }
    }

    public long getEffectiveSeed(RandomizerMode mode) {
        long modeSeed = getMode(mode).getSeed();
        return modeSeed != 0L ? modeSeed : (globalSeed != 0L ? globalSeed : 12345L);
    }

    public Map<RandomizerMode, ModeConfig> getModes() { return modes; }
    public Map<RandomizerMode, ModeConfig> getAllModes() { return modes; }
}
