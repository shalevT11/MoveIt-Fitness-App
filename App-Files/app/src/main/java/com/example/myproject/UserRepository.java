package com.example.myproject;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class UserRepository {
    private final UserDao userDao;     // Data Access Object for the User table
    private final LiveData<List<User>> allUsers;    // LiveData list of all users from the database
    private final ExecutorService executorService;     // Executor to run database operations off the main thread


    // Constructor receives Application to initialize the database and DAO
    public UserRepository(Application application){
        AppRoomDatabase db = AppRoomDatabase.getDatabase(application); // Get the singleton instance of the Room database
        userDao = db.userDao(); // Initialize the DAO
        allUsers = userDao.getAllUsers(); // Get LiveData of all users
        executorService = Executors.newSingleThreadExecutor(); // Set up a background thread for database operations
    }

    // Returns LiveData so the UI can observe the user list for changes
    public LiveData<List<User>> getAllUsers(){
        return allUsers;
    }

    public Future<Long> insert(User user) {
        return AppRoomDatabase.databaseWriteExecutor.submit(() -> userDao.insert(user));
    }

    public LiveData<User> getUserById(long id){
        return userDao.getUserById(id);
    }


    // Updates a user in the database (runs in background)
    public void update(User user){
        executorService.execute(()->userDao.update(user));
    }

    // Deletes a specific user from the database (runs in background)
    public void delete(User user){
        executorService.execute(()->userDao.delete(user));
    }

    // Deletes all users from the table (runs in background)
    public void deleteAll(){
        executorService.execute(userDao::deleteAll);
    }


}
