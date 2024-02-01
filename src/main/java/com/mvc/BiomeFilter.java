package com.mvc;

import com.seedfinding.mcbiome.source.OverworldBiomeSource;

public class BiomeFilter {
    private final long worldSeed;

    public BiomeFilter(long worldSeed) {
        this.worldSeed = worldSeed;
    }

    public boolean filterSeed() {
        OverworldBiomeSource owBiomeSource = new OverworldBiomeSource(StructureFilter.VERSION, worldSeed);
        return StructureFilter.dp.canSpawn(Storage.dpLocation, owBiomeSource);
    }
}