package com.mvc.filters;

import com.mvc.Config;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.state.Dimension;
import com.seedfinding.mcfeature.structure.DesertPyramid;
import com.seedfinding.mcfeature.structure.RuinedPortal;
import com.seedfinding.mcfeature.structure.Village;

public class OverworldFilter {
    public DesertPyramid dp = new DesertPyramid(Config.VERSION);
    public RuinedPortal rp = new RuinedPortal(Dimension.OVERWORLD, Config.VERSION);
    public Village village = new Village(Config.VERSION);

    public OverworldFilter(long structureSeed, ChunkRand chunkRand) {

    }

    public boolean filterOverworld() {
        return false;
    }
}
