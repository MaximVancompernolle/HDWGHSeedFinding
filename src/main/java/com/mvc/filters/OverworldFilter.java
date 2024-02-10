package com.mvc.filters;

import com.mvc.Config;
import com.seedfinding.mcbiome.source.OverworldBiomeSource;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.state.Dimension;
import com.seedfinding.mccore.util.math.DistanceMetric;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.util.pos.RPos;
import com.seedfinding.mcfeature.structure.DesertPyramid;
import com.seedfinding.mcfeature.structure.RuinedPortal;
import com.seedfinding.mcfeature.structure.Village;
import com.seedfinding.mcterrain.terrain.OverworldTerrainGenerator;

import java.util.HashSet;

public class OverworldFilter {
    private final long structureSeed;
    private final ChunkRand chunkRand;
    public DesertPyramid dp = new DesertPyramid(Config.VERSION);
    public RuinedPortal rp = new RuinedPortal(Dimension.OVERWORLD, Config.VERSION);
    public Village village = new Village(Config.VERSION);
    public OverworldBiomeSource owBiomeSource;
    public OverworldTerrainGenerator owTerrainGen;

    public OverworldFilter(long structureSeed, ChunkRand chunkRand) {
        this.structureSeed = structureSeed;
        this.chunkRand = chunkRand;
        this.owBiomeSource = new OverworldBiomeSource(Config.VERSION, structureSeed);
        this.owTerrainGen = new OverworldTerrainGenerator(owBiomeSource);
    }

    public boolean filterOverworld() {
        RPos[][] dpRegions = new RPos[2][2];
        RPos[][] villageRegions = new RPos[2][2];
        RPos[][] rpRegions = new RPos[2][2];

        for (int x = -1; x <= 0; x++) {
            for (int z = -1; z <= 0; z++) {
                dpRegions[x + 1][z + 1] = new RPos(x, z, 32 << 4);
                villageRegions[x + 1][z + 1] = new RPos(x, z, 32 << 4);
                rpRegions[x + 1][z + 1] = new RPos(x, z, 40 << 4);
            }
        }
        HashSet<CPos> dpLocations = new HashSet<>();

        for (RPos[] rowOfDpRegions : dpRegions) {
            for (RPos dpRegion : rowOfDpRegions) {
                CPos dpLocation = dp.getInRegion(structureSeed, dpRegion.getX(), dpRegion.getZ(), chunkRand);

                if (dpLocation == null) {
                    continue;
                }

                if (dpLocation.getMagnitudeSq() < Config.DP_MAX_DIST) {
                    dpLocations.add(dpLocation);
                }
            }
        }

        if (dpLocations.isEmpty()) {
            return false;
        }
        HashSet<CPos> villageLocations = new HashSet<>();

        for (RPos[] rowOfVillageRegions : villageRegions) {
            for (RPos villageRegion : rowOfVillageRegions) {
                CPos villageLocation = village.getInRegion(structureSeed, villageRegion.getX(), villageRegion.getZ(), chunkRand);

                if (villageLocation == null) {
                    continue;
                }

                for (CPos dpLocation : dpLocations) {
                    if (villageLocation.distanceTo(dpLocation, DistanceMetric.EUCLIDEAN_SQ) < Config.VILLAGE_MAX_DIST) {
                        villageLocations.add(villageLocation);
                    }
                }
            }
        }

        if (villageLocations.isEmpty()) {
            return false;
        }

        for (RPos[] rowOfRpRegions : rpRegions) {
            for (RPos rpRegion : rowOfRpRegions) {
                CPos rpLocation = rp.getInRegion(structureSeed, rpRegion.getX(), rpRegion.getZ(), chunkRand);

                if (rpLocation == null) {
                    continue;
                }

                for (CPos villageLocation : villageLocations) {
                    if (rpLocation.distanceTo(villageLocation, DistanceMetric.EUCLIDEAN_SQ) < Config.RP_MAX_DIST) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}