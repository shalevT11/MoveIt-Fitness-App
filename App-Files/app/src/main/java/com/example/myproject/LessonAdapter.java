package com.example.myproject;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


/**
 * RecyclerView Adapter for displaying lesson cards in a list
 * Handles lesson data binding, user interactions (favorites, registration),
 * and navigation to lesson details. Supports both raw resources and user-uploaded content.
 */

public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.LessonViewHolder> {

    private List<Lesson> lessonList;
    private OnFavoriteClickListener favoriteClickListener;
    private OnCheckboxChangedListener checkboxChangedListener;
    private Context context;

    /**
     * Interface for handling long click events on lesson items
     * Used for lesson management actions (edit, delete)
     */
    public interface OnLongClickListener {
        void onLongClick(Lesson lesson);
    }

    private OnLongClickListener onLongClickListener;

    /**
     * Sets the long click listener for lesson items
     * @param listener The listener to handle long click events
     */
    public void setOnLongClickListener(OnLongClickListener listener) {
        this.onLongClickListener = listener;
    }



    /**
     * Constructor
     * @param context Application context for UI operations
     */
    public LessonAdapter(Context context) {
        this.context = context;
    }


    /**
     * Interface for handling favorite button clicks
     * Allows parent components to respond to favorite status changes
     */
    public interface OnFavoriteClickListener{
        void onFavoriteClick(int position, boolean isChecked);
    }


    /**
     * Interface for handling checkbox state changes
     * Used for lesson registration status updates
     */
    public interface OnCheckboxChangedListener{
        void onCheckboxChanged(int position, boolean isChecked);
    }

    /**
     * Sets the favorite click listener
     * @param listener The listener to handle favorite button clicks
     */
    public void setOnFavoriteClickListener(OnFavoriteClickListener listener){
        this.favoriteClickListener = listener;
    }

    /**
     * Sets the checkbox change listener
     * @param listener The listener to handle checkbox state changes
     */
    public void setOnCheckboxChangedListener(OnCheckboxChangedListener listener){
        this.checkboxChangedListener = listener;
    }

    /**
     * Updates the lesson list and refreshes the UI
     * @param lessonList New list of lessons to display
     */
    public void setLessons(List<Lesson> lessonList){
        this.lessonList = lessonList;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class that holds references to the views in each lesson card
     * Contains UI elements: checkbox, image, lesson name, description, and favorite button
     */
    public static class LessonViewHolder extends RecyclerView.ViewHolder{
        CheckBox checkBox; // Lesson completion checkbox
        ImageView roundedImage; // Lesson thumbnail image
        TextView lessonName; // Lesson title
        TextView lessonDescription; // Lesson short description
        CheckBox fav_button; // Favorite toggle button

        /**
         * Constructor for ViewHolder
         * Initializes all view references from the item layout
         * @param itemView The view for individual lesson card
         */
        public LessonViewHolder(@NonNull View itemView){
            super(itemView);
            checkBox=itemView.findViewById(R.id.checkBox);
            roundedImage=itemView.findViewById(R.id.roundedImage);
            lessonName = itemView.findViewById(R.id.lessonName);
            lessonDescription = itemView.findViewById(R.id.lessonDescription);
            fav_button = itemView.findViewById(R.id.fav_button);
        }
    }

    /**
     * Creates new ViewHolder when RecyclerView needs a new item view
     * @param parent The ViewGroup into which the new View will be added
     * @param viewType The view type of the new View
     * @return A new ViewHolder that holds a View of the given view type
     */
    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_item, parent, false);
        return new LessonViewHolder(v);
    }

    /**
     * Binds lesson data to the ViewHolder views
     * Sets up UI elements with lesson information and configures click listeners
     * @param holder The ViewHolder to bind data to
     * @param position The position of the item within the adapter's data set
     */
    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position){
        Lesson lesson = lessonList.get(position);

        // Set lesson text content
        holder.lessonName.setText(lesson.getTitle());
        holder.lessonDescription.setText(lesson.getDescription());


        String imageUrl = lesson.getImageUrl();

        if (imageUrl.startsWith("content://") || imageUrl.startsWith("file://")){
            // User-uploaded image from device storage
            holder.roundedImage.setImageURI(Uri.parse(imageUrl));
        }
        else {
            // Raw resource image from app drawable folder
            int imageResId = holder.itemView.getContext().getResources()
                    .getIdentifier(lesson.getImageUrl(), "drawable", holder.itemView.getContext().getPackageName());

            //check why pic don't shows
            if (imageResId != 0) {
                holder.roundedImage.setImageResource(imageResId);
            } else {
                Log.e("Adapter", "Image resource not found for: " + lesson.getImageUrl());
            }
        }

        //img_miss

/*
        if (!lesson.getRegistered()){
            holder.checkBox.setVisibility(View.GONE);
        }
        else {
            holder.checkBox.setVisibility(View.VISIBLE);
        }
*/

        // Handle checkbox click - prevent direct changes, show informative message
        holder.checkBox.setOnClickListener(v ->{
            holder.checkBox.setChecked(lesson.getRegistered());
            if (holder.checkBox.isChecked()){
                Toast.makeText(context, "ניתן לבטל סימון רק מתוך השיעור עצמו", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(context, "ניתן לסמן רק מתוך השיעור עצמו", Toast.LENGTH_SHORT).show();

            }
        });

        // Set current registration status
        holder.checkBox.setChecked(lesson.getRegistered()); //the current

        // Set change listener for programmatic updates
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (checkboxChangedListener != null) {
                checkboxChangedListener.onCheckboxChanged(position, isChecked);
            }
        });

        // Remove previous listener to prevent unwanted triggers
        holder.fav_button.setOnCheckedChangeListener(null); //cancel previous listener

        // Set current favorite status
        holder.fav_button.setChecked(lesson.getFavorites()); //the current value

        // Set new listener for favorite status changes
        holder.fav_button.setOnCheckedChangeListener(((buttonView, isChecked) -> {
            if (favoriteClickListener != null){
                favoriteClickListener.onFavoriteClick(position, isChecked);
            }
        }));

        //Sets up click listener for lesson item to navigate to lesson details
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, LessonDetailsActivity.class);
            intent.putExtra("lessonId", lesson.getId()); // Pass only lesson ID
            context.startActivity(intent);
        });

        if (onLongClickListener != null) {
            holder.itemView.setOnLongClickListener(v -> {
                onLongClickListener.onLongClick(lesson);
                return true; // Consume the long click event
            });
        } else {
            holder.itemView.setOnLongClickListener(null);
        }


    }

    /**
     * Returns the total number of items in the data set
     * @return Size of lesson list, or 0 if list is null
     */
    @Override
    public int getItemCount() {
        return lessonList == null ? 0 : lessonList.size();
    }

    /**
     * Gets lesson object at specific position
     * @param position Position in the lesson list
     * @return Lesson object at the specified position
     */
    public Lesson getLessonAt(int position) {
        return lessonList.get(position);
    }

}
