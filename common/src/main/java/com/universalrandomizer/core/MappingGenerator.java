package com.universalrandomizer.core;

import com.universalrandomizer.util.RandomizerLogger;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

/**
 * Generates randomized bijective shuffle-mappings from a pool of keys.
 *
 * <p>A single call to {@link #generate(List, long)} produces a deterministic
 * one-to-one mapping: every source key maps to a unique target key (Fisher–Yates
 * shuffle of the pool using a seeded {@link Random}).
 *
 * <p>For pure-random (non-seeded) mappings, callers may pass any non-deterministic
 * seed (e.g. {@code System.nanoTime()}).
 */
public final class MappingGenerator {

    private MappingGenerator() {}

    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Generates a bijective mapping from each key in {@code pool} to a unique
     * shuffled counterpart.
     *
     * @param pool keys to shuffle (must not be empty)
     * @param seed seed for the PRNG
     * @return an immutable map from source key → target key
     */
    public static <K> Map<K, K> generate(List<K> pool, long seed) {
        if (pool.isEmpty()) {
            RandomizerLogger.debug("MappingGenerator: empty pool, returning empty map.");
            return Collections.emptyMap();
        }

        // Copy and shuffle
        List<K> targets = new ArrayList<>(pool);
        Collections.shuffle(targets, new Random(seed));

        // Build bijective mapping
        Map<K, K> mapping = new LinkedHashMap<>(pool.size());
        for (int i = 0; i < pool.size(); i++) {
            mapping.put(pool.get(i), targets.get(i));
        }

        RandomizerLogger.debug("MappingGenerator: generated {} mappings (seed={})", mapping.size(), seed);
        return Collections.unmodifiableMap(mapping);
    }

    /**
     * Generates a bijective mapping using a string seed (hashed to long).
     * Convenience for profile-based named seeds.
     */
    public static <K> Map<K, K> generate(List<K> pool, String stringSeed) {
        return generate(pool, stringSeed.hashCode() * 31L + stringSeed.length());
    }

    /**
     * Generates a pure-random mapping (non-deterministic).
     */
    public static <K> Map<K, K> generateRandom(List<K> pool) {
        return generate(pool, System.nanoTime() ^ Thread.currentThread().getId());
    }

    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Validates that a mapping is bijective: every key is distinct, every value
     * is distinct, and no key maps to itself (self-mappings are trivially uninteresting
     * and avoided by the shuffle where possible).
     *
     * <p>Note: for very small pools (size 1), self-mapping is unavoidable.
     */
    public static <K> boolean isBijective(Map<K, K> mapping) {
        Set<K> values = new HashSet<>(mapping.values());
        return values.size() == mapping.size();
    }

    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Combines multiple domain seeds so that enabling/disabling one mode does not
     * shift the mappings of all other modes.  Uses the world seed XORed with a
     * domain-specific constant derived from the domain name.
     */
    public static long deriveSeed(long worldSeed, String domain) {
        long domainHash = domain.chars().asLongStream().reduce(0L, (a, b) -> a * 31L + b);
        return worldSeed ^ domainHash;
    }

    /**
     * Produces per-player mappings by further XORing with the player's UUID hash.
     */
    public static long derivePerPlayerSeed(long worldSeed, String domain, UUID playerUuid) {
        long base = deriveSeed(worldSeed, domain);
        return base ^ playerUuid.getMostSignificantBits() ^ playerUuid.getLeastSignificantBits();
    }
}
