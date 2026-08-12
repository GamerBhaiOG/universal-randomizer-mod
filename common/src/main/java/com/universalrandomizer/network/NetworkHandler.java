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

    // ── Packet IDs ─────────────────────────────────────────────────────────────

    // These are declared on the packet records themselves

    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Registers all S→C packet receivers on the client.
     * Call during common initialization (both sides).
     */
    public static void registerPackets() {
        // S→C: settings sync
        NetworkManager.registerReceiver(
            NetworkManager.Side.S2C,
            SyncSettingsPacket.ID,
            (buf, context) -> {
                SyncSettingsPacket packet = SyncSettingsPacket.decode(buf);
                context.queue(() -> handleSettingsOnClient(packet));
            }
        );

        // S→C: mapping sync
        NetworkManager.registerReceiver(
            NetworkManager.Side.S2C,
            SyncMappingPacket.ID,
            (buf, context) -> {
                SyncMappingPacket packet = SyncMappingPacket.decode(buf);
                context.queue(() -> handleMappingOnClient(packet));
            }
        );

        // S→C: open setup screen request
        NetworkManager.registerReceiver(
            NetworkManager.Side.S2C,
            OPEN_SETUP_SCREEN_ID,
            (buf, context) -> context.queue(NetworkHandler::openSetupScreenOnClient)
        );

        RandomizerLogger.debug("NetworkHandler: packets registered.");
    }

    private static final net.minecraft.resources.ResourceLocation OPEN_SETUP_SCREEN_ID =
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

    // ── Client-side handlers ───────────────────────────────────────────────────

    /** Client: receives and stores the config mirror. */
    private static void handleSettingsOnClient(SyncSettingsPacket packet) {
        com.universalrandomizer.client.network.ClientNetworkHandler.handleSettingsOnClient(packet);
        RandomizerLogger.debug("NetworkHandler: received settings packet ({} chars)", packet.settingsJson().length());
    }

    /** Client: receives and stores the mapping mirror. */
    private static void handleMappingOnClient(SyncMappingPacket packet) {
        com.universalrandomizer.client.network.ClientNetworkHandler.handleMappingOnClient(packet);
        RandomizerLogger.debug("NetworkHandler: received mapping packet ({} chars)", packet.mappingJson().length());
    }

    /** Client: opens the randomizer setup screen. */
    private static void openSetupScreenOnClient() {
        com.universalrandomizer.client.network.ClientNetworkHandler.openSetupScreenOnClient();
    }
}
