package com.example.myproject;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class LessonViewModel extends AndroidViewModel {

    private LessonRepository repository; //all the actions that about db pass through him
    private LiveData<List<Lesson>> allLessons; //containing all lessons

    //brings the repository, and then sends the lessons to allLessons
    public LessonViewModel(@NonNull Application application){
        super(application);
        repository = new LessonRepository(application);
        allLessons = repository.getAllLessons();
    }

    //gets all the lessons
    public LiveData<List<Lesson>> getAllLessons(){
        return allLessons;
    }

    //insert a lesson
    public void insert (Lesson lesson){
        repository.insert(lesson);
    }

    //update lesson
    public void update (Lesson lesson){
        repository.update(lesson);
    }

    //delete specific lesson
    public void delete (Lesson lesson){
        repository.delete(lesson);
    }

    //delete all the lessons
    public void deleteAll (){
        repository.deleteAll();
    }

    public LiveData<Lesson> getLessonById(long id){
        return repository.getLessonById(id);
    }

}
