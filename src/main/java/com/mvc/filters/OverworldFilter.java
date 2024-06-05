package com.mvc.filters;

import com.mvc.Config;
import com.seedfinding.mccore.block.Block;
import com.seedfinding.mccore.block.Blocks;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.state.Dimension;
import com.seedfinding.mccore.util.data.Pair;
import com.seedfinding.mccore.util.math.DistanceMetric;
import com.seedfinding.mccore.util.pos.BPos;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mcfeature.loot.LootContext;
import com.seedfinding.mcfeature.loot.LootTable;
import com.seedfinding.mcfeature.loot.MCLootTables;
import com.seedfinding.mcfeature.loot.item.Item;
import com.seedfinding.mcfeature.loot.item.ItemStack;
import com.seedfinding.mcfeature.loot.item.Items;
import com.seedfinding.mcfeature.structure.DesertPyramid;
import com.seedfinding.mcfeature.structure.RuinedPortal;
import com.seedfinding.mcfeature.structure.Village;
import com.seedfinding.mcfeature.structure.generator.structure.RuinedPortalGenerator;

import java.util.HashSet;
import java.util.List;

public class OverworldFilter {
    private final long structureSeed;
    private final ChunkRand chunkRand;
//    private CPos dpLocation;
    private CPos rpLocation;
    private CPos villageLocation;
    private int obsidianInChest;

    public OverworldFilter(long structureSeed, ChunkRand chunkRand) {
        this.structureSeed = structureSeed;
        this.chunkRand = chunkRand;
    }

    public boolean filterOverworld() {
        return hasVillage() && hasRuinedPortal() && hasRpLoot(rpLocation) && isCompletable();
    }

    public boolean hasRuinedPortal() {
        RuinedPortal rp = new RuinedPortal(Dimension.OVERWORLD, Config.VERSION);
        rpLocation = rp.getInRegion(structureSeed, 0, 0, chunkRand);
        chunkRand.setCarverSeed(structureSeed, rpLocation.getX(), rpLocation.getZ(), Config.VERSION);

        if (chunkRand.nextFloat() < 0.50F) {
            return false;
        }

        return rpLocation != null && rpLocation.distanceTo(villageLocation, DistanceMetric.EUCLIDEAN_SQ) <= Config.RP_MAX_DIST;
    }

    public boolean hasVillage() {
        Village village = new Village(Config.VERSION);
        villageLocation = village.getInRegion(structureSeed, 0, 0, chunkRand);

        return villageLocation != null;
    }

//    public boolean hasDesertPyramid() {
//        DesertPyramid dp = new DesertPyramid(Config.VERSION);
//        dpLocation = dp.getInRegion(structureSeed, 0, 0, chunkRand);
//
//        return dpLocation != null;
//    }
//
//    public HashSet<CPos> hasVillage(CPos dpLocation) {
//        HashSet<CPos> villageLocations = new HashSet<>();
//        Village village = new Village(Config.VERSION);
//
//        for (int x = -1; x < 2; x++) {
//            for (int z = -1; z < 2; z++) {
//                villageLocations.add(village.getInRegion(structureSeed, dpLocation.getX() + x, dpLocation.getZ() + z, chunkRand));
//            }
//        }
//        villageLocations.removeIf(villageLocation -> villageLocation.distanceTo(dpLocation, DistanceMetric.EUCLIDEAN_SQ) > Config.VILLAGE_MAX_DIST);
//
//        return villageLocations;
//    }
//
//    public HashSet<CPos> getRuinedPortals(CPos villageLocation) {
//        HashSet<CPos> rpLocations = new HashSet<>();
//        RuinedPortal rp = new RuinedPortal(Dimension.OVERWORLD, Config.VERSION);
//
//        for (int x = -1; x < 2; x++) {
//            for (int z = -1; z < 2; z++) {
//                rpLocations.add(rp.getInRegion(structureSeed, villageLocation.getX() + x, villageLocation.getZ() + z, chunkRand));
//            }
//        }
//        rpLocations.removeIf(rpLocation -> rpLocation.distanceTo(villageLocation, DistanceMetric.EUCLIDEAN_SQ) > Config.RP_MAX_DIST);
//
//        return rpLocations;
//    }
//
//    public boolean hasDpLoot(CPos dpLocation) {
//        chunkRand.setDecoratorSeed(structureSeed, dpLocation.getX() << 4, dpLocation.getZ() << 4, 40003, Config.VERSION);
//        LootTable lootTable = MCLootTables.DESERT_PYRAMID_CHEST.get();
//        lootTable.apply(Config.VERSION);
//
//        int stringCount = 0;
//        int emeraldCount = 0;
//        int gunpowderCount = 0;
//
//        for (int i = 1; i <= 4; i++) {
//            LootContext lootContext = new LootContext(chunkRand.nextLong(), Config.VERSION);
//            List<ItemStack> chest = lootTable.generate(lootContext);
//
//            for (ItemStack stack : chest) {
//                Item item = stack.getItem();
//
//                if (item.equals(Items.STRING)) {
//                    stringCount += stack.getCount();
//                }
//                if (item.equals(Items.GUNPOWDER)) {
//                    gunpowderCount += stack.getCount();
//                }
//                if (item.equals(Items.EMERALD)) {
//                    emeraldCount += stack.getCount();
//                }
//                if (stringCount >= 39 && emeraldCount >= 13 && gunpowderCount >= 8) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

    public boolean hasRpLoot(CPos rpLocation) {
        chunkRand.setDecoratorSeed(structureSeed, rpLocation.getX() << 4, rpLocation.getZ() << 4, 40005, Config.VERSION);
        LootContext lootContext = new LootContext(chunkRand.nextLong(), Config.VERSION);
        LootTable lootTable = MCLootTables.RUINED_PORTAL_CHEST.get();
        lootTable.apply(Config.VERSION);
        List<ItemStack> items = lootTable.generate(lootContext);

        obsidianInChest = 0;
        boolean hasLight = false;
        boolean hasLooting = false;

        // TODO: check for flint and steel/fire charge/iron and flint in ruined portal chest
        for (ItemStack stack : items) {
            Item item = stack.getItem();

            if (item.getName().equals("golden_sword") && !hasLooting) {
                Pair<String, Integer> enchantment = item.getEnchantments().getFirst();
                String enchantmentName = enchantment.getFirst();
                Integer enchantmentLevel = enchantment.getSecond();

                if (!enchantmentName.equals("looting")) {
                    continue;
                }
                if (enchantmentLevel >= 2) {
                    hasLooting = true;
                }
            } else if (item.equals(Items.FLINT_AND_STEEL) || item.equals(Items.FIRE_CHARGE)) {
                hasLight = true;
            } else if (item.equals(Items.OBSIDIAN)) {
                obsidianInChest += stack.getCount();
            }
        }
        return hasLight && hasLooting;
    }

    public boolean isCompletable() {
        RuinedPortalGenerator rpGenerator = new RuinedPortalGenerator(Config.VERSION);

        if (!rpGenerator.generate(structureSeed, Dimension.OVERWORLD, rpLocation.getX(), rpLocation.getZ())) {
            return false;
        }
        if (rpGenerator.getType().equals("portal_10")) {
            return false;
        }
        List<Pair<Block, BPos>> minimalPortal = rpGenerator.getMinimalPortal();

        for (Pair<Block, BPos> pair : minimalPortal) {
            if (pair.getFirst().equals(Blocks.CRYING_OBSIDIAN)) {
                return false;
            }
        }
        return minimalPortal.size() + obsidianInChest >= 10;
    }
}