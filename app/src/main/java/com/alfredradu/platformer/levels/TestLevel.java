package com.alfredradu.platformer.levels;

import android.util.SparseArray;

public class TestLevel extends LevelData {
    private final SparseArray<String> mTileIdToSpriteName = new SparseArray<>();
    public TestLevel(){
        mTileIdToSpriteName.put(0, NULLSPRITE);
        mTileIdToSpriteName.put(1, PLAYER);
        mTileIdToSpriteName.put(2, "ground");
        mTileIdToSpriteName.put(3, "ground2");
        mTileIdToSpriteName.put(4, "ground3");

        mTiles = new int[][]{
                {2,0,0,0,0,0,0},
                {2,0,0,1,0,0,0},
                {2,0,0,0,0,0,0},
                {2,0,0,0,0,0,0},
                {2,0,0,0,0,0,0},
                {2,2,2,2,2,2,2},
                {0,0,0,0,0,0,0}
        };
        updateLevelDimensions();
    }
    @Override
    public String getSpriteName(int tileType) {
        final String fileName = mTileIdToSpriteName.get(tileType);
        if (fileName != null){
            return fileName;
        }
        return NULLSPRITE;
    }
}
