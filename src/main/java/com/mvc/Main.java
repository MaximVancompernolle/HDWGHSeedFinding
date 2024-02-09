package com.mvc;

import com.seedfinding.mccore.rand.ChunkRand;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println("Generating a seed...");
        ChunkRand chunkRand = new ChunkRand();
        Random random = new Random();
        int seedMatches = 0;

        FileWriter output = new FileWriter("./output.txt");
        long worldSeed;

        while (seedMatches < 1_000) {
            worldSeed = random.nextLong();

            if (filterStructureSeed(worldSeed % (1L << 48), chunkRand)) {
                output.write(worldSeed + "\n");
                seedMatches++;
            }
        }

        output.close();
    }

    public static boolean filterStructureSeed(long structureSeed, ChunkRand chunkRand) {
        StructureFilter filter = new StructureFilter(structureSeed, chunkRand);
        return filter.filterNether() && filter.filterEnd();
    }
}
