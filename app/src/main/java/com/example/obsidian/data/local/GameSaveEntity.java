package com.example.obsidian.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "game_save")
public class GameSaveEntity {
    @PrimaryKey
    public int id = 1;

    @NonNull
    public String serializedState;

    public GameSaveEntity() {
        this.serializedState = "";
    }

    @Ignore
    public GameSaveEntity(@NonNull String serializedState) {
        this.serializedState = serializedState;
    }
}
