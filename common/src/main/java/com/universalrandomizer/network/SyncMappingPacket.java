package com.universalrandomizer.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * S→C packet: sends the full serialized mapping JSON (all domain tables).
 *
 * <p>Chunked if the JSON exceeds Minecraft's packet size.
 * The client uses this to display mapping info in the GUI.
 *
 * <p>For very large modpacks the mapping JSON may be tens of KB — handled
 * via Architectury's NetworkManager which handles fragmentation automatically.
 */
public record SyncMappingPacket(String mappingJson) {

    public static final ResourceLocation ID = new ResourceLocation("universalrandomizer", "sync_mapping");

    public static SyncMappingPacket decode(FriendlyByteBuf buf) {
        return new SyncMappingPacket(buf.readUtf(4 << 20)); // max 4 MB
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(mappingJson, 4 << 20);
    }
}
