package com.universalrandomizer.platform.fabric;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;

/**
 * Fabric implementation of {@link com.universalrandomizer.platform.PlatformHelper}.
 *
 * <p>Architectury's {@code @ExpectPlatform} transformer rewires calls from
 * {@code PlatformHelper.xxx()} to this class at runtime on Fabric.
 * The class MUST be in the {@code platform.fabric} sub-package to match
 * the Architectury naming convention.
 */
public final class PlatformHelperImpl {

    private PlatformHelperImpl() {}

    public static Path getWorldSaveDir(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT).toAbsolutePath();
    }

    public static boolean isForge() {
        return false;
    }

    public static String getPlatformName() {
        return "Fabric " + FabricLoader.getInstance()
            .getModContainer("fabricloader")
            .map(c -> c.getMetadata().getVersion().getFriendlyString())
            .orElse("?");
    }
}
