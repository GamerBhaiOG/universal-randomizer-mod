package com.universalrandomizer.config;

/**
 * Per-mode configuration holding the enabled state, randomization strategy,
 * and an optional seed value.
 *
 * <p>Instances are serialized to/from JSON by {@link com.universalrandomizer.persist.MappingSerializer}.
 */
public class ModeConfig {

    /**
     * Determines how randomization mappings are generated for this mode.
     */
    public enum RandomType {
        /** Pure RNG, regenerated each world-load unless cached. */
        PURE_RANDOM,
        /** Seeded RNG — deterministic from {@link ModeConfig#seed}. */
        SEED_BASED,
        /** Each player gets their own mapping keyed by UUID. */
        PER_PLAYER,
        /** One shared mapping for the whole world, regenerated per world. */
        PER_WORLD,
        /** One shared mapping for all players on a server (default for MP). */
        SHARED_MULTIPLAYER
    }

    // ──────────────────────────────────────────────────────────────────────────

    /** Whether this mode is active. */
    private boolean enabled;

    /** How the mapping is generated. */
    private RandomType randomType;

    /**
     * Seed used when {@link RandomType#SEED_BASED}. Defaults to the world seed
     * (supplied by {@link com.universalrandomizer.core.RandomizerManager} on init).
     */
    private long seed;

    // ──────────────────────────────────────────────────────────────────────────

    /** Default constructor — mode disabled, seed-based with seed 0. */
    public ModeConfig() {
        this.enabled = false;
        this.randomType = RandomType.SEED_BASED;
        this.seed = 0L;
    }

    /** Convenience factory for a fully enabled, seed-based mode. */
    public static ModeConfig enabledSeed(long seed) {
        ModeConfig cfg = new ModeConfig();
        cfg.enabled = true;
        cfg.randomType = RandomType.SEED_BASED;
        cfg.seed = seed;
        return cfg;
    }

    /** Convenience factory for pure-random enabled mode. */
    public static ModeConfig enabledPure() {
        ModeConfig cfg = new ModeConfig();
        cfg.enabled = true;
        cfg.randomType = RandomType.PURE_RANDOM;
        return cfg;
    }

    // ──────────────────────────────────────────────────────────────────────────

    public boolean isEnabled()                  { return enabled; }
    public void setEnabled(boolean enabled)     { this.enabled = enabled; }

    public RandomType getRandomType()                   { return randomType; }
    public void setRandomType(RandomType randomType)    { this.randomType = randomType; }

    public long getSeed()                       { return seed; }
    public void setSeed(long seed)              { this.seed = seed; }

    /** Returns true if this mode uses a seeded RNG (deterministic). */
    public boolean isSeeded() {
        return randomType == RandomType.SEED_BASED;
    }

    /** Returns true if each player has an independent mapping. */
    public boolean isPerPlayer() {
        return randomType == RandomType.PER_PLAYER;
    }

    @Override
    public String toString() {
        return "ModeConfig{enabled=" + enabled + ", type=" + randomType + ", seed=" + seed + "}";
    }
}
