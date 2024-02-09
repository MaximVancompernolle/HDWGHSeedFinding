package com.mvc.filters;

import com.mvc.Config;
import com.seedfinding.mcbiome.source.NetherBiomeSource;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.math.DistanceMetric;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.util.pos.RPos;
import com.seedfinding.mcfeature.structure.BastionRemnant;
import com.seedfinding.mcfeature.structure.Fortress;
import com.seedfinding.mcterrain.terrain.NetherTerrainGenerator;

import java.util.HashSet;

public class NetherFilter {
    private final long structureSeed;
    private final ChunkRand chunkRand;
    public BastionRemnant bastion = new BastionRemnant(Config.VERSION);
    public Fortress fortress = new Fortress(Config.VERSION);
    public NetherBiomeSource netherBiomeSource;
    public NetherTerrainGenerator netherTerrainGenerator;

    public NetherFilter (long structureSeed, ChunkRand chunkRand) {
        this.structureSeed = structureSeed;
        this.chunkRand = chunkRand;
        this.netherBiomeSource = new NetherBiomeSource(Config.VERSION, structureSeed);
        this.netherTerrainGenerator = new NetherTerrainGenerator(netherBiomeSource);
    }

    public boolean filterNether() {
        RPos[][] bastionRegions = new RPos[2][2];
        RPos[][] fortressRegions = new RPos[2][2];

        for (int x = -1; x <= 0; x++) {
            for (int z = -1; z <= 0; z++) {
                bastionRegions[x + 1][z + 1] = new RPos(x, z, 27 << 4);
                fortressRegions[x + 1][z + 1] = new RPos(x, z, 27 << 4);
            }
        }
        HashSet<CPos> bastionLocations = new HashSet<>();

        for (RPos[] rowOfBastionRegions : bastionRegions) {
            for (RPos bastionRegion : rowOfBastionRegions) {
                CPos bastionLocation = bastion.getInRegion(structureSeed, bastionRegion.getX(), bastionRegion.getZ(), chunkRand);

                if (bastionLocation == null) {
                    continue;
                }
                double bastionDistance = bastionLocation.getMagnitudeSq();

                if (bastionDistance < Config.BASTION_MAX_DIST && filterNetherBiomes(bastionLocation)) {
                    bastionLocations.add(bastionLocation);
                }
            }
        }

        if (bastionLocations.isEmpty()) {
            return false;
        }

        for (RPos[] rowOfFortressRegions : fortressRegions) {
            for (RPos fortressRegion : rowOfFortressRegions) {
                CPos fortressLocation = fortress.getInRegion(structureSeed, fortressRegion.getX(), fortressRegion.getZ(), chunkRand);

                if (fortressLocation == null) {
                    continue;
                }

                for (CPos bastionEntry : bastionLocations) {
                    double fortressDistance = fortressLocation.distanceTo(bastionEntry, DistanceMetric.EUCLIDEAN_SQ);

                    if (fortressDistance < Config.FORTRESS_MAX_DIST) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean filterNetherBiomes(CPos bastionLocation) {
        return bastion.canSpawn(bastionLocation, netherBiomeSource) && bastion.canGenerate(bastionLocation, netherTerrainGenerator);
    }
}
