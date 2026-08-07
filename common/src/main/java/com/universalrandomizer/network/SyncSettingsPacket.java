package com.universalrandomizer.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

/**
 * S→C packet: sends the full serialized {@link com.universalrandomizer.config.RandomizerConfig}
 * JSON to the client on join and on server-side config changes.
 *
 * <p>The client stores this config read-only for GUI display and recipe book hints.
 */
public record SyncSettingsPacket(String settingsJson) {

    public static final ResourceLocation ID = new ResourceLocation("universalrandomizer", "sync_settings");

    public static SyncSettingsPacket decode(FriendlyByteBuf buf) {
        return new SyncSettingsPacket(buf.readUtf(1 << 20)); // max 1 MB
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(settingsJson, 1 << 20);
    }
}
