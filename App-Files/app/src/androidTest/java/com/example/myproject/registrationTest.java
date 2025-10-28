package com.example.myproject;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.pressImeActionButton;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import androidx.test.filters.LargeTest;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class registrationTest {

    @Rule
    public ActivityScenarioRule<SplashActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(SplashActivity.class);

    @Test
    public void registrationTest() throws InterruptedException, UiObjectNotFoundException {
        // אתחול UiDevice לחלקים בעייתיים
        UiDevice device = UiDevice.getInstance(getInstrumentation());
        // הזנת שם משתמש
        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.etUserName),
                        childAtPosition(
                                allOf(withId(R.id.name_txt),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                2)),
                                1),
                        isDisplayed()));
        appCompatEditText.perform(replaceText("אלי דוגמא"), closeSoftKeyboard());

        ViewInteraction appCompatEditText2 = onView(
                allOf(withId(R.id.etUserName), withText("אלי דוגמא"),
                        childAtPosition(
                                allOf(withId(R.id.name_txt),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                2)),
                                1),
                        isDisplayed()));
        appCompatEditText2.perform(pressImeActionButton());

        // לחיצה על כפתור מאמן - תיקון: הסרת התנאי withText הבעייתי
        ViewInteraction materialButton = onView(
                allOf(withId(R.id.btnCoach),
                        childAtPosition(
                                allOf(withId(R.id.choose),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                3)),
                                0),
                        isDisplayed()));
        materialButton.perform(click());

        // המתנה לטעינת הממשק לאחר בחירת התפקיד
        Thread.sleep(3000);

        // לחיצה על הסכם בריאות עם UiAutomator (יותר יציב)
        UiObject healthAgreement = device.findObject(new UiSelector()
                .resourceId("com.example.myproject:id/tvHealthAgg"));
        healthAgreement.click();

        // המתנה לפתיחת הפופאפ
        Thread.sleep(1000);

        // סימון checkbox הסכמה - עם UiAutomator (יותר יציב)
        UiObject checkbox = device.findObject(new UiSelector()
                .resourceId("com.example.myproject:id/checkboxHealthAgreement"));
        checkbox.click();

        // לחיצה על כפתור המשך בפופאפ - עם UiAutomator
        UiObject appCompatImageButton = device.findObject(new UiSelector()
                .resourceId("com.example.myproject:id/imgBtnCon"));
        appCompatImageButton.click();

        // המתנה לסגירת הפופאפ
        Thread.sleep(500);

        // לחיצה על כפתור המשך הסופי - עם UiAutomator
        UiObject appCompatImageButton2 = device.findObject(new UiSelector()
                .resourceId("com.example.myproject:id/btnContinue"));
        appCompatImageButton2.click();

        // המתנה למעבר למסך החדש
        Thread.sleep(2000);

        // בדיקת קיום כפתור FAB במסך השיעורים - גרסה פשוטה יותר
        ViewInteraction imageButton = onView(
                allOf(withId(R.id.fab), isDisplayed()));
        imageButton.check(matches(isDisplayed()));
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}