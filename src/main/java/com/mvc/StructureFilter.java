package com.mvc;

import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.state.Dimension;
import com.seedfinding.mccore.util.math.DistanceMetric;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcfeature.structure.DesertPyramid;
import com.seedfinding.mcfeature.structure.RuinedPortal;

public class StructureFilter {
    public static final MCVersion VERSION = MCVersion.v1_16_1;
    public static final double MAX_DIST = 100.0D * 100.0D;

    private final long structureSeed;
    private final ChunkRand chunkRand;

    public static DesertPyramid dp = new DesertPyramid(VERSION);
    public static RuinedPortal rp = new RuinedPortal(Dimension.OVERWORLD, VERSION);


    public StructureFilter(long structureSeed, ChunkRand chunkRand) {
        this.structureSeed = structureSeed;
        this.chunkRand = chunkRand;
    }

    public boolean filterSeed() {
        CPos dpLocation = dp.getInRegion(structureSeed, 0, 0, chunkRand);
//        return dpLocation.distanceTo(new CPos(x, z), DistanceMetric.EUCLIDEAN_SQ) < MAX_DIST;
        if (dpLocation.toBlockPos().getMagnitudeSq() < MAX_DIST) {
            System.out.println(dpLocation.toBlockPos());
            Storage.dpLocation = dpLocation;
            return true;
        }
        return false;
    }
}
