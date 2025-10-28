package com.example.myproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import java.util.concurrent.Future;

/**
 * Main Activity for user registration and onboarding
 * Handles user registration flow including:
 * - Username input
 * - Role selection (Coach/Trainee)
 * - Health agreement acceptance via popup
 * - User creation and navigation to lessons
 * Features clickable text for health agreement and form validation
 */
public class MainActivity extends AppCompatActivity {

    // UI component for displaying health agreement text with clickable link
    private TextView tvHealthAgg;

    // Continue button that appears after health agreement is accepted
    private ImageButton btnContinue;


    // Role selection buttons for user type
    private MaterialButton btnCoach; // Coach/Trainer role button
    private MaterialButton btnTrainee; // Trainee role button


    // ViewModel for database operations on User entities
    private UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Configure status bar appearance for Android M and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //status bar icons black
            View decor = getWindow().getDecorView();
            decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        super.onCreate(savedInstanceState);

        // Hide the default action bar for cleaner registration interface
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize ViewModel for user database operations
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);


        setContentView(R.layout.activity_main);

        // Initialize UI components by connecting to XML layout elements
        tvHealthAgg = findViewById(R.id.tvHealthAgg);
        EditText etUserName = findViewById(R.id.etUserName); //the UserName
        btnContinue = findViewById(R.id.btnContinue); //match the ID of the btn
        //the choose in buttons
        btnCoach = findViewById(R.id.btnCoach);
        btnTrainee = findViewById(R.id.btnTrainee);

        // Hide continue button initially - only show after health agreement acceptance
        btnContinue.setVisibility(View.GONE);


        // Create clickable text for health agreement
        // Define the full text to be displayed
        String fullText = "אשר את הסכם הבריאות שלנו.";
        SpannableString spannableString = new SpannableString(fullText);


        // Define which specific words should be clickable
        String clickablePart="הסכם הבריאות"; //clickable words
        int startIndex = fullText.indexOf(clickablePart);
        int endIndex = startIndex + clickablePart.length();

        // Make the clickable words blue to indicate they are interactive
        spannableString.setSpan(new ForegroundColorSpan(Color.BLUE),
                startIndex,
                endIndex,
                Spanned.SPAN_COMPOSING);

        // Add click functionality to open health agreement popup when clicked
        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                showHealthPopup();
            }
        },
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );


        // Apply the spannable text to TextView and enable link movement for clicks
        tvHealthAgg.setText(spannableString);
        tvHealthAgg.setMovementMethod(LinkMovementMethod.getInstance());

        // Setup role selection buttons with mutual exclusion logic
        // Only one button can be selected at a time (coach OR trainee)
        btnTrainee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Select trainee button and deselect coach button
                btnTrainee.setSelected(true);
                btnCoach.setSelected(false);
            }
        });btnCoach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Select coach button and deselect trainee button
                btnCoach.setSelected(true);
                btnTrainee.setSelected(false);
            }
        });



        // Setup continue button with comprehensive form validation
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Validate that username is not empty
                if (etUserName.getText().toString().isEmpty()){ //check if there is a name
                    Toast.makeText(MainActivity.this, "אנא הכנס שם", Toast.LENGTH_LONG).show();
                    etUserName.requestFocus();
                }
                // Validate that a role has been selected
                else if (!btnCoach.isSelected() && !btnTrainee.isSelected()) {//check that the user choose the job
                    Toast.makeText(MainActivity.this, "אנא בחר תפקיד", Toast.LENGTH_LONG).show();

                } else {
                    // All validations passed - proceed with user creation

                    // Get username from input field
                    String name = etUserName.getText().toString(); //the userName

                    // Determine selected role based on button selection
                    String role;
                    if (btnCoach.isSelected()) {
                        role = "מאמן";  // Coach
                    } else {
                        role = "מתאמן";  // Trainee
                    }

                    // Create new user object with entered data
                    User user = new User(name, role);

                    // Insert user into database asynchronously and get future user ID
                    Future<Long> futureUserId = userViewModel.insert(user);

                    // Handle database operation in background thread to avoid blocking UI
                    new Thread(()->{
                        try {
                            // Wait for database insertion to complete and retrieve user ID
                            long userId = futureUserId.get();

                            // Save user ID to shared preferences for future app sessions
                            getSharedPreferences("my_prefs", MODE_PRIVATE)
                                    .edit()
                                    .putLong("userId", userId)
                                    .apply();

                            // Navigate to lessons activity on UI thread
                            runOnUiThread(()->{
                                //crate an intent, because we goes to another activity
                                Intent intent = new Intent(MainActivity.this, com.example.myproject.LessonsActivity.class);
                                //start the intent
                                startActivity(intent);
                                finish(); // Close registration activity
                            });
                        } catch (Exception e){
                            // Handle any errors during user creation process
                            e.printStackTrace();
                            runOnUiThread(()->{
                                Toast.makeText(MainActivity.this, "שגיאה בשמירת המשתמש", Toast.LENGTH_LONG).show();
                            });
                        }
                    }).start();

                }
            }
        });
    }



    /**
     * Displays health agreement popup window
     * Contains detailed health agreement text and checkbox for user acceptance
     * Only allows continuation after user explicitly checks the agreement
     */
    private void showHealthPopup(){
        // Inflate the popup layout from XML
        View popupView = LayoutInflater.from(this).inflate(R.layout.health_agreement,null);

        // Create popup window with appropriate size and behavior
        PopupWindow popupWindow = new PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT, // Width wraps content
                LinearLayout.LayoutParams.WRAP_CONTENT, // Height wraps content
                true); // Focusable - allows interaction

        // Display popup centered on screen above current content
        popupWindow.showAtLocation(tvHealthAgg, Gravity.CENTER,0,0);

        // Get references to popup UI components
        //the continue btn
        ImageButton imgBtnCon = popupView.findViewById(R.id.imgBtnCon);
        //checkbox to the agreement
        CheckBox checkboxHealthAgreement = popupView.findViewById(R.id.checkboxHealthAgreement);

        // Setup continue button behavior within popup
        imgBtnCon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if user has accepted the health agreement
                if (checkboxHealthAgreement.isChecked()){
                    // Agreement accepted - close popup and enable main continue button
                    popupWindow.dismiss(); //back to the welcome page
                    btnContinue.setVisibility(View.VISIBLE); //make the btn visible

                }
                else {
                    // Agreement not checked - show warning message
                    Toast.makeText(getApplicationContext(),"אנא אשר את הסכם הבריאות",Toast.LENGTH_SHORT).show();
                    checkboxHealthAgreement.requestFocus();
                }
            }
        });

    }
}