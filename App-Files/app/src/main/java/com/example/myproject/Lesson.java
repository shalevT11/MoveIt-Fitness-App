package com.example.myproject;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

//table for our lessons
@Entity(tableName = "lesson_table")
public class Lesson {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    //name of the lesson
    @NonNull
    @ColumnInfo(name = "title")
    private String title;

    //image of the lesson
    @NonNull
    @ColumnInfo(name = "imageUrl")
    private String imageUrl;

    //short description of the lesson
    @NonNull
    @ColumnInfo(name = "description")
    private String description;

    //video of the lesson
    @NonNull
    @ColumnInfo(name = "videoUrl")
    private String videoUrl;

    //text file of the lesson
    @NonNull
    @ColumnInfo(name = "textFileUrl")
    private String textFileUrl;

    //level of the lesson
    @NonNull
    @ColumnInfo(name = "level")
    private String level;

    @NonNull
    @ColumnInfo(name = "isRegistered")
    private Boolean isRegistered;

    @NonNull
    @ColumnInfo(name = "isFavorites")
    private Boolean isFavorites;

    @NonNull
    @ColumnInfo(name = "trainerName")
    private String trainerName;




    public Lesson(@NonNull String title, @NonNull String imageUrl, @NonNull String description, @NonNull String videoUrl, @NonNull String textFileUrl, @NonNull String level, @NonNull Boolean isRegistered, @NonNull Boolean isFavorites, @NonNull String trainerName) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.description = description;
        this.videoUrl = videoUrl;
        this.textFileUrl = textFileUrl;
        this.level = level;
        this.isRegistered = isRegistered;
        this.isFavorites = isFavorites;
        this.trainerName = trainerName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    @NonNull
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(@NonNull String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @NonNull
    public String getDescription() {
        return description;
    }

    public void setDescription(@NonNull String description) {
        this.description = description;
    }

    @NonNull
    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(@NonNull String videoUrl) {
        this.videoUrl = videoUrl;
    }

    @NonNull
    public String getTextFileUrl() {
        return textFileUrl;
    }

    public void setTextFileUrl(@NonNull String textFileUrl) {
        this.textFileUrl = textFileUrl;
    }

    @NonNull
    public String getLevel() {
        return level;
    }

    public void setLevel(@NonNull String level) {
        this.level = level;
    }


    @NonNull
    public Boolean getRegistered() {
        return isRegistered;
    }

    public void setRegistered(@NonNull Boolean registered) {
        isRegistered = registered;
    }

    @NonNull
    public Boolean getFavorites() {
        return isFavorites;
    }

    public void setFavorites(@NonNull Boolean favorites) {
        isFavorites = favorites;
    }

    @NonNull
    public String getTrainerName() {
        return trainerName;
    }

    public void setTrainerName(@NonNull String trainerName) {
        this.trainerName = trainerName;
    }
}
