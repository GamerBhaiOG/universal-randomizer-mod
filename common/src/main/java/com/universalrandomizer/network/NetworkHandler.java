package com.universalrandomizer.network;

import com.universalrandomizer.core.RandomizerManager;
import com.universalrandomizer.persist.MappingSerializer;
import com.universalrandomizer.util.RandomizerLogger;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

/**
 * Registers and manages all network packets for Universal Randomizer.
 *
 * <p>Packets are registered during common init via {@link #registerPackets()}.
 * Sending happens:
 * <ul>
 *   <li>On player join: {@link #syncToPlayer(ServerPlayer)}</li>
 *   <li>After reset/config change: {@link #syncToAll(net.minecraft.server.MinecraftServer)}</li>
 * </ul>
 *
 * <p>Client-side handling updates the local config mirror for GUI display.
 */
public final class NetworkHandler {

    private NetworkHandler() {}

    public static final net.minecraft.resources.ResourceLocation OPEN_SETUP_SCREEN_ID =
        new net.minecraft.resources.ResourceLocation("universalrandomizer", "open_setup");

    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Sends settings and mapping packets to a single player (on join).
     */
    public static void syncToPlayer(ServerPlayer player) {
        RandomizerManager mgr = RandomizerManager.getInstance();
        if (!mgr.isInitialized()) return;

        // Settings packet
        String settingsJson = MappingSerializer.serializeConfig(mgr.getConfig());
        FriendlyByteBuf settingsBuf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        new SyncSettingsPacket(settingsJson).encode(settingsBuf);
        NetworkManager.sendToPlayer(player, SyncSettingsPacket.ID, settingsBuf);

        // Mapping packet
        String mappingJson = MappingSerializer.serializeMapping(mgr.getTable());
        FriendlyByteBuf mappingBuf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        new SyncMappingPacket(mappingJson).encode(mappingBuf);
        NetworkManager.sendToPlayer(player, SyncMappingPacket.ID, mappingBuf);

        RandomizerLogger.debug("NetworkHandler: synced to player {}", player.getName().getString());
    }

    /**
     * Broadcasts updated settings and mapping to all connected players.
     */
    public static void syncToAll(net.minecraft.server.MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            syncToPlayer(player);
        }
    }

    /**
     * Sends an "open setup screen" packet to the specified player
     * (called by /randomizer setup).
     */
    public static void sendSetupScreen(ServerPlayer player) {
        FriendlyByteBuf buf = new FriendlyByteBuf(io.netty.buffer.Unpooled.buffer());
        NetworkManager.sendToPlayer(player, OPEN_SETUP_SCREEN_ID, buf);
    }
}
