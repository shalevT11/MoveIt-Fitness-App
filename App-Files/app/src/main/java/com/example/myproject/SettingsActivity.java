package com.example.myproject;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;

/**
 * Settings Activity for user profile management
 * Allows users to update their personal information including:
 * - Role (Coach/Trainee)
 * - Difficulty level preference
 * - Phone number
 * - Age
 * - Gender
 * Features form validation and returns to lessons with updated preferences
 */
public class SettingsActivity extends AppCompatActivity {

    // Role selection buttons for changing user type
    private MaterialButton btnCoach; // Coach/Trainer role button
    private MaterialButton btnTrainee; // Trainee role button

    // Navigation and action buttons
    private ImageButton btnBack; // Back to lessons without saving
    private ImageButton btnSave; // Save changes and return to lessons

    // Form input components for user profile data
    private Spinner spnLevel; // Difficulty level preference spinner
    private EditText etPhone; // Phone number input field
    private EditText etAge; // Age input field
    private Spinner spnGender; // Gender selection spinner

    // Current user object for updating profile information
    private User currentUser;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        // Configure status bar appearance for Android M and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR); // אייקונים כהים
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        // Initialize UI components by connecting to XML layout elements
        btnCoach = findViewById(R.id.btnCoach);
        btnTrainee = findViewById(R.id.btnTrainee);
        btnBack = findViewById(R.id.btnBack); //back to lessons
        spnLevel = findViewById(R.id.spnLevel); //level of trainer
        etPhone = findViewById(R.id.etPhone); //phone number
        etAge = findViewById(R.id.etAge); //age
        spnGender = findViewById(R.id.spnGender); //gender

        // Load current user data and populate form fields
        UserViewModel userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        long userId = getSharedPreferences("my_prefs", MODE_PRIVATE).getLong("userId", -1);

        // Load user data if valid user ID exists
        if (userId != -1){
            userViewModel.getUserById(userId).observe(this, user -> {
                if (user != null) {
                        currentUser = user;

                        // Set role selection based on current user role
                        String role = currentUser.getRole();
                        if (role.equals("מתאמן")){ // If user is trainee
                            btnTrainee.setSelected(true);
                            btnCoach.setSelected(false);
                        }
                        else { // If user is coach
                            btnCoach.setSelected(true);
                            btnTrainee.setSelected(false);
                        }

                    // Populate phone field if user has phone number saved
                        if(currentUser.getPhone() != null){
                            etPhone.setText(currentUser.getPhone());
                        }

                    // Populate age field if user has age saved
                        if(currentUser.getAge() != null){
                            etAge.setText(String.valueOf(currentUser.getAge()));
                        }

                    // Set gender spinner selection if user has gender saved
                        if(currentUser.getGender() != null){
                            setSpinnerSelection(spnGender, currentUser.getGender());
                        }

                    // Set level spinner selection if user has level preference saved
                        if(currentUser.getLevel() != null){
                            setSpinnerSelection(spnLevel, currentUser.getLevel());
                        }
                }
            });
        }

        // Setup role selection buttons with mutual exclusion logic
        // Only one role can be selected at a time (coach OR trainee)
        btnTrainee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Select trainee button and deselect coach button
                btnTrainee.setSelected(true);
                btnCoach.setSelected(false);
            }
        });

        btnCoach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Select coach button and deselect trainee button
                btnCoach.setSelected(true);
                btnTrainee.setSelected(false);
            }
        });

        // Setup back button to close settings without saving changes
        btnBack.setOnClickListener(v->{
            finish();
        });


        // Setup save button with validation and user update functionality
        btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v-> {
            //update the user DB

            // Get input values and trim whitespace
            String ageStr = etAge.getText().toString().trim();
            String phoneStr = etPhone.getText().toString().trim();

            // Validate age input if provided
            if (!ageStr.isEmpty()) {
                int age = Integer.parseInt(ageStr);
                // Check if age is within reasonable range (10-130)
                if (age < 10 || age > 130) {
                    Toast.makeText(SettingsActivity.this, "נא להזין גיל בין 10-130", Toast.LENGTH_SHORT).show();
                    etAge.requestFocus();
                    return; // Stop execution if validation fails
                }
            }

            // Validate phone number format if provided
            if (!phoneStr.isEmpty()) {
                // Check if phone starts with "05" and has exactly 10 digits (Israeli mobile format)
                if (!phoneStr.startsWith("05") || phoneStr.length() != 10) {
                    Toast.makeText(SettingsActivity.this, "נא להזין מספר פלאפון תקין", Toast.LENGTH_SHORT).show();
                    etPhone.requestFocus();
                    return; // Stop execution if validation fails
                }
            }

            // All validations passed - proceed with user update
            if (currentUser == null) return;

            // Update phone number if provided
            if (!phoneStr.isEmpty()) {
                currentUser.setPhone(phoneStr);
            }

            // Update age if provided
            if (!ageStr.isEmpty()) {
                currentUser.setAge(Integer.parseInt(ageStr));
            }

            // Update level and gender from spinner selections
            currentUser.setLevel(spnLevel.getSelectedItem().toString());
            currentUser.setGender(spnGender.getSelectedItem().toString());

            // Update role based on button selection
            if (btnCoach.isSelected()) {
                currentUser.setRole("מאמן"); // Coach
            } else {
                currentUser.setRole("מתאמן"); // Trainee
            }

            // Update user in database on background thread to avoid blocking UI
            new Thread(() -> userViewModel.update(currentUser)).start();

            // Navigate back to lessons activity with updated level preference
            Intent intent = new Intent(SettingsActivity.this, LessonsActivity.class);
            intent.putExtra("level", spnLevel.getSelectedItem().toString()); // Pass selected level
            startActivity(intent);
            finish(); // Close settings activity

        });
    }

    /**
     * Helper method to set spinner selection to match a specific value
     * Used when loading existing user data into spinner components
     * @param spinner The spinner component to set selection for
     * @param value The value to select in the spinner
     */
    private void setSpinnerSelection(Spinner spinner, String value){
        // Loop through all spinner items to find matching value
        for (int i = 0; i<spinner.getCount(); i++){
            if (spinner.getItemAtPosition(i).toString().equals(value)){
                spinner.setSelection(i); // Set selection to matching item
                break; // Exit loop once match is found
            }
        }
    }
}
