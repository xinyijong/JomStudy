package com.example.project_jomstudy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddCourse extends AppCompatActivity {

    EditText courseName;
    EditText courseCode;
    EditText instructorName;
    EditText instructorEmail;
    EditText day;
    EditText startTime;
    EditText endTime;
    Button addCourseButton;
    ImageView backButton;

    FirebaseDatabase rootNode;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_course);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        courseName = findViewById(R.id.courseName);
        courseCode = findViewById(R.id.courseCode);
        instructorName = findViewById(R.id.instructorName);
        instructorEmail = findViewById(R.id.instructorEmail);
        day = findViewById(R.id.day);
        startTime = findViewById(R.id.startTime);
        endTime = findViewById(R.id.endTime);
        addCourseButton = findViewById(R.id.addCourseButton);

        addCourseButton.setOnClickListener(v -> {
            if (validateInputs()) {
                // Show loading dialog
                AlertDialog loadingDialog = showLoadingDialog();

                rootNode = FirebaseDatabase.getInstance();
                reference = rootNode.getReference("Courses");

                String course = courseName.getText().toString().trim();
                String code = courseCode.getText().toString().trim();
                String instructor = instructorName.getText().toString().trim();
                String instructorE = instructorEmail.getText().toString().trim();
                String days = day.getText().toString().trim();
                String start = startTime.getText().toString().trim();
                String end = endTime.getText().toString().trim();

                CourseClass courseData = new CourseClass(course, code, instructorE, instructor, days, start, end);

                reference.child(code).setValue(courseData).addOnCompleteListener(task -> {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        loadingDialog.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Course saved successfully!", Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(AddCourse.this, Home.class);
                            intent.putExtra("navigateToFragment", "CourseFragment"); // Specify the fragment to show
                            intent.putExtra("selectedTab", R.id.course); // Specify the tab to select
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(this, "Course saving failed.", Toast.LENGTH_SHORT).show();
                        }
                    }, 1000); // Delay of 1 second
                });
            }
        });

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
    }

    private boolean validateInputs() {
        boolean isValid = true;

        if (courseName.getText().toString().trim().isEmpty()) {
            courseName.setError("Course name is required");
            isValid = false;
            courseName.requestFocus();
        }

        if (courseCode.getText().toString().trim().isEmpty()) {
            courseCode.setError("Course code is required");
            isValid = false;
            courseCode.requestFocus();
        }

        if (instructorName.getText().toString().trim().isEmpty()) {
            instructorName.setError("Instructor name is required");
            isValid = false;
            instructorName.requestFocus();
        }

        String email = instructorEmail.getText().toString().trim();
        if (email.isEmpty()) {
            instructorEmail.setError("Instructor email is required");
            isValid = false;
            instructorEmail.requestFocus();
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            instructorEmail.setError("Enter a valid email address");
            isValid = false;
            instructorEmail.requestFocus();
        }

        if (day.getText().toString().trim().isEmpty()) {
            day.setError("Day is required");
            isValid = false;
            day.requestFocus();
        }

        if (startTime.getText().toString().trim().isEmpty()) {
            startTime.setError("Start time is required");
            isValid = false;
            startTime.requestFocus();
        }

        if (endTime.getText().toString().trim().isEmpty()) {
            endTime.setError("End time is required");
            isValid = false;
            endTime.requestFocus();
        }

        return isValid;
    }

    private AlertDialog showLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.saving, null);
        builder.setView(dialogView);
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }
}