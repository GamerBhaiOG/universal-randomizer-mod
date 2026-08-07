package com.universalrandomizer;

import com.universalrandomizer.command.RandomizerCommand;
import com.universalrandomizer.network.NetworkHandler;
import com.universalrandomizer.util.RandomizerLogger;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Loader-agnostic mod initialization.
 */
public final class UniversalRandomizerCommon {

    public static final String MOD_ID = "universalrandomizer";
    public static final String MOD_NAME = "Universal Randomizer";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public static void init() {
        LOGGER.info("{} is initializing on {}...", MOD_NAME,
            com.universalrandomizer.platform.PlatformHelper.getPlatformName());

        // Register networking packets
        NetworkHandler.registerPackets();

        // Register commands
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) ->
            RandomizerCommand.register(dispatcher));

        // Client-side keybindings
        if (Platform.getEnv() == net.fabricmc.api.EnvType.CLIENT) {
            com.universalrandomizer.client.KeyBindingHandler.register();
        }

        // Server lifecycle hooks
        LifecycleEvent.SERVER_STARTED.register(server ->
            com.universalrandomizer.core.RandomizerManager.getInstance().initialize(server));

        LifecycleEvent.SERVER_STOPPED.register(server ->
            com.universalrandomizer.core.RandomizerManager.shutdown());

        // Sync settings to joining players and show welcome message
        PlayerEvent.PLAYER_JOIN.register(player -> {
            NetworkHandler.syncToPlayer(player);
            com.universalrandomizer.core.RandomizerManager mgr =
                com.universalrandomizer.core.RandomizerManager.getInstance();
            long activeCount = java.util.Arrays.stream(
                com.universalrandomizer.config.RandomizerMode.values())
                .filter(mgr::isEnabled).count();
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "§b§l[Universal Randomizer] §r§7is active! "
                + "§a" + activeCount + " mode(s) §7enabled. "
                + "Type §f/randomizer status §7or press §f[R] §7to open GUI."));
        });

        RandomizerLogger.info("{} initialized.", MOD_NAME);
    }
}
