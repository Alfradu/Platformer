package com.alfredradu.platformer.levels;

import android.util.SparseArray;

import com.alfredradu.platformer.Game;
import com.alfredradu.platformer.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class TestLevel extends LevelData {
    private final SparseArray<String> _tileIdToSpriteName = new SparseArray<>();
    public TestLevel(){
        _tileIdToSpriteName.put(0, NULLSPRITE);
        _tileIdToSpriteName.put(1, PLAYER);
        _tileIdToSpriteName.put(2, "ground");
        _tileIdToSpriteName.put(3, "ground2");
        _tileIdToSpriteName.put(4, "ground3");

        InputStream is = Game.getCont().getResources().openRawResource(R.raw.level1);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        int row = 0;
        int column = 0;
        String line;
        ArrayList<String[]> temp = new ArrayList<>();
        try {
            while ((line = br.readLine()) != null) {
                String[] str = line.split(",");
                row++;
                if (str.length > column){
                    column = str.length;
                }
                temp.add(str);
            }
        } catch(Exception e) {
            System.out.println(e);
        }
        _tiles = new int[row][column];
        for (int i = 0; i < row; i++){
            for (int j = 0; j < column; j++){
                //TODO: check for pickups or other entities present
                _tiles[i][j] = Integer.parseInt(temp.get(i)[j]);
            }
        }
        updateLevelDimensions();
    }
    @Override
    public String getSpriteName(int tileType) {
        final String fileName = _tileIdToSpriteName.get(tileType);
        if (fileName != null){
            return fileName;
        }
        return NULLSPRITE;
    }
}
