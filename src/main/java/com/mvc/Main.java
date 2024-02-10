package com.mvc;

import com.mvc.filters.EndFilter;
import com.mvc.filters.NetherFilter;
import com.mvc.filters.OverworldFilter;
import com.seedfinding.mccore.rand.ChunkRand;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("starting seed finding");
        FileWriter output = new FileWriter("./output.txt");
        ExecutorService customThreadPool = new ThreadPoolExecutor(Config.THREADS, Config.THREADS, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1000), new ThreadPoolExecutor.CallerRunsPolicy());
        int seedMatches = 0;

        while (seedMatches < Config.SEED_MATCHES) {
            customThreadPool.submit(new ThreadOperation(output));
            seedMatches = ThreadOperation.getSeedMatches();
        }
        customThreadPool.shutdown();

        if (!customThreadPool.awaitTermination(60, TimeUnit.SECONDS)) {
            System.out.println("thread pool termination timed out");
        }
        output.close();
        System.out.printf("%,d seeds checked with %,d matches", ThreadOperation.getSeedsChecked(), seedMatches);
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
    private static FileWriter output = null;
    private static int seedMatches = 0;
    private static int seedsChecked = 0;

    public ThreadOperation(FileWriter output) {
        if (ThreadOperation.output == null) { //only pass output object on first initialization of class
            ThreadOperation.output = output;
        }
    }

    @Override
    public void run() {
        long structureSeed = random.nextLong() % (1L << 48);

        if (Main.filterStructureSeed(structureSeed, chunkRand)) {
            try {
                output.write(structureSeed + "\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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