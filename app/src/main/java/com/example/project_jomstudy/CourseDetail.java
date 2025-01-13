package com.example.project_jomstudy;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.project_jomstudy.databinding.ActivityCourseDetailBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class CourseDetail extends AppCompatActivity {

    ActivityCourseDetailBinding binding;
    String courseCode, courseName, instructorName, instructorEmail, day, startTime, endTime;

    ImageView backButton;
    Button editButton, deleteButton;

    private static final int REQUEST_CODE_EDIT_COURSE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_course_detail);

        // Check if the user is authenticated
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            // No user is authenticated, redirect to login activity
            Intent intent = new Intent(CourseDetail.this, LogIn.class);
            startActivity(intent);
            finish(); // Close the current activity
            return; // Prevent further execution
        }


        binding = ActivityCourseDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        courseCode = getIntent().getStringExtra("Course");
        courseName = getIntent().getStringExtra("CourseName");
        instructorName = getIntent().getStringExtra("InstructorName");
        instructorEmail = getIntent().getStringExtra("InstructorEmail");
        day = getIntent().getStringExtra("Day");
        startTime = getIntent().getStringExtra("StartTime");
        endTime = getIntent().getStringExtra("EndTime");


        binding.courseCode.setText(courseCode);
        binding.courseName.setText(courseName);
        binding.instructorName.setText(instructorName);
        binding.instructorEmail.setText(instructorEmail);
        binding.day.setText(day);
        binding.startTime.setText(startTime);
        binding.endTime.setText(endTime);

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        editButton = findViewById(R.id.edit_button);
        editButton.setOnClickListener(v -> passCourseData());

        deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_EDIT_COURSE && resultCode == RESULT_OK) {
            // Get updated data from the intent
            String updatedCourseName = data.getStringExtra("CourseName");
            String updatedCourseCode = data.getStringExtra("Course");
            String updatedInstructorName = data.getStringExtra("InstructorName");
            String updatedInstructorEmail = data.getStringExtra("InstructorEmail");
            String updatedDay = data.getStringExtra("Day");
            String updatedStartTime = data.getStringExtra("StartTime");
            String updatedEndTime = data.getStringExtra("EndTime");

            // Update the UI with the new data using binding
            binding.courseName.setText(updatedCourseName);
            binding.courseCode.setText(updatedCourseCode);
            binding.instructorName.setText(updatedInstructorName);
            binding.instructorEmail.setText(updatedInstructorEmail);
            binding.day.setText(updatedDay);
            binding.startTime.setText(updatedStartTime);
            binding.endTime.setText(updatedEndTime);
        }
    }


    public void passCourseData() {
        String courseCode = binding.courseCode.getText().toString().trim();
        String courseName = binding.courseName.getText().toString().trim();
        String instructorName = binding.instructorName.getText().toString().trim();
        String instructorEmail = binding.instructorEmail.getText().toString().trim();
        String day = binding.day.getText().toString().trim();
        String startTime = binding.startTime.getText().toString().trim();
        String endTime = binding.endTime.getText().toString().trim();

        Intent intent = new Intent(CourseDetail.this, EditCourse.class);
        intent.putExtra("Course", courseCode);
        intent.putExtra("CourseName", courseName);
        intent.putExtra("InstructorName", instructorName);
        intent.putExtra("InstructorEmail", instructorEmail);
        intent.putExtra("Day", day);
        intent.putExtra("StartTime", startTime);
        intent.putExtra("EndTime", endTime);

        // Start EditCourse activity with the intent
        startActivityForResult(intent, REQUEST_CODE_EDIT_COURSE);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog to confirm the deletion
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Delete Course")
                .setMessage("Are you sure you want to delete this course?")
                .setPositiveButton("Yes", (dialogInterface, which) -> {
                    deleteCourse();
                })
                .setNegativeButton("No", null)
                .show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(18);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(18);
    }

    private void deleteCourse() {
        String courseCodeToDelete = binding.courseCode.getText().toString().trim();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // User is not authenticated, redirect to login
            Intent intent = new Intent(CourseDetail.this, LogIn.class);
            startActivity(intent);
            finish();
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("users")
                .child(currentUser.getUid()) // Use the authenticated user's UID
                .child("Courses");

        Query query = ref.orderByChild("courseCode").equalTo(courseCodeToDelete);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                        // Delete the course node from the database
                        courseSnapshot.getRef().removeValue().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(CourseDetail.this, "Course deleted successfully", Toast.LENGTH_SHORT).show();
                                finish(); // Close the activity after deletion
                            } else {
                                Toast.makeText(CourseDetail.this, "Failed to delete course", Toast.LENGTH_SHORT).show();
                            }
                        });
                        break; // Exit the loop once the course is deleted
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Handle database error
                Toast.makeText(CourseDetail.this, "Error occurred: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}