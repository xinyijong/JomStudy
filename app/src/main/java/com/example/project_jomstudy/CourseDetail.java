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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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
        backButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        editButton = findViewById(R.id.edit_button);
        editButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                passCourseData();
            }
        });

        deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_EDIT_COURSE && resultCode == RESULT_OK) {
            // Get updated data from the intent
            String updatedCourseName = data.getStringExtra("CourseName");
            String updatedCourseCode = data.getStringExtra("CourseCode");
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

    public void passCourseData(){
        String courseCode = binding.courseCode.getText().toString().trim();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Courses");
        Query checkCourseDatabase = ref.orderByChild("courseCode").equalTo(courseCode);

        checkCourseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                        String courseCodeDB = courseSnapshot.child("courseCode").getValue(String.class);
                        String courseNameDB = courseSnapshot.child("courseName").getValue(String.class);
                        String instructorNameDB = courseSnapshot.child("instructorName").getValue(String.class);
                        String instructorEmailDB = courseSnapshot.child("instructorEmail").getValue(String.class);
                        String dayDB = courseSnapshot.child("day").getValue(String.class);
                        String startTimeDB = courseSnapshot.child("startTime").getValue(String.class);
                        String endTimeDB = courseSnapshot.child("endTime").getValue(String.class);

                        Intent intent = new Intent(CourseDetail.this, EditCourse.class);
                        intent.putExtra("Course", courseCodeDB);
                        intent.putExtra("CourseName", courseNameDB);
                        intent.putExtra("InstructorName", instructorNameDB);
                        intent.putExtra("InstructorEmail", instructorEmailDB);
                        intent.putExtra("Day", dayDB);
                        intent.putExtra("StartTime", startTimeDB);
                        intent.putExtra("EndTime", endTimeDB);

                        startActivityForResult(intent, REQUEST_CODE_EDIT_COURSE);
                        break; // Exit the loop as we only need the first match.
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle the error
            }
        });
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

        // Now that the dialog is shown, you can access the buttons
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextSize(18);  // Increase text size of "Yes"
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextSize(18);  // Increase text size of "No"
    }

    private void deleteCourse() {
        String courseCodeToDelete = binding.courseCode.getText().toString().trim();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Courses");

        // Query for the course using the course code
        Query query = ref.orderByChild("courseCode").equalTo(courseCodeToDelete);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
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
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
                Toast.makeText(CourseDetail.this, "Error occurred: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}