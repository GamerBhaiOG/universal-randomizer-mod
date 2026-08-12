package com.universalrandomizer.forge;

import com.universalrandomizer.UniversalRandomizerCommon;
import com.universalrandomizer.forge.glm.RandomizerLootModifier;
import com.mojang.serialization.Codec;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * NeoForge entry point — delegates all initialization to the common module.
 */
@Mod(UniversalRandomizerCommon.MOD_ID)
public class UniversalRandomizerForge {

    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLM_SERIALIZERS = 
        DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, UniversalRandomizerCommon.MOD_ID);

    public UniversalRandomizerForge() {
        UniversalRandomizerCommon.init();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        GLM_SERIALIZERS.register("randomizer_modifier", RandomizerLootModifier.CODEC);
        GLM_SERIALIZERS.register(modEventBus);

        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(ForgeEventHandler.class);

        if (net.neoforged.fml.loading.FMLEnvironment.dist == net.neoforged.api.distmarker.Dist.CLIENT) {
            ClientInit.registerClient();
        }
    }

    private static class ClientInit {
        private static void registerClient() {
            com.universalrandomizer.client.KeyBindingHandler.register();
        }
    }
}
