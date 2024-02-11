package com.mvc.filters;

import com.mvc.Config;
import com.seedfinding.mcbiome.source.OverworldBiomeSource;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.state.Dimension;
import com.seedfinding.mccore.util.data.Pair;
import com.seedfinding.mccore.util.math.DistanceMetric;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.util.pos.RPos;
import com.seedfinding.mcfeature.loot.ChestContent;
import com.seedfinding.mcfeature.loot.item.Item;
import com.seedfinding.mcfeature.loot.item.ItemStack;
import com.seedfinding.mcfeature.loot.item.Items;
import com.seedfinding.mcfeature.structure.DesertPyramid;
import com.seedfinding.mcfeature.structure.RuinedPortal;
import com.seedfinding.mcfeature.structure.Village;
import com.seedfinding.mcfeature.structure.generator.structure.DesertPyramidGenerator;
import com.seedfinding.mcfeature.structure.generator.structure.RuinedPortalGenerator;
import com.seedfinding.mcterrain.terrain.OverworldTerrainGenerator;

import java.util.HashSet;
import java.util.List;

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

                if (dpLocation == null || !hasDpLoot(dpLocation)) {
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

                if (rpLocation == null || !hasRpLoot(rpLocation)) {
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

    public boolean hasDpLoot(CPos dpLocation) {
        DesertPyramidGenerator dpGen = new DesertPyramidGenerator(Config.VERSION);
        dpGen.generate(owTerrainGen, dpLocation, chunkRand);
        List<ChestContent> loot = dp.getLoot(structureSeed, dpGen, chunkRand, false);

        int stringCount = 0;
        int gunpowderCount = 0;

        for (ChestContent chest : loot) {
            if (!(chest.contains(Items.STRING) || chest.contains(Items.GUNPOWDER))) {
                continue;
            }
            for (ItemStack stack : chest.getItems()) {
                Item item = stack.getItem();

                if (!(item.equals(Items.STRING) || item.equals(Items.GUNPOWDER))) {
                    continue;
                }

                if (item.equals(Items.STRING)) {
                    stringCount += stack.getCount();
                } else if (item.equals(Items.GUNPOWDER)) {
                    gunpowderCount += stack.getCount();
                }

                if (stringCount >= 39 && gunpowderCount >= 8) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasRpLoot(CPos rpLocation) {
        RuinedPortalGenerator rpGen = new RuinedPortalGenerator(Config.VERSION);
        rpGen.generate(owTerrainGen, rpLocation, chunkRand);
        List<ChestContent> loot = rp.getLoot(structureSeed, rpGen, chunkRand, false);

        // TODO: check for flint and steel/fire charge/iron and flint in ruined portal chest
        for (ChestContent chest : loot) {
            if (!chest.contains(Items.GOLDEN_SWORD)) {
                continue;
            }
            for (ItemStack stack : chest.getItems()) {
                Item item = stack.getItem();

                if (!item.equals(Items.GOLDEN_SWORD)) {
                    continue;
                }
                for (Pair<String, Integer> enchantment : item.getEnchantments()) {
                    String enchantmentName = enchantment.getFirst();
                    Integer enchantmentLevel = enchantment.getSecond();

                    if (!enchantmentName.equals("looting")) {
                        continue;
                    }
                    if (enchantmentLevel >= 2) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}