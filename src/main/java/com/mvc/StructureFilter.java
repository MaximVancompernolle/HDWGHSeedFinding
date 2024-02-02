package com.mvc;

import com.seedfinding.mcbiome.source.EndBiomeSource;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.math.DistanceMetric;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.util.pos.RPos;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcfeature.structure.EndCity;
import com.seedfinding.mcfeature.structure.generator.structure.EndCityGenerator;
import com.seedfinding.mcterrain.terrain.EndTerrainGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class StructureFilter {
    public static final MCVersion VERSION = MCVersion.v1_16_1;
    public static final double MAX_DIST = 128.0D * 128.0D;
    private final long structureSeed;
    private final ChunkRand chunkRand;
    public static EndCity ec = new EndCity(VERSION);
    public static CPos ecLocation;


    public StructureFilter(long structureSeed, ChunkRand chunkRand) {
        this.structureSeed = structureSeed;
        this.chunkRand = chunkRand;
    }

    public static BPos firstGateway(long structureSeed) {
        ArrayList<Integer> gateways = new ArrayList<>();
        for (int i = 0; i < 20; i++) gateways.add(i);
        Collections.shuffle(gateways, new Random(structureSeed));
        double angle = 2.0 * ((-1 * Math.PI) + (Math.PI / 20) * (gateways.remove(gateways.size() - 1)));
        int gateway_x = (int)(1024.0 * Math.cos(angle));
        int gateway_z = (int)(1024.0 * Math.sin(angle));
        return new BPos(gateway_x, 0, gateway_z);
    }

    public boolean filterEnd() {
        BPos gateway = firstGateway(structureSeed);
        RPos region = gateway.toRegionPos(20 << 4);
        ecLocation = ec.getInRegion(structureSeed, region.getX(), region.getZ(), chunkRand);
        return ecLocation.toBlockPos().distanceTo(gateway, DistanceMetric.EUCLIDEAN_SQ) < MAX_DIST;
    }

    public boolean filterEndBiomes() {
        EndBiomeSource source = new EndBiomeSource(VERSION, structureSeed);
        EndTerrainGenerator terrainGen = new EndTerrainGenerator(source);

        if (!ec.canSpawn(ecLocation.getX(), ecLocation.getZ(), source)) return false;
        if (!ec.canGenerate(ecLocation.getX(), ecLocation.getZ(), terrainGen)) return false;

        EndCityGenerator ecGen = new EndCityGenerator(VERSION);
        ecGen.generate(terrainGen, ecLocation);

        return ecGen.hasShip();
    }
}
