package com.mvc;

import com.seedfinding.mccore.version.MCVersion;

public class Config {
    public static final int THREADS = 12;
    public static final int SEED_MATCHES = 1;
    public static final int LOG_DELAY = 10_000;
    public static final MCVersion VERSION = MCVersion.v1_16_1;
    public static final double DP_MAX_DIST = 32D * 32D;
    public static final double RP_MAX_DIST = 16D * 16D;
    public static final double VILLAGE_MAX_DIST = 16D * 16D;
    public static final double BASTION_MAX_DIST = 10D * 10D;
    public static final double FORTRESS_MAX_DIST = 14D * 14D;
    public static final double EC_MAX_DIST = 20D * 20D;
}