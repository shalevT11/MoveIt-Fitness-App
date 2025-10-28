package com.example.myproject;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.Future;

public class UserViewModel extends AndroidViewModel {

    private final UserRepository repository; //all the actions that about db pass through him
    private final LiveData<List<User>> allUsers; //containing all lessons

    //brings the repository, and then sends the users to allUsers
    public UserViewModel(@NonNull Application application){
        super(application);
        repository = new UserRepository(application);
        allUsers = repository.getAllUsers();
    }

    //gets all the users
    public LiveData<List<User>> getAllUsers(){
        return allUsers;
    }

    public Future<Long> insert(User user) {
        return repository.insert(user);
    }
    public LiveData<User> getUserById(long id){
        return repository.getUserById(id);
    }

    //update user
    public void update(User user){
        repository.update(user);
    }

    //delete specific user
    public void delete(User user){
        repository.delete(user);
    }

    //delete all the users
    public void deleteAll(){
        repository.deleteAll();
    }
}
