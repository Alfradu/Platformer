package com.alfredradu.platformer.levels;

public abstract class LevelData {
    public static final String NULLSPRITE = "nullsprite";
    public static final String PLAYER = "player";
    public static final String ENEMY = "enemy";
    public static final String HIDDEN = "nullsprite"; //TODO: use transparent sprite
    public static final int NO_TILE = 0;

    int[][] _tiles;
    int mHeight;
    int mWidth;


    public int getTile(final int x, final int y){
        return _tiles[y][x];
    }

    int[] getRow(final int y){
        return _tiles[y];
    }

    void updateLevelDimensions(){
        mHeight = _tiles.length;
        mWidth = _tiles[0].length;
    }

    abstract public String getSpriteName(final int tileType);

}
