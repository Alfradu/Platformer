package com.alfredradu.platformer.levels;

public abstract class LevelData {
    public static final String NULLSPRITE = "nullsprite";
    public static final String PLAYER = "player";
    public static final int NO_TILE = 0;

    int[][] mTiles;
    int mHeight;
    int mWidth;

    public int getTile(final int x, final int y){
        return mTiles[y][x];
    }

    int[] getRow(final int y){
        return mTiles[y];
    }

    void updateLevelDimensions(){
        mHeight = mTiles.length;
        mWidth = mTiles[0].length;
    }

    abstract public String getSpriteName(final int tileType);

}
