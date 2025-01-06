package com.example.project_jomstudy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditCourse extends AppCompatActivity {

    EditText courseName;
    EditText courseCode;
    EditText instructorName;
    EditText instructorEmail;
    EditText day;
    EditText startTime;
    EditText endTime;
    String courseNameString;
    String courseCodeString;
    String instructorNameString;
    String instructorEmailString;
    String dayString;
    String startTimeString;
    String endTimeString;
    Button editCourseButton;
    ImageView backButton;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_course);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        reference = FirebaseDatabase.getInstance().getReference("Courses");

        courseName = findViewById(R.id.courseName);
        courseCode = findViewById(R.id.courseCode);
        courseCode.setEnabled(false);
        instructorName = findViewById(R.id.instructorName);
        instructorEmail = findViewById(R.id.instructorEmail);
        day = findViewById(R.id.day);
        startTime = findViewById(R.id.startTime);
        endTime = findViewById(R.id.endTime);
        editCourseButton = findViewById(R.id.edit_button);

        showData();

        editCourseButton.setOnClickListener(v -> {
            if (validateInputs()) {
                // Compare the new values with the original ones
                if (isDataUnchanged()) {
                    // Show toast message if no changes are made
                    Toast.makeText(EditCourse.this, "No changes made", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Show loading dialog while saving
                AlertDialog loadingDialog = showLoadingDialog();

                // Create an object with the updated course details
                CourseClass updatedCourseData = new CourseClass(
                        courseName.getText().toString(),
                        courseCode.getText().toString(),
                        instructorEmail.getText().toString(),
                        instructorName.getText().toString(),
                        day.getText().toString(),
                        startTime.getText().toString(),
                        endTime.getText().toString()
                );

                // Save the changes to Firebase
                reference.child(courseCodeString).setValue(updatedCourseData).addOnCompleteListener(task -> {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        loadingDialog.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(EditCourse.this, "Course updated successfully!", Toast.LENGTH_SHORT).show();

                            // Send updated data back to CourseDetail
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("CourseName", courseName.getText().toString());
                            resultIntent.putExtra("CourseCode", courseCode.getText().toString());
                            resultIntent.putExtra("InstructorName", instructorName.getText().toString());
                            resultIntent.putExtra("InstructorEmail", instructorEmail.getText().toString());
                            resultIntent.putExtra("Day", day.getText().toString());
                            resultIntent.putExtra("StartTime", startTime.getText().toString());
                            resultIntent.putExtra("EndTime", endTime.getText().toString());

                            // Set result with updated data
                            setResult(RESULT_OK, resultIntent);

                            // Finish the EditCourse activity and return to CourseDetail
                            finish();
                        } else {
                            Toast.makeText(EditCourse.this, "Course update failed.", Toast.LENGTH_SHORT).show();
                        }
                    }, 1000); // Delay of 1 second for smooth transition
                });
            }
        });

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void showData(){
        Intent intent = getIntent();
        courseNameString = intent.getStringExtra("CourseName");
        courseCodeString = intent.getStringExtra("Course");
        instructorNameString = intent.getStringExtra("InstructorName");
        instructorEmailString = intent.getStringExtra("InstructorEmail");
        dayString = intent.getStringExtra("Day");
        startTimeString = intent.getStringExtra("StartTime");
        endTimeString = intent.getStringExtra("EndTime");

        courseName.setText(courseNameString);
        courseCode.setText(courseCodeString);
        instructorName.setText(instructorNameString);
        instructorEmail.setText(instructorEmailString);
        day.setText(dayString);
        startTime.setText(startTimeString);
        endTime.setText(endTimeString);
    }

    private boolean validateInputs() {
        boolean isValid = true;

        if (courseName.getText().toString().trim().isEmpty()) {
            courseName.setError("Course name is required");
            courseName.requestFocus();
            isValid = false;
        }

        if (courseCode.getText().toString().trim().isEmpty()) {
            courseCode.setError("Course code is required");
            courseCode.requestFocus();
            isValid = false;
        }

        if (instructorName.getText().toString().trim().isEmpty()) {
            instructorName.setError("Instructor name is required");
            instructorName.requestFocus();
            isValid = false;
        }

        String email = instructorEmail.getText().toString().trim();
        if (email.isEmpty()) {
            instructorEmail.setError("Instructor email is required");
            instructorEmail.requestFocus();
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            instructorEmail.setError("Enter a valid email address");
            instructorEmail.requestFocus();
            isValid = false;
        }

        if (day.getText().toString().trim().isEmpty()) {
            day.setError("Day is required");
            day.requestFocus();
            isValid = false;
        }

        if (startTime.getText().toString().trim().isEmpty()) {
            startTime.setError("Start time is required");
            startTime.requestFocus();
            isValid = false;
        }

        if (endTime.getText().toString().trim().isEmpty()) {
            endTime.setError("End time is required");
            endTime.requestFocus();
            isValid = false;
        }

        return isValid;
    }

    private boolean isDataUnchanged() {
        return courseName.getText().toString().equals(courseNameString) &&
                courseCode.getText().toString().equals(courseCodeString) &&
                instructorName.getText().toString().equals(instructorNameString) &&
                instructorEmail.getText().toString().equals(instructorEmailString) &&
                day.getText().toString().equals(dayString) &&
                startTime.getText().toString().equals(startTimeString) &&
                endTime.getText().toString().equals(endTimeString);
    }

    private AlertDialog showLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.updating, null);
        builder.setView(dialogView);
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }
}