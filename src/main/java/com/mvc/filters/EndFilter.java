package com.mvc.filters;

import com.mvc.Config;
import com.seedfinding.mcbiome.source.EndBiomeSource;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.data.Pair;
import com.seedfinding.mccore.util.math.DistanceMetric;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.util.pos.RPos;
import com.seedfinding.mcfeature.loot.ChestContent;
import com.seedfinding.mcfeature.loot.item.Item;
import com.seedfinding.mcfeature.loot.item.ItemStack;
import com.seedfinding.mcfeature.structure.EndCity;
import com.seedfinding.mcfeature.structure.generator.structure.EndCityGenerator;
import com.seedfinding.mcterrain.terrain.EndTerrainGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.List;

public class EndFilter {
    private final long structureSeed;
    private final ChunkRand chunkRand;
    public EndCity ec = new EndCity(Config.VERSION);
    public EndCityGenerator ecGen = new EndCityGenerator(Config.VERSION);
    public EndBiomeSource endBiomeSource;
    public EndTerrainGenerator endTerrainGen;

    public EndFilter (long structureSeed, ChunkRand chunkRand) {
        this.structureSeed = structureSeed;
        this.chunkRand = chunkRand;
        this.endBiomeSource = new EndBiomeSource(Config.VERSION, structureSeed);
        this.endTerrainGen = new EndTerrainGenerator(endBiomeSource);
    }

    public boolean filterEnd() {
        CPos gateway = firstGateway(structureSeed);
        RPos gatewayRegionPos = gateway.toBlockPos().toRegionPos(20 << 4); //toRegionPos is only accurate with BPos
        RPos[][] regions = new RPos[3][3];

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                regions[x + 1][z + 1] = gatewayRegionPos.add(x, z);
            }
        }
        HashSet<CPos> ecLocations = new HashSet<>();

        for (RPos[] rowOfRegions : regions) {
            for (RPos region : rowOfRegions) {
                CPos ecLocation = ec.getInRegion(structureSeed, region.getX(), region.getZ(), chunkRand);
                double ecDistance = ecLocation.distanceTo(gateway, DistanceMetric.EUCLIDEAN_SQ);
                if ((ecDistance < Config.EC_MAX_DIST) && citySpawnsWithShip(ecLocation)) {
                    ecLocations.add(ecLocation);
                }
            }
        }

        if (ecLocations.isEmpty()) {
            return false;
        }

        for (CPos cityEntry : ecLocations) {
            if (filterEndLoot(cityEntry)) {
                return true;
            }
        }
        return false;
    }

    public static CPos firstGateway(long structureSeed) {
        ArrayList<Integer> gateways = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            gateways.add(i);
        }
        Collections.shuffle(gateways, new Random(structureSeed));
        double angle = 2.0 * ((-1 * Math.PI) + (Math.PI / 20) * (gateways.removeLast()));
        int gateway_x = (int)(1024.0 * Math.cos(angle));
        int gateway_z = (int)(1024.0 * Math.sin(angle));

        return new CPos(gateway_x >> 4, gateway_z >> 4);
    }

    public boolean citySpawnsWithShip(CPos ecLocation) {
        if (!ec.canSpawn(ecLocation, endBiomeSource) || !ec.canGenerate(ecLocation, endTerrainGen)) {
            return false;
        }
        ecGen.generate(endTerrainGen, ecLocation, chunkRand);

        return ecGen.hasShip();
    }

    public boolean filterEndLoot(CPos ecLocation) {
        boolean foundGoodSword = false;
        ecGen.generate(endTerrainGen, ecLocation, chunkRand);
        List<ChestContent> loot = ec.getLoot(structureSeed, ecGen, chunkRand, false);

        lootLoop:
        for (ChestContent chest : loot) {
            for (ItemStack stack : chest.getItems()) {
                if (foundGoodSword) {
                    break lootLoop;
                }
                foundGoodSword = hasGoodSword(stack);
            }
        }
        return foundGoodSword;
    }

    public boolean hasGoodSword(ItemStack stack) {
        Item item = stack.getItem();
        int swordType = switch (item.getName()) {
            case "iron_sword" -> 2;
            case "diamond_sword" -> 1;
            default -> 0;
        };

        if (swordType == 0) {
            return false;
        }

        boolean hasSmite = false;
        boolean hasLooting = false;

        for (Pair<String, Integer> enchantment : item.getEnchantments()) {
            String enchantmentName = enchantment.getFirst();
            Integer enchantmentLevel = enchantment.getSecond();

            if (enchantmentName.equals("smite") && !hasSmite) {
                hasSmite = enchantmentLevel - swordType >= 3;
            } else if (enchantmentName.equals("looting") && !hasLooting) {
                hasLooting = enchantmentLevel == 3;
            }
        }
        return hasSmite && hasLooting;
    }
}
