package com.mvc;

import com.mvc.filters.EndFilter;
import com.mvc.filters.NetherFilter;
import com.mvc.filters.OverworldFilter;
import com.seedfinding.mccore.rand.ChunkRand;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws IOException {
        SynchronousOutput output = new SynchronousOutput("./output.txt");
        ExecutorService executor = Executors.newFixedThreadPool(Config.THREADS);
        int seedMatches = 0;

        while (seedMatches < Config.SEED_MATCHES) {
            executor.submit(new ThreadOperation(output));
            seedMatches = ThreadOperation.getSeedMatches();
            System.out.printf("%,d seeds checked with %,d matches\r", ThreadOperation.getSeedsChecked(), seedMatches);
        }
        executor.shutdown();
        output.close();
    }

    public static boolean filterStructureSeed(long structureSeed, ChunkRand chunkRand) {
        NetherFilter netherFilter = new NetherFilter(structureSeed, chunkRand);
        OverworldFilter overworldFilter = new OverworldFilter(structureSeed, chunkRand);
        EndFilter endFilter = new EndFilter(structureSeed, chunkRand);

        return netherFilter.filterNether() && overworldFilter.filterOverworld() && endFilter.filterEnd();
    }
}

class ThreadOperation implements Runnable {
    private static final Random random = new Random();
    private final ChunkRand chunkRand = new ChunkRand();
    private static SynchronousOutput output = null;
    private static int seedMatches = 0;
    private static int seedsChecked = 0;

    public ThreadOperation (SynchronousOutput output) {
        if (ThreadOperation.output == null) { //only pass output object on first initialization of class
            ThreadOperation.output = output;
        }
    }

    @Override
    public void run() {
        long structureSeed = random.nextLong() % (1L << 48);

        if (Main.filterStructureSeed(structureSeed, chunkRand)) {
            output.write(structureSeed + "\n");
            seedMatches++;
        }
        seedsChecked++;
    }

    public static int getSeedMatches() {
        return seedMatches;
    }

    public static int getSeedsChecked() {
        return seedsChecked;
    }
}

class SynchronousOutput {
    private final BufferedWriter writer;

    public SynchronousOutput(String path) throws IOException {
        writer = new BufferedWriter(new FileWriter(path));
    }

    public synchronized void write(String output) {
        try {
            writer.write(output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() throws IOException {
        writer.close();
    }
}