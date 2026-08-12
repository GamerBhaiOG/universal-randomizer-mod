package com.universalrandomizer.client.network;

import com.universalrandomizer.client.gui.RandomizerHubScreen;
import com.universalrandomizer.network.ClientConfigCache;
import com.universalrandomizer.network.SyncMappingPacket;
import com.universalrandomizer.network.SyncSettingsPacket;
import net.minecraft.client.Minecraft;

/**
 * Isolated client-only network handler.
 * Prevents client classes (like Minecraft or Screen) from being loaded
 * by the JVM on dedicated servers.
 */
public final class ClientNetworkHandler {

    private ClientNetworkHandler() {}

    public static void handleSettingsOnClient(SyncSettingsPacket packet) {
        ClientConfigCache.updateConfig(packet.settingsJson());
    }

    public static void handleMappingOnClient(SyncMappingPacket packet) {
        ClientConfigCache.updateMapping(packet.mappingJson());
    }

    public static void openSetupScreenOnClient() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            mc.execute(() -> mc.setScreen(new RandomizerHubScreen()));
        }
    }
}
