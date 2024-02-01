package com.mvc;

import com.seedfinding.mccore.rand.ChunkRand;

import java.util.Random;

public class Main {

    public static void main(String[] args) {
        System.out.println("Generating a seed...");
        ChunkRand chunkRand = new ChunkRand();
        Random rand = new Random();

        long structureSeed = rand.nextLong() % (1L << 48);
        while (true) {
            if (filterStructureSeed(structureSeed, chunkRand)) {
                for (long biomeSeed = 0; biomeSeed < 1L << 16; biomeSeed++) {
                    long worldSeed = biomeSeed << 48 | structureSeed;
                    if (filterBiomeSeed(worldSeed)) {
                        System.out.println(worldSeed + " is a matching seed.");
                        return;
                    }
                }
            }
            structureSeed = rand.nextLong() % (1L << 48);
        }
    }

    public static boolean filterStructureSeed(long structureSeed, ChunkRand chunkRand) {
        return new StructureFilter(structureSeed, chunkRand).filterSeed();
    }

    public static boolean filterBiomeSeed(long worldSeed) {
        return new BiomeFilter(worldSeed).filterSeed();
    }
}
