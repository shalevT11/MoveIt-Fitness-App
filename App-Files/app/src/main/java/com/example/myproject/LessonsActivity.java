package com.example.myproject;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Main activity for displaying lessons organized by difficulty levels (tabs)
 * Features different functionality based on user role:
 * - Trainers: Can add, edit, and delete lessons via FAB and swipe/long-press gestures
 * - Trainees: Can only view and interact with lessons (favorites, registration)
 * Implements lesson filtering by difficulty level through tabs
 */
public class LessonsActivity extends AppCompatActivity {
    private TextView tvUserName; // Displays welcome message with username
    private LessonViewModel lessonViewModel; // ViewModel for database operations on lessons
    private LessonAdapter adapter; // RecyclerView adapter for displaying lessons
    private TabLayout tabLayout; // Tab layout for filtering by difficulty level
    private ImageButton btnMenu; // Menu button for accessing settings
    private FloatingActionButton fab; // Floating action button for adding lessons (trainers only)

    // State management variables
    private String userLevel = "מתחילים"; // Default level for new users (Beginners)
    private boolean isFirstLoad = true; // Flag to handle initial load behavior
    private TabLayout.OnTabSelectedListener tabSelectedListener;
    private RecyclerView recyclerview; // RecyclerView for displaying the lesson cards



