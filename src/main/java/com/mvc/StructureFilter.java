package com.mvc;

import com.seedfinding.mcbiome.source.EndBiomeSource;
import com.seedfinding.mcbiome.source.NetherBiomeSource;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.data.Pair;
import com.seedfinding.mccore.util.math.DistanceMetric;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.util.pos.RPos;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mcfeature.loot.ChestContent;
import com.seedfinding.mcfeature.loot.item.Item;
import com.seedfinding.mcfeature.loot.item.ItemStack;
import com.seedfinding.mcfeature.structure.BastionRemnant;
import com.seedfinding.mcfeature.structure.EndCity;
import com.seedfinding.mcfeature.structure.Fortress;
import com.seedfinding.mcfeature.structure.generator.structure.EndCityGenerator;
import com.seedfinding.mcterrain.terrain.EndTerrainGenerator;
import com.seedfinding.mcterrain.terrain.NetherTerrainGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.List;

public class StructureFilter {
    public static final MCVersion VERSION = MCVersion.v1_16_1;
    public static final double MAX_DIST = 12D * 12D;
    private final long structureSeed;
    private final ChunkRand chunkRand;
    public BastionRemnant bastion = new BastionRemnant(VERSION);
    public Fortress fortress = new Fortress(VERSION);
    public EndCity ec = new EndCity(VERSION);
    public EndCityGenerator ecGen = new EndCityGenerator(VERSION);
    public NetherBiomeSource netherBiomeSource;
    public NetherTerrainGenerator netherTerrainGenerator;
    public EndBiomeSource endBiomeSource;
    public EndTerrainGenerator endTerrainGen;

    public StructureFilter(long structureSeed, ChunkRand chunkRand) {
        this.structureSeed = structureSeed;
        this.chunkRand = chunkRand;
        this.netherBiomeSource = new NetherBiomeSource(VERSION, structureSeed);
        this.netherTerrainGenerator = new NetherTerrainGenerator(netherBiomeSource);
        this.endBiomeSource = new EndBiomeSource(VERSION, structureSeed);
        this.endTerrainGen = new EndTerrainGenerator(endBiomeSource);
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

                if (bastionDistance < MAX_DIST && filterNetherBiomes(bastionLocation)) {
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

                    if (fortressDistance < MAX_DIST) {
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

    public static CPos firstGateway(long structureSeed) {
        ArrayList<Integer> gateways = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            gateways.add(i);
        }
        Collections.shuffle(gateways, new Random(structureSeed));
        double angle = 2.0 * ((-1 * Math.PI) + (Math.PI / 20) * (gateways.remove(gateways.size() - 1)));
        int gateway_x = (int)(1024.0 * Math.cos(angle));
        int gateway_z = (int)(1024.0 * Math.sin(angle));
        return new CPos(gateway_x >> 4, gateway_z >> 4);
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
                if ((ecDistance < MAX_DIST) && citySpawnsWithShip(ecLocation)) {
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
