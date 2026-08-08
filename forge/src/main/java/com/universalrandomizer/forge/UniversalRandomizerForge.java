package com.universalrandomizer.forge;

import com.universalrandomizer.UniversalRandomizerCommon;
import com.universalrandomizer.forge.glm.RandomizerLootModifier;
import com.mojang.serialization.Codec;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Forge entry point — delegates all initialization to the common module.
 */
@Mod(UniversalRandomizerCommon.MOD_ID)
public class UniversalRandomizerForge {

    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLM_SERIALIZERS = 
        DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, UniversalRandomizerCommon.MOD_ID);

    public UniversalRandomizerForge() {
        UniversalRandomizerCommon.init();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        GLM_SERIALIZERS.register("randomizer_modifier", RandomizerLootModifier.CODEC);
        GLM_SERIALIZERS.register(modEventBus);

        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(ForgeEventHandler.class);

        if (net.minecraftforge.fml.loading.FMLEnvironment.dist == net.minecraftforge.api.distmarker.Dist.CLIENT) {
            com.universalrandomizer.client.KeyBindingHandler.register();
        }
    }
}
