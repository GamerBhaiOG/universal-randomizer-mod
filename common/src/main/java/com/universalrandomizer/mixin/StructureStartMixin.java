package com.universalrandomizer.mixin;

import com.universalrandomizer.config.RandomizerMode;
import com.universalrandomizer.core.RandomizerManager;
import com.universalrandomizer.features.StructureSpawnRandomizer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin on StructureStart to trigger structure spawn randomization during world gen structure placement.
 */
@Mixin(StructureStart.class)
public abstract class StructureStartMixin {

    @Shadow public abstract Structure getStructure();

    @Inject(
        method = "placeInWorld",
        at = @At("HEAD"),
        remap = true,
        require = 0
    )
    private void universalRandomizer$onPlaceStructure(WorldGenLevel level, StructureManager structureManager, ChunkGenerator chunkGenerator, RandomSource random, BoundingBox box, ChunkPos chunkPos, CallbackInfo ci) {
        RandomizerManager mgr = RandomizerManager.getInstance();
        if (!mgr.isInitialized() || !mgr.isEnabled(RandomizerMode.STRUCTURE_SPAWNS)) return;

        Structure originalStructure = this.getStructure();
        if (originalStructure != null) {
            ResourceLocation key = level.registryAccess().registryOrThrow(Registries.STRUCTURE).getKey(originalStructure);
            if (key != null) {
                StructureSpawnRandomizer.getMappedStructure(key);
            }
        }
    }
}
