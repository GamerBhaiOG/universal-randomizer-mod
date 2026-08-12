package com.universalrandomizer.platform.forge;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;

/**
 * Forge implementation of {@link com.universalrandomizer.platform.PlatformHelper}.
 *
 * <p>Architectury's {@code @ExpectPlatform} transformer rewires calls from
 * {@code PlatformHelper.xxx()} to this class at runtime on Forge.
 * The class MUST be in the {@code platform.forge} sub-package to match
 * the Architectury naming convention.
 */
public final class PlatformHelperImpl {

    private PlatformHelperImpl() {}

    public static Path getWorldSaveDir(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT).toAbsolutePath();
    }

    public static boolean isForge() {
        return true;
    }

    public static String getPlatformName() {
        return "NeoForge 1.20.4";
    }
}
