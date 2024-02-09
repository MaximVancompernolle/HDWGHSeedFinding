package com.mvc;

import com.mvc.filters.EndFilter;
import com.mvc.filters.NetherFilter;
import com.mvc.filters.OverworldFilter;
import com.seedfinding.mccore.rand.ChunkRand;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class Main {
    public static void main(String[] args) throws IOException {
        ChunkRand chunkRand = new ChunkRand();
        Random random = new Random();
        FileWriter output = new FileWriter("./output.txt");
        int seedMatches = 0;
        long seedsChecked = 0;
        long worldSeed;

        while (seedMatches < Config.SEED_MATCHES) {
            worldSeed = random.nextLong();

            if (filterStructureSeed(worldSeed % (1L << 48), chunkRand)) {
                output.write(worldSeed + "\n");
                seedMatches++;
            }
            System.out.printf("%,d\r", ++seedsChecked);
        }
        output.close();
    }

    public static boolean filterStructureSeed(long structureSeed, ChunkRand chunkRand) {
        NetherFilter netherFilter = new NetherFilter(structureSeed, chunkRand);
        OverworldFilter overworldFilter = new OverworldFilter(structureSeed, chunkRand);
        EndFilter endFilter = new EndFilter(structureSeed, chunkRand);

        return netherFilter.filterNether() && overworldFilter.filterOverworld() && endFilter.filterEnd();
    }
}
