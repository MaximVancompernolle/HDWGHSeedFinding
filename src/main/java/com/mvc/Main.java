package com.mvc;

import com.mvc.filters.EndFilter;
import com.mvc.filters.OverworldFilter;
import com.seedfinding.mccore.rand.ChunkRand;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Starting seed finding...");
        Scanner scanner = new Scanner(new File("./ssvFastionEndCityShipSeedsWithCoords.txt"));
        FileWriter output = new FileWriter("./output.txt");
        int seedsChecked = 0;
        int seedMatches = 0;
        long nextTime = 0;
        long currentTime;

        while (scanner.hasNextLong() && seedMatches < Config.SEED_MATCHES) {
            long structureSeed = scanner.nextLong();
            int xCord = scanner.nextInt();
            int zCord = scanner.nextInt();

            Long matchedStructureSeed = filterStructureSeed(structureSeed, xCord, zCord) ? structureSeed : null;

            if (matchedStructureSeed != null) {
                output.write(matchedStructureSeed + "\n");
                seedMatches++;
            }
            seedsChecked++;
            currentTime = System.currentTimeMillis();

            if (currentTime > nextTime) {
                System.out.printf("%,d seeds checked with %,d matches\r", seedsChecked, seedMatches);
                nextTime = currentTime + Config.LOG_DELAY;
            }
        }
        output.close();
        System.out.printf("%,d seeds checked with %,d matches.\r", seedsChecked, seedMatches);
    }

    public static boolean filterStructureSeed(long structureSeed, int xCord, int zCord) {
        ChunkRand chunkRand = new ChunkRand();
        EndFilter endFilter = new EndFilter(structureSeed, chunkRand, xCord, zCord);

        return endFilter.filterEnd();
    }
}