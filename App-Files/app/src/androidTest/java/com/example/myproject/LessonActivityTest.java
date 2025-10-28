package com.example.myproject;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class LessonActivityTest {

    @Rule
    public ActivityScenarioRule<LessonsActivity> activityRule =
            new ActivityScenarioRule<>(LessonsActivity.class);

    @Test
    public void testEditLesson_andReturnToLessonsActivity() {
        // Perform long click on the first item in the RecyclerView
        onView(withId(R.id.recyclerview))
                .perform(androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition(0, longClick()));

        // Verify edit screen fields are displayed and not empty
        onView(withId(R.id.etLessonName))
                .check(matches(isDisplayed()))
                .check(matches(withText(not(""))));

        onView(withId(R.id.etShortDis))
                .check(matches(isDisplayed()))
                .check(matches(withText(not(""))));

        onView(withId(R.id.spnLevel))
                .check(matches(isDisplayed()));

        // Edit the lesson name
        String newLessonName = "אימון - ערוך";
        onView(withId(R.id.etLessonName))
                .perform(replaceText(newLessonName));

        // Click the save button to save changes
        onView(withId(R.id.btnSave))
                .perform(click());


        // Add a short delay to make the transition noticeable
        try {
            Thread.sleep(2000); // 2-second delay for visibility
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Retry checking the RecyclerView with a short delay
        for (int i = 0; i < 3; i++) {
            try {
                // Verify RecyclerView is displayed
                onView(withId(R.id.recyclerview))
                        .check(matches(isDisplayed()));

                // Verify the updated lesson name appears in the RecyclerView
                onView(withId(R.id.recyclerview))
                        .check(matches(hasDescendant(withText(newLessonName))));

                break; // Exit loop if successful
            } catch (Exception e) {
                // Wait briefly before retrying
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }
    }
}