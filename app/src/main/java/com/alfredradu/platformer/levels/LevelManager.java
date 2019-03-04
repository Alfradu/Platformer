package com.alfredradu.platformer.levels;

import com.alfredradu.platformer.Game;
import com.alfredradu.platformer.entities.Entity;
import com.alfredradu.platformer.entities.Player;
import com.alfredradu.platformer.entities.StaticEntity;
import com.alfredradu.platformer.utils.BitmapPool;

import java.util.ArrayList;

public class LevelManager {
    public int _levelHeight = 0;
    public int _levelWidth = 0;

    public final ArrayList<Entity> _entities = new ArrayList<>();
    private final ArrayList<Entity> _entitiesToAdd = new ArrayList<>();
    private final ArrayList<Entity> _entitiesToRemove = new ArrayList<>();
    public Player _player = null;
    private BitmapPool _pool = null;

    public LevelManager(final LevelData map, final BitmapPool pool){
        _pool = pool;
        loadMapAssets(map);
    }

    public void update (final double dt){
        for (Entity e : _entities){
            e.update(dt);
        }
        checkCollisions();
        addAndRemoveEntities();
    }

    private void checkCollisions(){ //TODO: only test against closest entities
        final int count = _entities.size();
        Entity a, b;
        for(int i = 0; i < count-1; i++){
            a = _entities.get(i);
            for(int j = i+1; j < count; j++){
                b = _entities.get(j);
                if (a.isColliding(b)){
                    a.onCollision(b);
                    b.onCollision(a);
                }
            }
        }
    }

    private void loadMapAssets(final LevelData map){
        cleanup();
        _levelHeight = map.mHeight;
        _levelWidth = map.mWidth;

        for(int y = 0; y < _levelHeight; y++){
            final int[] row = map.getRow(y);
            for (int x = 0; x < row.length; x++){
                final int tileID = row[x];
                if (tileID == LevelData.NO_TILE){ continue; }
                final String spriteName = map.getSpriteName(tileID);
                createEntity(spriteName, x, y);
            }
        }
    }

    private void createEntity(final String spriteName, final int xpos, final int ypos){
        Entity e = null;
        if (spriteName.equalsIgnoreCase(LevelData.PLAYER)){
            e = new Player(spriteName, xpos, ypos);
            if (_player == null){
                _player = (Player) e;
            }
        } else {
            e = new StaticEntity(spriteName, xpos, ypos);
        }
        addEntity(e);
    }

    private void addAndRemoveEntities(){
        for(Entity e : _entitiesToRemove){
            _entities.remove(e);
        }
        for(Entity e : _entitiesToAdd){
            _entities.add(e);
        }
        _entitiesToRemove.clear();
        _entitiesToAdd.clear();
    }

    public void addEntity(final Entity e){
        if (e != null){ _entitiesToAdd.add(e); }
    }

    public void removeEntity(final Entity e){
        if (e != null){ _entitiesToRemove.add(e); }
    }

    private void cleanup(){
        addAndRemoveEntities();
        for(Entity e : _entities){
            e.destroy();
        }
        _entities.clear();
        _player = null;
        _pool.empty();
    }

    public void destroy(){
        cleanup();
    }
}
