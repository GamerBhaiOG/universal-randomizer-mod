package com.universalrandomizer.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;

/**
 * Platform-specific utility methods abstracted via {@link ExpectPlatform}.
 * Implementations live in each loader's {@code platform} package.
 */
public final class PlatformHelper {

    private PlatformHelper() {}

    /**
     * Returns the save directory for the currently running world.
     * Used by {@link com.universalrandomizer.persist.PersistenceManager} to
     * locate {@code world/randomizer/}.
     */
    @ExpectPlatform
    public static Path getWorldSaveDir(MinecraftServer server) {
        throw new AssertionError("Platform implementation not found");
    }

    /**
     * Returns true when running on Forge, false on Fabric.
     * Used for conditional behavior where Architectury doesn't fully abstract.
     */
    @ExpectPlatform
    public static boolean isForge() {
        throw new AssertionError("Platform implementation not found");
    }

    /**
     * Returns the human-readable platform name (e.g. "Forge 47.2.0" or "Fabric 0.15.7").
     */
    @ExpectPlatform
    public static String getPlatformName() {
        throw new AssertionError("Platform implementation not found");
    }
}
