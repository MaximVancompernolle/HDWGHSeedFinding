package com.mvc;

import com.mvc.filters.EndFilter;
import com.seedfinding.mccore.rand.ChunkRand;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        System.out.println("Starting seed finding...");
        Scanner scanner = new Scanner(new File("./ssvFastionEndCityShipSeedsWithCoords.txt"));
        Scanner checkpointScanner = new Scanner(new File("./boincpoint"));
        FileWriter output = new FileWriter("./output.txt", true);
        int seedsChecked = 0;
        int seedsCheckpointLoaded = 0;
        if (checkpointScanner.hasNextInt()) {
            seedsCheckpointLoaded = checkpointScanner.nextInt();
        }
        int seedMatches = 0;
        long nextTime = 0;
        long currentTime;

        while (scanner.hasNextLong()) {
            long structureSeed = scanner.nextLong();
            int xCord = scanner.nextInt();
            int zCord = scanner.nextInt();

            if (seedsChecked < seedsCheckpointLoaded) {
                seedsChecked++;
                continue;
            }

            Long matchedStructureSeed = filterStructureSeed(structureSeed, xCord, zCord) ? structureSeed : null;

            if (matchedStructureSeed != null) {
                output.write(matchedStructureSeed + "\n");
                seedMatches++;
            }
            seedsChecked++;

            currentTime = System.currentTimeMillis();
            if (currentTime > nextTime) {
                System.out.printf("%,d seeds checked with %,d matches\r", seedsChecked, seedMatches);
                output.flush();
                try (FileWriter writer = new FileWriter("./boincpoint")) {
                    writer.write(String.valueOf(seedsChecked));
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try (FileWriter writer = new FileWriter("./boinc_frac")) {
                    writer.write(String.format("%.6f", (double) seedsChecked / 321017.0));
                    writer.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }

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