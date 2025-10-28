package com.example.myproject;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LessonDetailsActivity extends AppCompatActivity {
    private LessonViewModel lessonViewModel;
    private Lesson currentLesson;

    private TextView tvLessonTitle, tvTrainerName, tvDescription, tvRegister, tvBigLessonTitle;
    private VideoView videoView;
    private Button btnRegister;
    private ImageButton btnBack;
    private CheckBox fav_button;
    private MediaController mediaController;

    // Flag to track if activity is active (prevents window leaks)
    private boolean isActivityActive = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set dark icons on status bar for Android M and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.watch_lesson);

        // Initialize UI components
        tvLessonTitle = findViewById(R.id.tvLessonTitle); //lesson title
        tvBigLessonTitle = findViewById(R.id.tvBigLessonTitle); //lesson title
        videoView = findViewById(R.id.videoView); //the video
        btnRegister = findViewById(R.id.btnRegister); //the register to lesson class
        tvRegister = findViewById(R.id.tvRegister); //text that we register to the lesson
        btnBack = findViewById(R.id.btnBack); //back to lessons
        tvDescription=findViewById(R.id.tvDescription);
        fav_button = findViewById(R.id.fav_button); //fav btn
        tvTrainerName = findViewById(R.id.tvTrainerName); //the name of the trainer


        // Get lesson ID from intent
        long lessonId = getIntent().getLongExtra("lessonId", -1); //get the id of the current lesson
        //if lessons don't exist
        /*
        if (lessonId == -1){
            finish();
            return;
        }
        */

        // Initialize ViewModel
        lessonViewModel = new ViewModelProvider(this).get(LessonViewModel.class);

        // Observe lesson data by ID
        lessonViewModel.getLessonById(lessonId).observe(this, lesson ->{
            currentLesson = lesson;

            // Setup favorite button
            // Remove listener temporarily to prevent triggering during setup
            fav_button.setOnCheckedChangeListener(null);
                fav_button.setChecked(currentLesson.getFavorites());
            // Set listener to update favorite status
                fav_button.setOnCheckedChangeListener(((buttonView, isChecked) -> {
                    currentLesson.setFavorites(isChecked);
                }));


             // Set lesson information in UI
                tvLessonTitle.setText(currentLesson.getTitle()); //match the name of the lesson
                tvTrainerName.setText(currentLesson.getTrainerName()); //match the trainer name of the lesson
                tvBigLessonTitle.setText(currentLesson.getTitle()); //match the name of the lesson

            // Update register button text based on current status
                updateButtonText(btnRegister, currentLesson.getRegistered());

            // Hide register confirmation text if lesson is not registered
                if (currentLesson.getRegistered()==false){
                    tvRegister.setVisibility(View.GONE);
                }

            // Setup register button click listener
                btnRegister.setOnClickListener(v ->{
                    // Toggle registration status
                    boolean isRegisterLesson = !currentLesson.getRegistered();
                    currentLesson.setRegistered(isRegisterLesson);


                    // Show toast message
                    Toast.makeText(this,
                            isRegisterLesson ? "שיעור נרשם כבוצע" : "בוטל ביצוע שיעור",
                            Toast.LENGTH_SHORT).show();

                    // Update button text
                    updateButtonText(btnRegister, isRegisterLesson); //update the btn txt

                    // Show/hide registration confirmation text
                    if (isRegisterLesson) {
                        tvRegister.setVisibility(View.VISIBLE);
                    } else {
                        tvRegister.setVisibility(View.GONE);
                    }
                });

                // Load and display text file content
                String textUrl = currentLesson.getTextFileUrl();
                if (textUrl.startsWith("content://") || textUrl.startsWith("file://")){
                    // Load file from phone storage (user-uploaded content)
                    Uri textUri = Uri.parse(textUrl);
                    String text = readTextFromUri(textUri);
                    tvDescription.setText(text);
                }
                else {
                    // Load text from raw resources (pre-built content)
                    Uri textUri = Uri.parse(currentLesson.getTextFileUrl());
                    int resId = Integer.parseInt(textUri.getLastPathSegment());
                    String text = readRawTextFile(resId);
                    tvDescription.setText(text);
                }

            // Load and setup video playback
            String videoUrl = currentLesson.getVideoUrl();
            if (videoUrl.startsWith("content://")) {
                // Content URI from device storage
                Uri videoUri = Uri.parse(videoUrl);
                videoView.setVideoURI(videoUri);
            } else if (videoUrl.startsWith("file://")) {
                // File URI from internal storage - extract path and use setVideoPath
                String filePath = Uri.parse(videoUrl).getPath();
                if (filePath != null && new File(filePath).exists()) {
                    videoView.setVideoPath(filePath);
                } else {
                    Log.e("LessonDetails", "Video file not found: " + filePath);
                }
            } else if (videoUrl.startsWith("/")) {
                // Direct file path from internal storage
                if (new File(videoUrl).exists()) {
                    videoView.setVideoPath(videoUrl);
                } else {
                    Log.e("LessonDetails", "Video file not found: " + videoUrl);
                }
            } else {
                // Load video from raw resources (pre-built content)
                Uri uri = Uri.parse(currentLesson.getVideoUrl());
                Log.d("LessonDetails", "Video URI: " + uri.toString());
                videoView.setVideoURI(uri);
            }

            // Setup media controller for video playback
            mediaController = new MediaController(this);
            mediaController.setAnchorView(videoView);
            videoView.setMediaController(mediaController);

            // Setup video prepared listener to show controls when ready
            videoView.setOnPreparedListener(mp -> {
                // Multiple checks to prevent window leaks
                if (isActivityActive && mediaController != null && !isFinishing() && !isDestroyed()) {
                    try {
                        mediaController.show();
                    } catch (Exception e) {
                        Log.e("LessonDetails", "Error showing media controller: " + e.getMessage());
                    }
                }
            });

            // Start video playback if activity is still active
            if (isActivityActive && !isFinishing()) {
                videoView.start();
            }
            });



        // Setup back button to save changes and close activity
        btnBack.setOnClickListener(v->{
            lessonViewModel.update(currentLesson);
            finish();
        });


    }

    /**
     * Updates the register button text based on registration status
     * @param button The button to update
     * @param isRegistered Current registration status
     */
    private void updateButtonText(Button button, boolean isRegistered) {
        if (isRegistered) {
            button.setText("סמן כלא בוצע");
        } else {
            button.setText("סמן שיעור כבוצע");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Mark activity as inactive to prevent operations
        isActivityActive = false; // הוסף את השורה הזו

        // Pause video playback
        if (videoView != null) {
            videoView.pause();
        }

        // Hide media controller safely
        if (mediaController != null) {
            try {
                mediaController.hide();
            } catch (Exception e) {
                Log.e("LessonDetails", "Error hiding media controller in onPause: " + e.getMessage());
            }
        }
    }

    @Override
    protected void onDestroy() {
        // Mark activity as inactive
        isActivityActive = false;

        // Clean up media controller first
        if (mediaController != null) {
            try {
                mediaController.hide();
                mediaController = null; // Prevent memory leaks
            } catch (Exception e) {
                Log.e("LessonDetails", "Error in mediaController cleanup: " + e.getMessage());
            }
        }

        // Clean up video view
        if (videoView != null) {
            try {
                videoView.stopPlayback();
                videoView.setVideoURI(null); // Clear URI to release resources
                videoView = null; // Prevent memory leaks
            } catch (Exception e) {
                Log.e("LessonDetails", "Error in videoView cleanup: " + e.getMessage());
            }
        }

        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Mark activity as inactive when not visible
        isActivityActive = false;

        // Hide media controller when activity stops
        if (mediaController != null) {
            try {
                mediaController.hide();
            } catch (Exception e) {
                Log.e("LessonDetails", "Error hiding media controller in onStop: " + e.getMessage());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Mark activity as active when resumed
        isActivityActive = true;
    }

    /**
     * Reads text content from raw resource file
     * @param resId Resource ID of the text file
     * @return String content of the file or error message
     */
    private String readRawTextFile(int resId) {
        InputStream inputStream = null;
        try {
            inputStream = getResources().openRawResource(resId);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader reader = new BufferedReader(inputStreamReader);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            reader.close();
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "שגיאה בטעינת הקובץ";
        } finally {
            if (inputStream != null) {
                try { inputStream.close(); } catch (Exception ignored) {}
            }
        }
    }

    /**
     * Reads text content from URI (user-uploaded files)
     * @param uri URI of the text file
     * @return String content of the file or error message
     */
    private String readTextFromUri(Uri uri) {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "שגיאה בטעינת הקובץ";
        }
        return stringBuilder.toString();
    }
}
