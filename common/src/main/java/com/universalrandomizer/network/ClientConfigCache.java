package com.universalrandomizer.network;

import com.universalrandomizer.config.RandomizerConfig;
import com.universalrandomizer.core.MappingTable;
import com.universalrandomizer.persist.MappingSerializer;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Client-side mirror of the server's randomizer config and mapping table.
 *
 * <p>Populated via sync packets on join. Used by GUI screens and recipe book hints.
 * Never executed on dedicated server — all accesses from common code must be
 * guarded by client-side environment checks.
 */
public final class ClientConfigCache {

    private ClientConfigCache() {}

    private static volatile RandomizerConfig clientConfig = null;
    private static volatile MappingTable clientTable = null;
    private static final AtomicBoolean setupScreenRequested = new AtomicBoolean(false);

    /** Updates the client config mirror from received JSON. */
    public static void updateConfig(String settingsJson) {
        clientConfig = MappingSerializer.deserializeConfig(settingsJson);
    }

    /** Updates the client mapping table mirror from received JSON. */
    public static void updateMapping(String mappingJson) {
        clientTable = new MappingTable();
        MappingSerializer.deserializeMappingInto(mappingJson, clientTable);
    }

    /** Signals that the setup screen should open (checked by the client tick handler). */
    public static void requestOpenSetupScreen() {
        setupScreenRequested.set(true);
    }

    /** Consumes the setup screen request flag (returns true if screen should open). */
    public static boolean pollSetupScreenRequest() {
        return setupScreenRequested.getAndSet(false);
    }

    public static RandomizerConfig getConfig() { return clientConfig; }
    public static MappingTable getTable()      { return clientTable; }
    public static boolean hasConfig()          { return clientConfig != null; }
}
