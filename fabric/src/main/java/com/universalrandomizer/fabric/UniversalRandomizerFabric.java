package com.universalrandomizer.fabric;

import com.universalrandomizer.UniversalRandomizerCommon;
import net.fabricmc.api.ModInitializer;

/**
 * Fabric server-side entry point — delegates all initialization to the common module.
 */
public class UniversalRandomizerFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        UniversalRandomizerCommon.init();
        FabricEventHandler.registerEvents();
    }
}
