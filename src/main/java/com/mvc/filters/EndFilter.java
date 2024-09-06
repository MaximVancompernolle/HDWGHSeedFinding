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
import com.seedfinding.mcfeature.loot.item.Items;
import com.seedfinding.mcfeature.structure.EndCity;
import com.seedfinding.mcfeature.structure.generator.structure.EndCityGenerator;
import com.seedfinding.mcterrain.terrain.EndTerrainGenerator;

import java.util.ArrayList;
import java.util.List;

public class EndFilter {
    private final long structureSeed;
    private final ChunkRand chunkRand;
    public EndCity ec = new EndCity(Config.VERSION);
    public CPos gateway;

    public EndFilter(long structureSeed, ChunkRand chunkRand, int xCord, int zCord) {
        this.structureSeed = structureSeed;
        this.chunkRand = chunkRand;
        this.gateway = new CPos(xCord >> 4, zCord >> 4);
    }

    public boolean filterEnd() {
        return checkCity();
    }

    public boolean checkCity() {
        RPos gatewayRegionPos = gateway.toBlockPos().toRegionPos(20 << 4); //toRegionPos is only accurate with BPos

        for (int x = -1; x < 2; x++) {
            for (int z = -1; z < 2; z++) {
                CPos ecLocation = ec.getInRegion(structureSeed, gatewayRegionPos.getX() + x, gatewayRegionPos.getZ() + z, chunkRand);
                if (ecLocation.distanceTo(gateway, DistanceMetric.EUCLIDEAN_SQ) > Config.EC_MAX_DIST) {
                    continue;
                }
                if (hasEndLoot(ecLocation)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasEndLoot(CPos ecLocation) {
        EndBiomeSource endBiomeSource = new EndBiomeSource(Config.VERSION, structureSeed);
        EndTerrainGenerator endTerrainGen = new EndTerrainGenerator(endBiomeSource);
        EndCityGenerator ecGen = new EndCityGenerator(Config.VERSION);

        if (!ecGen.generate(endTerrainGen, ecLocation, chunkRand)) {
            return false;
        }
//        ecGen.generate(endTerrainGen, ecLocation, chunkRand);

        List<ChestContent> loot = ec.getLoot(structureSeed, ecGen, chunkRand, false);
        int ironCount = 0;
        int diamondCount = 0;
        ArrayList<ItemStack> swordList = new ArrayList<>();
        boolean hasHelmet = false;
        boolean diamondHelmet = false;
        boolean hasChestplate = false;
        boolean diamondChestplate = false;
        boolean hasLeggings = false;
        boolean diamondLeggings = false;
        boolean hasBoots = false;
        boolean diamondBoots = false;
        boolean hasPickaxe = false;
        boolean hasShovel = false;

        for (ChestContent chest : loot) {
            for (ItemStack stack : chest.getItems()) {
                if (stack.getItem().equals(Items.IRON_INGOT)) {
                    ironCount += stack.getCount();
                } else if (stack.getItem().equals(Items.DIAMOND)) {
                    diamondCount += stack.getCount();
                } else if (stack.getItem().getName().equals("diamond_sword") || stack.getItem().getName().equals("iron_sword")) {
                    swordList.add(stack);
                } else if (stack.getItem().getName().equals("diamond_helmet")) {
                    if (isGoodHelmet(stack)) {
                        hasHelmet = true;
                    }
                    diamondHelmet = true;
                } else if (stack.getItem().getName().equals("iron_helmet")) {
                    if (isGoodHelmet(stack)) {
                        hasHelmet = true;
                    }
                } else if (stack.getItem().getName().equals("diamond_chestplate")) {
                    hasChestplate = true;
                    diamondChestplate = true;
                } else if (stack.getItem().getName().equals("iron_chestplate")) {
                    hasChestplate = true;
                } else if (stack.getItem().getName().equals("diamond_leggings")) {
                    hasLeggings = true;
                    diamondLeggings = true;
                } else if (stack.getItem().getName().equals("iron_leggings")) {
                    hasLeggings = true;
                } else if (stack.getItem().getName().equals("diamond_boots")) {
                    if (isGoodBoots(stack)) {
                        hasBoots = true;
                    }
                    diamondBoots = true;
                } else if (stack.getItem().getName().equals("iron_boots")) {
                    if (isGoodBoots(stack)) {
                        hasBoots = true;
                    }
                } else if (stack.getItem().getName().equals("diamond_pickaxe") || stack.getItem().getName().equals("iron_pickaxe")) {
                    hasPickaxe |= isGoodTool(stack);
                } else if (stack.getItem().getName().equals("diamond_shovel") || stack.getItem().getName().equals("iron_shovel")) {
                    hasShovel |= isGoodTool(stack);
                }
            }
        }
        if (swordList.isEmpty()) {
            return false;
        } else if (!hasGoodSword(swordList)) {
            return false;
        }
        if (!hasHelmet || !hasChestplate || !hasLeggings || !hasBoots || !hasPickaxe || !hasShovel) {
            return false;
        }
        if (ironCount < 31) {
            return false;
        }
        diamondCount -= diamondHelmet ? 0 : 5;
        diamondCount -= diamondChestplate ? 0 : 8;
        diamondCount -= diamondLeggings ? 0 : 7;
        diamondCount -= diamondBoots ? 0 : 4;

        return diamondCount >= 7;
    }

    public boolean hasGoodSword(ArrayList<ItemStack> swordList) {
        boolean hasDiamondSmite = false;
        boolean hasDiamondLooting = false;
        boolean hasIronSmite = false;
        boolean hasIronLooting = false;

        for (ItemStack stack : swordList) {
            Item item = stack.getItem();
            int swordType = switch (item.getName()) {
                case "iron_sword" -> 2;
                case "diamond_sword" -> 1;
                default -> 0;
            };

            for (Pair<String, Integer> enchantment : item.getEnchantments()) {
                String enchantmentName = enchantment.getFirst();
                Integer enchantmentLevel = enchantment.getSecond();

                if (enchantmentName.equals("smite") && (enchantmentLevel - swordType > 2)) {
                    switch (swordType) {
                        case 1 -> hasDiamondSmite = true;
                        case 2 -> hasIronSmite = true;
                    }
                } else if (enchantmentName.equals("looting") && (enchantmentLevel > 1)) {
                    switch (swordType) {
                        case 1 -> hasDiamondLooting = true;
                        case 2 -> hasIronLooting = true;
                    }
                }
            }
        }
        return (hasDiamondSmite && hasDiamondLooting) || (hasIronSmite && hasIronLooting);
    }

    public boolean isGoodHelmet(ItemStack stack) {
        Item item = stack.getItem();

        for (Pair<String, Integer> enchantment : item.getEnchantments()) {
            String enchantmentName = enchantment.getFirst();
            if (enchantmentName.equals("aqua_affinity")) {
                return true;
            }
        }
        return false;
    }

    public boolean isGoodBoots(ItemStack stack) {
        Item item = stack.getItem();

        for (Pair<String, Integer> enchantment : item.getEnchantments()) {
            String enchantmentName = enchantment.getFirst();
            Integer enchantmentLevel = enchantment.getSecond();
            if (enchantmentName.equals("depth_strider") && enchantmentLevel == 3) {
                return true;
            }
        }
        return false;
    }

    public boolean isGoodTool(ItemStack stack) {
        Item item = stack.getItem();

        for (Pair<String, Integer> enchantment : item.getEnchantments()) {
            String enchantmentName = enchantment.getFirst();
            Integer enchantmentLevel = enchantment.getSecond();
            if (enchantmentName.equals("efficiency") && enchantmentLevel >= 4) {
                return true;
            }
        }
        return false;
    }
}