    private List<Lesson> allLessons = new ArrayList<>(); // Complete list of lessons from database

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Configure status bar appearance for Android M and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lessons); //connect to the xml file

        // Initialize UI components by connecting to XML layout elements
        tvUserName = findViewById(R.id.tvUserName);
        fab = findViewById(R.id.fab);
        btnMenu = findViewById(R.id.btnMenu);

        // Configure RecyclerView for displaying lesson cards in a vertical list
        recyclerview = findViewById(R.id.recyclerview);
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LessonAdapter(this);

        // Set up favorite button click listener in lesson cards
        // When user clicks favorite, update lesson in database
        adapter.setOnFavoriteClickListener((position, isChecked) -> {
            Lesson lesson = adapter.getLessonAt(position);
            lesson.setFavorites(isChecked);
            lessonViewModel.update(lesson);
        });

        recyclerview.setAdapter(adapter);

        // Create the 3 difficulty level tabs: Beginners, Intermediate, Advanced
        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("מתחילים"));
        tabLayout.addTab(tabLayout.newTab().setText("בינוני"));
        tabLayout.addTab(tabLayout.newTab().setText("מתקדמים"));

        // Set up tab selection listener to filter lessons when user switches tabs
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Filter lessons to show only those matching selected difficulty level
                filterLessonsByLevel(tab.getText().toString());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Setup menu button to show popup menu with settings option
        btnMenu.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(LessonsActivity.this, v);
            popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_settings) {
                    // Navigate to settings activity when settings is clicked
                    startActivity(new Intent(LessonsActivity.this, SettingsActivity.class));
                    return true;
                }
                return false;
            });
            popup.show();
        });

        // Load user data and set up lesson observation
        observeUserAndLessons();


    }

    /**
     * Observes user data to determine role and permissions, then loads lessons
     * Sets up different functionality based on user role (trainer vs trainee)
     */
    private void observeUserAndLessons() {
        UserViewModel userViewModel = new ViewModelProvider(this).get(UserViewModel.class);

        // Get user ID from shared preferences
        long userId = getSharedPreferences("my_prefs", MODE_PRIVATE).getLong("userId", -1); //gets the user by the userId from shared-preferences

        if (userId != -1) {
            // Observe user data to get username, role, and preferred difficulty level
            userViewModel.getUserById(userId).observe(this, user ->{
                if (user != null) {
                    // Set user level, defaulting to beginners if not set
                    // This ensures lessons load properly even for new users
                    if (user.getLevel() == null){ //if it's null the level will be beginners, w/o it, the lessons don't load
                        userLevel = "מתחילים";
                    }
                    else {
                        userLevel = user.getLevel(); //gets the level of the user to set on the tabs
                    }

                    // Display welcome message with username
                    String name = user.getUsername(); //username shows on screen
                    String role = user.getRole(); //gets the role and if he is a trainer he will have the fab btn
                    tvUserName.setText("ברוך הבא: " + name);

                    // Configure UI based on user role
                    if (role.equals("מתאמן")) {
                        // Trainee: Hide FAB button (no lesson creation permission)
                        fab.setVisibility(View.GONE);
                    } else {
                        // Trainer: Enable full lesson management features

                        // Enable long click to edit lessons
                        adapter.setOnLongClickListener(lesson -> {
                            Intent intent = new Intent(LessonsActivity.this, AddLessonActivity.class);
                            intent.putExtra("lessonId", lesson.getId());
                            startActivity(intent);
                        });

                        // Add swipe-to-delete functionality for lesson management
                        // Trainers can swipe left or right on lesson cards to delete them
                        ItemTouchHelper helper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                                // Drag disabled (0 means no drag directions allowed)
                                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) { // Swipe enabled in both directions

                            @Override
                            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                                return false; // Drag functionality disabled
                            }

                            @Override
                            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                                // Get the position of the swiped item
                                int position = viewHolder.getAdapterPosition();
                                Lesson lessonToDelete = adapter.getLessonAt(position); //gets the lesson from the adapter

                                // Delete lesson from database through ViewModel
                                lessonViewModel.delete(lessonToDelete); //delete the lesson from view model

                                // Show confirmation toast to user
                                Toast.makeText(LessonsActivity.this, "השיעור "+lessonToDelete.getTitle()+" נמחק", Toast.LENGTH_SHORT).show();
                            }
                        });
                        // Attach the swipe helper to the RecyclerView
                        helper.attachToRecyclerView(recyclerview);

                        // Setup FAB click to add new lessons
                        fab.setOnClickListener(v -> {
                            Intent intent = new Intent(LessonsActivity.this, AddLessonActivity.class);
                            intent.putExtra("username", name); // Pass trainer name for lesson creation
                            startActivity(intent);
                        });
                    }
                }
                // Always observe lessons after user is loaded
                observeLessons();
            });
        } else {
            // If no user found in preferences, just load lessons with default settings
            observeLessons();
        }
    }

    /**
     * Observes all lessons from database and filters by selected tab or user level
     * Handles initial tab selection and maintains current selection on data updates
     */
    private void observeLessons() {
        lessonViewModel = new ViewModelProvider(this).get(LessonViewModel.class);
        lessonViewModel.getAllLessons().observe(this, lessons -> {
            // Store all lessons for filtering purposes
            allLessons = lessons;

            if (isFirstLoad) { //if its the first time
                // First time loading: set appropriate tab based on user level or settings intent
                String level = userLevel;
                Intent intent = getIntent();

                // Check if level was passed from Settings activity
                if (intent != null && intent.hasExtra("level")) { //gets the level from settings
                    level = intent.getStringExtra("level"); // בא מה־Settings
                }

                // Find and select the correct tab based on level
                for (int i = 0; i < tabLayout.getTabCount(); i++) {
                    TabLayout.Tab tab = tabLayout.getTabAt(i);
                    if (tab != null && tab.getText() != null && tab.getText().toString().equals(level)) {
                        tab.select();
                        break;
                    }
                }

                // Filter lessons by the selected level
                filterLessonsByLevel(level);
                isFirstLoad = false; // Prevent this logic from running again
            } else { //the else is for times he just moves between lesson
                // Subsequent loads: maintain current tab selection and re-filter
                TabLayout.Tab selectedTab = tabLayout.getTabAt(tabLayout.getSelectedTabPosition());
                if (selectedTab != null && selectedTab.getText() != null) {
                    filterLessonsByLevel(selectedTab.getText().toString());
                }
            }
        });
    }

    /**
     * Filters lessons by difficulty level and updates the adapter
     * Only shows lessons that match the specified difficulty level
     * @param level The difficulty level to filter by (מתחילים/בינוני/מתקדמים)
     */
    private void filterLessonsByLevel(String level) {
        List<Lesson> filtered = new ArrayList<>();

        // Go through all lessons and add only those matching the level
        for (Lesson lesson : allLessons) {
            if (lesson.getLevel().equals(level)) {
                filtered.add(lesson);
            }
        }

        // Update adapter with filtered lesson list
        adapter.setLessons(filtered);
    }
}