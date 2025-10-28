package com.example.myproject;


import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LessonRepository {

    private final LessonDao lessonDao; // Data Access Object for the Lesson table
    private final LiveData<List<Lesson>> allLessons; // LiveData list of all lessons from the database
    private final ExecutorService executorService; // Executor to run database operations off the main thread

    // Constructor receives Application to initialize the database and DAO
    public LessonRepository(Application application){
        AppRoomDatabase db = AppRoomDatabase.getDatabase(application); // Get the singleton instance of the Room database
        lessonDao = db.lessonDao(); // Initialize the DAO
        allLessons = lessonDao.getAllLessons(); // Get LiveData of all lessons
        executorService = Executors.newSingleThreadExecutor(); // Set up a background thread for database operations
    }

    // Returns LiveData so the UI can observe the lesson list for changes
    public LiveData<List<Lesson>> getAllLessons(){
        return allLessons;
    }

    //to open the details
    public LiveData<Lesson> getLessonById(long id){
        return lessonDao.getLessonById(id);
    }

    // Inserts a lesson into the database (runs in background)
    public void insert(Lesson lesson){
        executorService.execute(()->lessonDao.insert(lesson));
    }

    // Updates a lesson in the database (runs in background)
    public void update(Lesson lesson){
        executorService.execute(()->lessonDao.update(lesson));
    }

    // Deletes a specific user from the database (runs in background)
    public void delete(Lesson lesson){
        executorService.execute(()->lessonDao.delete(lesson));
    }

    // Deletes all users from the table (runs in background)
    public void deleteAll(){
        executorService.execute(lessonDao::deleteAll);
    }
}
