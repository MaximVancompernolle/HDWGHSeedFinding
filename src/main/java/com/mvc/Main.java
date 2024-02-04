package com.mvc;

import com.seedfinding.mccore.rand.ChunkRand;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        System.out.println("Generating a seed...");
        ChunkRand chunkRand = new ChunkRand();

        try {
            Scanner scanner = new Scanner(new File("./12eyeseeds.txt"));
            FileWriter output = new FileWriter("./city-sword.txt");

            while (scanner.hasNextLong()) {
                long worldSeed = scanner.nextLong();
                if (filterStructureSeed(worldSeed % (1L << 48), chunkRand)) {
                    output.write(worldSeed + "\n");
                }
            }
            output.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean filterStructureSeed(long structureSeed, ChunkRand chunkRand) {
        StructureFilter filter = new StructureFilter(structureSeed, chunkRand);
        return filter.filterEnd();
    }
}
