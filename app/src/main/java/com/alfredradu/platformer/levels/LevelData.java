package com.alfredradu.platformer.levels;

public abstract class LevelData {
    public static final String NULLSPRITE = "nullsprite";
    public static final String PLAYER = "player";
    public static final String ENEMY = "enemy";
    public static final String SPEARS = "spears";
    public static final String HEART = "heart";
    public static final String COIN = "coin";
    public static final int NO_TILE = 0;
    int[][] _tiles;
    int _height;
    int _width;
    String _mapName = "testLvl";
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
