package com.example.myproject;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

//table for our users
@Entity(tableName = "user_table")
public class User {


    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    @NonNull
    @ColumnInfo(name = "username")
    private String username;

    @NonNull
    @ColumnInfo(name = "role") //Trainer/trainee
    private String role;

    @ColumnInfo(name = "level")
    private String level;

    @ColumnInfo(name="phone")
    private String phone;

    @ColumnInfo(name="age")
    private Integer age;

    @ColumnInfo(name = "gender")
    private String gender;

    //constructor
    public User(@NonNull String username, @NonNull String role) {
        this.username = username;
        this.role = role;
        this.level=null;
        this.phone = null;
        this.age = null;
        this.gender = null;
    }

    //getters and setters
    @NonNull
    public String getUsername() {
        return username;
    }

    public void setUsername(@NonNull String username) {
        this.username = username;
    }

    @NonNull
    public String getRole() {
        return role;
    }

    public void setRole(@NonNull String role) {
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
