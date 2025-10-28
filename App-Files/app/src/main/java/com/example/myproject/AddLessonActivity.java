package com.example.myproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Activity for adding new lessons or editing existing ones.
 * Supports file selection (video, text, image) from device storage
 * and automatically copies them to internal storage for app persistence.
 * Handles both raw resources (pre-built content) and user-uploaded content.
 */
public class AddLessonActivity extends AppCompatActivity {

    //ViewModel for DB options (delete, update, insert)
    private LessonViewModel lessonViewModel;


    // Request codes for file picking intents to identify which type of file was selected
    private static final int PICK_VIDEO_REQUEST = 1;
    private static final int PICK_TEXT_REQUEST = 2;
    private static final int PICK_IMAGE_REQUEST=3;


    // Store selected URIs for files (video, text, image), -  these will be validated before
    private Uri selectedVideoUri = null;
    private  Uri selectedTextUri = null;
    private Uri selectedImageUri = null;

    // UI elements for file selection and form inputs
    private MaterialButton btnAddVideo; // Button to select video file
    private MaterialButton btnAddTxt; // Button to select text file
    private ImageButton btnAddPic, btnSave; // Image selection and save buttons

    private ImageButton btnBack; // Back navigation button
    private Spinner spnLevel; // Difficulty level selection spinner


    // Form input fields for lesson details
    private EditText etLessonName, etShortDis;

