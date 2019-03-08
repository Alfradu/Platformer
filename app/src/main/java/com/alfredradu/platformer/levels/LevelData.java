package com.alfredradu.platformer.levels;

public abstract class LevelData {
    public static final String NULLSPRITE = "nullsprite";
    public static final String PLAYER = "player";
    public static final String ENEMY = "enemy";
    public static final String SPEARS = "spears";
    public static final String GROUND_RL = "wavegrass_2roundleft";
    public static final String GROUND_RR = "wavegrass_2roundright";
    public static final String GROUND_SQ = "wavegrass_square";
    public static final String MUD = "wavegrass_mudsquare";
    public static final String GROUND_URL = "wavegrass_uproundleft";
    public static final String GROUND_URR = "wavegrass_uproundright";
    public static final String HEART = "heart";
    public static final String COIN = "coin";
    public static final int NO_TILE = 0;

    int[][] _tiles;
    int _height;
    int _width;

    public int getTile(final int x, final int y){
        return _tiles[y][x];
    }

    int[] getRow(final int y){
        return _tiles[y];
    }

    void updateLevelDimensions(){
        _height = _tiles.length;
        _width = _tiles[0].length;
    }

    abstract public String getSpriteName(final int tileType);

}
