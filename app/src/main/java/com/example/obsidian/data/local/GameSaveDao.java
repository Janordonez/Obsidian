package com.example.obsidian.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface GameSaveDao {
    @Query("SELECT * FROM game_save WHERE id = 1 LIMIT 1")
    GameSaveEntity getSave();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSave(GameSaveEntity save);

    @Query("DELETE FROM game_save WHERE id = 1")
    void deleteSave();
}
