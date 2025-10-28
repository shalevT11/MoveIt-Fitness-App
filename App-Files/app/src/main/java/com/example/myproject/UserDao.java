package com.example.myproject;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.Update;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.List;

@Dao
public interface UserDao {
    @Insert
    long insert(User user);

    //by the ID we will get the useName
    @Query("SELECT * FROM user_table WHERE id = :id")
    LiveData<User> getUserById(long id);

    @Query("SELECT * FROM user_table")
    LiveData<List<User>> getAllUsers();


    @Query("DELETE FROM user_table")
    void deleteAll();

    @Update
    void update(User user);

    @Delete
    void delete(User user);
}
