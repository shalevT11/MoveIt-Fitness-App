package com.example.myproject;


import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//tells Room that its a DB
@Database(entities = {User.class, Lesson.class}, version = 4, exportSchema = false)
public abstract class AppRoomDatabase extends RoomDatabase {

    //the Dao methods, here we connects between them to Room
    public abstract UserDao userDao();
    public abstract LessonDao lessonDao();


    private static volatile AppRoomDatabase INSTANCE;

    //executor
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    //if its null, build the DB
    public static AppRoomDatabase getDatabase(final Context context){
        if (INSTANCE==null){
            synchronized (AppRoomDatabase.class){
                if (INSTANCE==null){
                    //creates database here
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppRoomDatabase.class,
                            "my_database"
                    )
                            .addCallback(roomCallback)
                            .fallbackToDestructiveMigration() //add destroy and rebuild
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    //its happens on the first time that the app is up, i.e. default lessons
    private static final RoomDatabase.Callback roomCallback = new RoomDatabase.Callback(){
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db){
            super.onCreate(db);

            Log.d("RoomDB", "✅ onCreate DB callback is running");

            databaseWriteExecutor.execute(()->{

                Log.d("RoomDB", "✅ Inserting default lessons");

                UserDao userDao = INSTANCE.userDao();
                LessonDao lessonDao = INSTANCE.lessonDao();

                lessonDao.insert(new Lesson(
                        "אימון פלג גוף עליון",
                        "cbun_upper_body_pic",
                        "פלג גוף עליון חזק – גב, חזה, ידיים.",
                        "android.resource://com.example.myproject/"+R.raw.cbum_upper_body,
                        "android.resource://com.example.myproject/"+R.raw.upper_body_desc,
                        "מתחילים",
                        false,
                        false,
                        "כריס באמסטד"
                ));

                lessonDao.insert(new Lesson(
                        "אימון בטן",
                        "abs_pic",
                        "אימון בטן לחיזוק הליבה והכוח.",
                        "android.resource://com.example.myproject/"+R.raw.abs_video,
                        "android.resource://com.example.myproject/"+R.raw.abs_desc,
                        "בינוני",
                        false,
                        false,
                        "ריאן טרי"
                ));

                lessonDao.insert(new Lesson(
                        "אימון רגליים",
                        "cbum_legs_pic",
                        "חיזוק רגליים יציב, כוח לכל הגוף.",
                        "android.resource://com.example.myproject/"+R.raw.legs_cbum_video,
                        "android.resource://com.example.myproject/"+R.raw.legs_desc,
                        "בינוני",
                        false,
                        false,
                        "כריס באמסטד"
                ));

                lessonDao.insert(new Lesson(
                        "אימון PUSH",
                        "cbum_push",
                        "אימון חזה כתפיים ויד אחורית.",
                        "android.resource://com.example.myproject/"+R.raw.push_cbum_video,
                        "android.resource://com.example.myproject/"+R.raw.chest_desc,
                        "מתחילים",
                        false,
                        false,
                        "כריס באמסטד"
                ));

                lessonDao.insert(new Lesson(
                        "אימון כתפיים",
                        "cbun_shoulder_pic",
                        "כתפיים חזקות – שליטה ובנייה.",
                        "android.resource://com.example.myproject/"+R.raw.cbum_shoulders_video,
                        "android.resource://com.example.myproject/"+R.raw.shoulders_desc,
                        "מתקדמים",
                        false,
                        false,
                        "כריס באמסטד"
                ));



            });

        }
    };




}
