package com.universalrandomizer.client.network;

import com.universalrandomizer.client.gui.RandomizerHubScreen;
import com.universalrandomizer.network.ClientConfigCache;
import com.universalrandomizer.network.NetworkHandler;
import com.universalrandomizer.network.SyncMappingPacket;
import com.universalrandomizer.network.SyncSettingsPacket;
import com.universalrandomizer.util.RandomizerLogger;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;

/**
 * Registers client-side packet receivers for S→C sync packets and screen open requests.
 *
 * <p>Located in the {@code client} package so it is NEVER loaded on dedicated servers.
 */
public final class ClientNetworkHandler {

    private ClientNetworkHandler() {}

    /**
     * Registers all S→C packet receivers on the client.
     */
    public static void registerClientPackets() {
        // S→C: settings sync
        NetworkManager.registerReceiver(
            NetworkManager.Side.S2C,
            SyncSettingsPacket.ID,
            (buf, context) -> {
                SyncSettingsPacket packet = SyncSettingsPacket.decode(buf);
                context.queue(() -> ClientConfigCache.updateConfig(packet.settingsJson()));
            }
        );

        // S→C: mapping sync
        NetworkManager.registerReceiver(
            NetworkManager.Side.S2C,
            SyncMappingPacket.ID,
            (buf, context) -> {
                SyncMappingPacket packet = SyncMappingPacket.decode(buf);
                context.queue(() -> ClientConfigCache.updateMapping(packet.mappingJson()));
            }
        );

        // S→C: open setup screen request
        NetworkManager.registerReceiver(
            NetworkManager.Side.S2C,
            NetworkHandler.OPEN_SETUP_SCREEN_ID,
            (buf, context) -> context.queue(() -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc != null) {
                    mc.setScreen(new RandomizerHubScreen());
                }
            })
        );

        RandomizerLogger.debug("ClientNetworkHandler: client packets registered.");
    }
}
