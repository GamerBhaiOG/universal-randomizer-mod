package com.universalrandomizer.fabric.client;

import com.universalrandomizer.client.KeyBindingHandler;
import com.universalrandomizer.client.network.ClientNetworkHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Fabric client-side entry point for Universal Randomizer.
 */
@Environment(EnvType.CLIENT)
public class UniversalRandomizerFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        KeyBindingHandler.register();
        ClientNetworkHandler.registerClientPackets();
    }
}