    //lesson id and interface for edit mode
    private long lessonId = -1; // Default value indicates "add new lesson" mode
    private Lesson existingLesson; // Holds lesson data when editing existing lesson

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Set dark status bar icons for Android M and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR); // אייקונים כהים
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_lesson); // Load the layout XML


        // Initialize ViewModel for database operations
        lessonViewModel = new ViewModelProvider(this).get(LessonViewModel.class);

        // Initialize UI components by connecting to XML layout elements
        btnBack = findViewById(R.id.btnBack); //match the back btn
        etLessonName = findViewById(R.id.etLessonName); //match the lesson name
        etShortDis = findViewById(R.id.etShortDis); //math the short description
        spnLevel = findViewById(R.id.spnLevel); //match the level of the lesson
        btnSave=findViewById(R.id.btnSave); //the save btn


        // Setup video selection button with file picker intent
        btnAddVideo = findViewById(R.id.btnAddVideo); //match the btn to add the video
        btnAddVideo.setOnClickListener(v ->{ //new intent that the btn open
            // Open a file picker for video files
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("video/*"); // Filter to show only video files
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, "בחר קובץ וידאו"), PICK_VIDEO_REQUEST);
        });

        // Setup text file selection button with file picker intent
        btnAddTxt = findViewById(R.id.btnAddTxt); //match the btn to add the txt
        btnAddTxt.setOnClickListener(v ->{
            // Open a file picker specifically for text files
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("text/plain"); // Filter to show only plain text files
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent, "בחר קובץ טקסט"), PICK_TEXT_REQUEST);
        });

        // Setup image selection button with file picker intent
        btnAddPic = findViewById(R.id.btnAddPic);
        btnAddPic.setOnClickListener(v ->{
            // Open a file picker specifically for image files
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*"); // Filter to show only image files
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        // Setup back button to close activity without saving
        btnBack.setOnClickListener(v->{
            finish();
        });

        // Check if this activity was opened for editing an existing lesson, if we did, we have this intent which is the lessonId
        lessonId = getIntent().getLongExtra("lessonId", -1);
        if (lessonId != -1){
            // Observe the lesson by ID
            lessonViewModel.getLessonById(lessonId).observe(this, lesson -> {
                if (lesson != null){
                    existingLesson = lesson;

                    // Populate form fields with existing lesson data
                    etLessonName.setText(lesson.getTitle()); //gets the name of the lesson
                    etShortDis.setText(lesson.getDescription()); //gets the short des
                    setSpinnerSelection(spnLevel, lesson.getLevel()); //gets the level of the lesson

                    // Load and display existing image
                    if (lesson.getImageUrl() != null && !lesson.getImageUrl().isEmpty()) {
                        String imageUrl = lesson.getImageUrl();
                        ImageButton imageButton = findViewById(R.id.btnAddPic);

                        if (imageUrl.startsWith("content://") || imageUrl.startsWith("file://")) {
                            // User-uploaded image file from device storage
                            selectedImageUri = Uri.parse(imageUrl);
                            imageButton.setImageURI(selectedImageUri);
                        } else if (imageUrl.startsWith("android.resource://")) {
                            // Already a full resource URI - use directly
                            selectedImageUri = Uri.parse(imageUrl);
                            imageButton.setImageURI(selectedImageUri);
                        } else {
                            // Raw resource name - convert to resource ID and display
                            int imageResId = getResources().getIdentifier(imageUrl, "drawable", getPackageName());
                            if (imageResId != 0) {
                                imageButton.setImageResource(imageResId);
                                // Create marker URI to indicate resource exists without creating invalid URI
                                selectedImageUri = Uri.parse("raw_resource://" + imageUrl);
                            } else {
                                Log.e("AddLessonActivity", "Image resource not found for: " + imageUrl);
                            }
                        }
                    }


                    // Load and display existing video
                    if (lesson.getVideoUrl() != null && !lesson.getVideoUrl().isEmpty()) {
                        String videoUrl = lesson.getVideoUrl();
                        VideoView videoView = findViewById(R.id.videoPreview);
                        videoView.setVisibility(View.VISIBLE);

                        // Determine video type and load accordingly
                        if (videoUrl.startsWith("android.resource://") || !videoUrl.contains("/")) {
                            // Raw resource video from app bundle
                            try {
                                if (!videoUrl.contains("/")) {
                                    // Resource name only, create full URI
                                    int resourceId = getResources().getIdentifier(videoUrl, "raw", getPackageName());
                                    selectedVideoUri = Uri.parse("android.resource://" + getPackageName() + "/" + resourceId);
                                } else {
                                    selectedVideoUri = Uri.parse(videoUrl);
                                }
                                videoView.setVideoURI(selectedVideoUri);
                            } catch (Exception e) {
                                Log.e("AddLessonActivity", "Error loading raw video: " + e.getMessage());
                            }
                        } else if (videoUrl.startsWith("file://")) {
                            // File URI from internal storage - extract path for setVideoPath
                            String filePath = Uri.parse(videoUrl).getPath();
                            if (filePath != null && new File(filePath).exists()) {
                                selectedVideoUri = Uri.fromFile(new File(filePath));
                                videoView.setVideoPath(filePath);
                            } else {
                                Log.e("AddLessonActivity", "Video file not found: " + filePath);
                            }
                        } else if (videoUrl.startsWith("/")) {
                            // Direct file path from internal storage
                            if (new File(videoUrl).exists()) {
                                selectedVideoUri = Uri.fromFile(new File(videoUrl));
                                videoView.setVideoPath(videoUrl);
                            } else {
                                Log.e("AddLessonActivity", "Video file not found: " + videoUrl);
                            }
                        } else {
                            // Try as content URI from external storage
                            try {
                                selectedVideoUri = Uri.parse(videoUrl);
                                videoView.setVideoURI(selectedVideoUri);
                            } catch (Exception e) {
                                Log.e("AddLessonActivity", "Error loading video: " + e.getMessage());
                            }
                        }

                        videoView.start();
                    }


                    // Load and display existing text file content
                    if (lesson.getTextFileUrl() != null && !lesson.getTextFileUrl().isEmpty()) {
                        String textUrl = lesson.getTextFileUrl();
                        selectedTextUri = Uri.parse(textUrl); // Always update selectedTextUri for validation

                        ScrollView scrollView = findViewById(R.id.scrollView);
                        scrollView.setVisibility(View.VISIBLE);
                        TextView txtView = findViewById(R.id.textPreview);
                        try {
                            // Read text content from URI
                            InputStream inputStream = getContentResolver().openInputStream(selectedTextUri);
                            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                            StringBuilder builder = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                builder.append(line).append("\n");
                            }
                            txtView.setText(builder.toString());
                            reader.close();
                            inputStream.close();
                        } catch (Exception e) {
                            txtView.setText("שגיאה בטעינת קובץ הטקסט");
                        }
                    }
                }
            });
        }

        // Setup save button with comprehensive validation
        btnSave.setOnClickListener(v ->{
            // Validate lesson name is not empty
            if (etLessonName.getText().toString().isEmpty()){
                btnSave.setActivated(false);
                Toast.makeText(AddLessonActivity.this, "אנא מלא שם שיעור", Toast.LENGTH_SHORT).show();
                etLessonName.requestFocus(); //makes the code ne on lesson name after the toast
            }
            // Validate short description is not empty
            else if (etShortDis.getText().toString().isEmpty()) {
                btnSave.setActivated(false);
                Toast.makeText(AddLessonActivity.this, "אנא מלא תיאור קצר", Toast.LENGTH_SHORT).show();
                etShortDis.requestFocus(); //makes the code ne on short dis after the toast

            }
            // Validate lesson name length (max 19 characters)
            else if (etLessonName.length() > 19) {
                    Toast.makeText(this, "שם השיעור לא יכול להיות יותר מ-19 תווים", Toast.LENGTH_SHORT).show();
                    etLessonName.requestFocus(); //makes the code ne on lesson name after the toast
            }
            // Validate description length (max 40 characters)
            else if (etShortDis.length() > 40) {
                    Toast.makeText(this, "התיאור הקצר לא יכול להיות יותר מ-40 תווים", Toast.LENGTH_SHORT).show();
                    etShortDis.requestFocus(); //makes the code ne on short dis after the toast
            }
            // Validate that image is selected
            else if (selectedImageUri == null) {
                Toast.makeText(this, "אנא הוסף תמונה לשיעור", Toast.LENGTH_SHORT).show();
            }
            // Validate that video is selected
            else if (selectedVideoUri == null) {
                Toast.makeText(this, "אנא הוסף וידאו לשיעור", Toast.LENGTH_SHORT).show();
            }
            // Validate that text file is selected
            else if (selectedTextUri == null) {
                Toast.makeText(this, "אנא הוסף קובץ טקסט לשיעור", Toast.LENGTH_SHORT).show();
            }
            // All validations passed - proceed with saving
            else {
                saveLesson();
                finish();
            }
        });

    }

    @SuppressWarnings("WrongConstant")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();

            // Handle video file selection
            if (requestCode == PICK_VIDEO_REQUEST) {
                // Copy selected video to internal storage for app persistence
                String localPath = copyFileToInternalStorage(uri, "video", "mp4");
                if (localPath != null) {
                    selectedVideoUri = Uri.fromFile(new File(localPath));

                    // Display video preview
                    VideoView videoView = findViewById(R.id.videoPreview);
                    videoView.setVisibility(View.VISIBLE);
                    videoView.setVideoPath(localPath);
                    videoView.start();
                } else {
                    Toast.makeText(this, "Failed to save video", Toast.LENGTH_SHORT).show();
                }
            }

            // Handle text file selection result
            if (requestCode == PICK_TEXT_REQUEST) {
                // Copy selected text file to internal storage
                String localPath = copyFileToInternalStorage(uri, "text", "txt");
                if (localPath != null) {
                    selectedTextUri = Uri.fromFile(new File(localPath));

                    // Display text preview
                    TextView txtView = findViewById(R.id.textPreview);
                    ScrollView scrollView = findViewById(R.id.scrollView);
                    scrollView.setVisibility(View.VISIBLE);

                    try {
                        // Read and display text content
                        BufferedReader reader = new BufferedReader(new FileReader(localPath));
                        StringBuilder builder = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            builder.append(line).append("\n");
                        }
                        txtView.setText(builder.toString());
                        reader.close();
                    } catch (Exception e) {
                        txtView.setText("Error loading text file");
                    }
                } else {
                    Toast.makeText(this, "Failed to save text file", Toast.LENGTH_SHORT).show();
                }
            }

            // Handle image file selection result
            if (requestCode == PICK_IMAGE_REQUEST) {
                // Copy selected image to internal storage
                String localPath = copyFileToInternalStorage(uri, "image", "jpg");
                if (localPath != null) {
                    selectedImageUri = Uri.fromFile(new File(localPath));

                    // Display image preview
                    ImageButton imageButton = findViewById(R.id.btnAddPic);
                    imageButton.setImageURI(selectedImageUri);
                } else {
                    Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
                }
            }

            // Maintain video playback after any file selection to keep user experience smooth
            if (selectedVideoUri != null) {
                VideoView videoView = findViewById(R.id.videoPreview);
                if (videoView != null && videoView.getVisibility() == View.VISIBLE) {
                    String videoUriString = selectedVideoUri.toString();

                    // Check video type and use appropriate playback method
                    if (videoUriString.startsWith("android.resource://")) {
                        // Raw resource video - use setVideoURI
                        videoView.setVideoURI(selectedVideoUri);
                    } else {
                        // Internal storage file - use setVideoPath
                        String videoPath = selectedVideoUri.getPath();
                        if (videoPath != null && new File(videoPath).exists()) {
                            videoView.setVideoPath(videoPath);
                        }
                    }
                    videoView.start();
                }
            }
        }
    }



    /**
     * Saves lesson data to database with smart URI handling
     * Handles both new lesson creation and existing lesson updates
     * Preserves original resource names for raw resources to avoid broken references
     */
    private void saveLesson(){
        String lessonName = etLessonName.getText().toString();
        String shortDescription = etShortDis.getText().toString();

        // Smart URI handling - preserve original names for raw resources to avoid broken references
        String videoUri;
        String textUri;
        String imageUri;

        if (lessonId != -1 && existingLesson != null) {
            // Editing existing lesson - preserve original resource names if not changed

            // Handle image URI
            if (selectedImageUri.toString().startsWith("raw_resource://") &&
                    existingLesson.getImageUrl() != null) {
                // Keep original raw resource name to avoid broken references
                imageUri = existingLesson.getImageUrl();
            } else {
                imageUri = selectedImageUri.toString();
            }

            // Handle video URI - keep original name for raw resources
            if (selectedVideoUri.toString().startsWith("android.resource://") &&
                    existingLesson.getVideoUrl() != null &&
                    !existingLesson.getVideoUrl().startsWith("android.resource://")) {
                videoUri = existingLesson.getVideoUrl();
            } else {
                videoUri = selectedVideoUri.toString();
            }

            // Handle text URI - keep original name for raw resources
            if (selectedTextUri.toString().startsWith("android.resource://") &&
                    existingLesson.getTextFileUrl() != null &&
                    !existingLesson.getTextFileUrl().startsWith("android.resource://")) {
                textUri = existingLesson.getTextFileUrl();
            } else {
                textUri = selectedTextUri.toString();
            }
        } else {
            // Creating new lesson - use URIs as they are
            videoUri = selectedVideoUri.toString();
            textUri = selectedTextUri.toString();
            imageUri = selectedImageUri.toString();
        }

        String selectedLevel = spnLevel.getSelectedItem().toString();
        String trainerName = getIntent().getStringExtra("username");

        if (lessonId != -1 && existingLesson != null){
            // Update existing lesson with new data
            existingLesson.setTitle(lessonName);
            existingLesson.setDescription(shortDescription);
            existingLesson.setVideoUrl(videoUri);
            existingLesson.setTextFileUrl(textUri);
            existingLesson.setImageUrl(imageUri);
            existingLesson.setLevel(selectedLevel);
            lessonViewModel.update(existingLesson);
        }
        else {
            // Create and insert new lesson
            Lesson lesson = new Lesson(lessonName, imageUri, shortDescription, videoUri, textUri, selectedLevel, false, false, trainerName);
            lessonViewModel.insert(lesson);
        }
    }

    // Keep access permission for picked URIs
    @SuppressWarnings("WrongConstant")
    private void persistUriPermission(Intent data, Uri uri) {
        if (data == null || uri == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final int takeFlags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            try {
                getContentResolver().takePersistableUriPermission(uri, takeFlags);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sets spinner selection to match specific value
     * Used when loading existing lesson data for editing
     * @param spinner The spinner component to update
     * @param value The value to select in the spinner
     */
    private void setSpinnerSelection(Spinner spinner, String value){
        // Loop through all spinner options to find matching value
        for (int i = 0; i<spinner.getCount(); i++){
            if (spinner.getItemAtPosition(i).toString().equals(value)){
                spinner.setSelection(i);
                break; // Exit loop once match is found
            }
        }
    }

    /**
     * Copies selected file from external storage to app's internal storage
     * This ensures files remain accessible even if original is deleted from device
     * Critical for app persistence - users can delete original files without breaking lessons
     * @param sourceUri URI of the source file from device storage
     * @param prefix Prefix for the copied file name (video, text, image)
     * @param extension File extension for the copied file
     * @return Absolute path of the copied file in internal storage, or null if failed
     */
    private String copyFileToInternalStorage(Uri sourceUri, String prefix, String extension) {
        // Create unique filename with timestamp to avoid conflicts
        String fileName = prefix + "_" + System.currentTimeMillis() + "." + extension;
        File destinationFile = new File(getFilesDir(), fileName);

        try (InputStream in = getContentResolver().openInputStream(sourceUri);
             OutputStream out = new FileOutputStream(destinationFile)) {

            // Copy file in chunks for memory efficiency
            byte[] buffer = new byte[4096];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

            return destinationFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Helper method to identify if video URL represents a raw resource
     * Used to determine correct video loading method
     * @param videoUrl The video URL to check
     * @return true if it's a raw resource, false if it's an internal storage file
     */
    private boolean isRawResource(String videoUrl) {
        return videoUrl != null &&
                (videoUrl.startsWith("android.resource://") ||
                        (!videoUrl.contains("/") && !videoUrl.startsWith("file://")));
    }

    /**
     * Plays video based on its type (raw resource vs internal storage)
     * Uses appropriate method for each video type to ensure proper playback
     * @param videoUrl The video URL to play
     */
    private void playVideo(String videoUrl) {
        VideoView videoView = findViewById(R.id.videoPreview);
        if (videoView == null) return;

        if (isRawResource(videoUrl)) {
            // Raw resource video - use setVideoURI method
            try {
                if (!videoUrl.contains("/")) {
                    // Resource name only - create full URI
                    int resourceId = getResources().getIdentifier(videoUrl, "raw", getPackageName());
                    selectedVideoUri = Uri.parse("android.resource://" + getPackageName() + "/" + resourceId);
                } else {
                    selectedVideoUri = Uri.parse(videoUrl);
                }
                videoView.setVideoURI(selectedVideoUri);
            } catch (Exception e) {
                Log.e("AddLessonActivity", "Error loading raw video: " + e.getMessage());
                return;
            }
        } else {
            // Internal storage video - use setVideoPath method
            if (new File(videoUrl).exists()) {
                selectedVideoUri = Uri.fromFile(new File(videoUrl));
                videoView.setVideoPath(videoUrl);
            } else {
                Log.e("AddLessonActivity", "Video file not found: " + videoUrl);
                return;
            }
        }

        videoView.setVisibility(View.VISIBLE);
        videoView.start();
    }
}
