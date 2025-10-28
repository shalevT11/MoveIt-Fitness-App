package com.example.myproject;

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
public interface LessonDao {
    @Insert
    void insert(Lesson lesson);

    @Query("SELECT * FROM lesson_table WHERE id = :id")
    LiveData<Lesson> getLessonById(long id);


    @Query("SELECT * FROM lesson_table")
    LiveData<List<Lesson>> getAllLessons();

    //deletes all lessons
    @Query("DELETE FROM user_table")
    void deleteAll();

    //dell one lessons
    @Delete
    void delete(Lesson lesson);

    @Update
    void update(Lesson lesson);
}
