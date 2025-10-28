# MoveIt-Fitness-App
Personal fitness coaching Android app with role-based system (coach &amp; trainee), built with Java, Room, and Material Design.

# 🏋️ MoveIt – Personal Fitness Coaching App

**MoveIt** is an Android fitness coaching app designed to help users follow personalized workout programs.  
The app includes two user roles — **Coach** and **Trainee** — and provides a clean, intuitive interface for managing lessons, tracking progress, and saving favorites.

---

## 🚀 Features

- 👥 **Role-based system** — Separate interfaces and permissions for coaches and trainees.  
- 💪 **Workout management** — View, and complete workouts by difficulty level.  
- ❤️ **Favorites system** — Mark favorite workouts with a heart icon for quick access.  
- ✅ **Progress tracking** — Mark lessons as completed and track progress visually.  
- 💾 **Local data storage** — Uses Room Database and SharedPreferences for persistence.  
- 🎥 **Multimedia support** — Coaches can upload videos, images, and text guides for lessons.  
- 📱 **Modern UI** — Built with Material Design and optimized for Android devices.

---

## 🧩 Architecture

The app is built using the **MVVM (Model–View–ViewModel)** architecture pattern to ensure a clean separation between logic and UI.

**Main Components:**
- **Room Database** — Stores user and workout data locally.  
- **ViewModel & LiveData** — Handles reactive data updates.  
- **Repositories & DAOs** — Manage database access.  
- **SharedPreferences** — Saves user session data (e.g., `userId`, `role`).

---

## 🛠️ Technologies Used

- **Language:** Java  
- **Frameworks:** Android SDK, Room, LiveData, ViewModel  
- **UI:** XML, Material Design  
- **Database:** Room (SQLite)  
- **Storage:** Internal storage + SharedPreferences  

---

## 🧪 Testing

- ✅ **Unit Tests:** For Room Database entities and DAOs.  
- 🧍 **Manual UI Tests:** Performed across different Android devices.  
- 🤖 **Espresso Tests:** Automated interface testing.  
- 💬 **UX Testing:** Conducted with real users for feedback and improvements. 

---

## 📥 Installation

-  Download the APK from this repository.  
-  Allow installation from unknown sources on your Android device.  
-  Open the APK and install.  
-  Launch MoveIt and start your workouts! 💪

> © 2025 Shalev Turjeman. All rights reserved.
