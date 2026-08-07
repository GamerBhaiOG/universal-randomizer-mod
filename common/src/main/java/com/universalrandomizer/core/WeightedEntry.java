package com.universalrandomizer.core;

import java.util.List;

/**
 * Represents a single weighted mapping entry: a target key and its relative weight.
 *
 * <p>Used by {@link MappingTable} to store distributions where one source maps to
 * multiple possible targets with different probabilities.
 *
 * <p>Example: {@code stone → [dirt(70), diamond(20), netherite_ingot(10)]}.
 *
 * @param <K> the registry key type (typically {@link net.minecraft.resources.ResourceLocation})
 */
public record WeightedEntry<K>(K key, int weight) {

    /**
     * Given an ordered list of weighted entries whose weights sum to at least 1,
     * selects a key by rolling {@code roll} against cumulative weights.
     *
     * @param entries ordered list of entries
     * @param roll    a random integer in [0, totalWeight)
     * @return the selected key, or the last entry's key as fallback
     */
    public static <K> K select(List<WeightedEntry<K>> entries, int roll) {
        int cumulative = 0;
        for (WeightedEntry<K> entry : entries) {
            cumulative += entry.weight();
            if (roll < cumulative) return entry.key();
        }
        // Fallback: return last entry (handles rounding errors)
        return entries.get(entries.size() - 1).key();
    }

    /**
     * Computes the total weight of a list of entries.
     */
    public static <K> int totalWeight(List<WeightedEntry<K>> entries) {
        return entries.stream().mapToInt(WeightedEntry::weight).sum();
    }
}
