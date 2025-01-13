package com.example.project_jomstudy;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class EditCourse extends AppCompatActivity {

    EditText courseName, courseCode, instructorName, instructorEmail, startTime, endTime;
    Spinner day;
    Button editCourseButton;
    ImageView backButton;

    DatabaseReference reference;
    private AlertDialog savingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_course);

        // Initialize views
        courseName = findViewById(R.id.courseName);
        courseCode = findViewById(R.id.courseCode);
        instructorName = findViewById(R.id.instructorName);
        instructorEmail = findViewById(R.id.instructorEmail);
        startTime = findViewById(R.id.startTime);
        endTime = findViewById(R.id.endTime);
        day = findViewById(R.id.day);
        editCourseButton = findViewById(R.id.edit_button);
        backButton = findViewById(R.id.backButton);

        // Set up the spinner with available days
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.days_of_week, android.R.layout.simple_spinner_dropdown_item);
        day.setAdapter(adapter);

        // Get the course data from the previous activity (Intent)
        Intent intent = getIntent();
        String courseNameString = intent.getStringExtra("CourseName");
        String courseCodeString = intent.getStringExtra("Course");
        String instructorNameString = intent.getStringExtra("InstructorName");
        String instructorEmailString = intent.getStringExtra("InstructorEmail");
        String dayString = intent.getStringExtra("Day");
        String startTimeString = intent.getStringExtra("StartTime");
        String endTimeString = intent.getStringExtra("EndTime");

        // Autofill the fields with the values passed in the Intent
        courseName.setText(courseNameString);
        courseCode.setText(courseCodeString);
        instructorName.setText(instructorNameString);
        instructorEmail.setText(instructorEmailString);
        startTime.setText(startTimeString);
        endTime.setText(endTimeString);

        courseCode.setEnabled(false);
        courseCode.setTextColor(Color.GRAY);

        // Set the selected day in the spinner
        if (dayString != null) {
            int spinnerPosition = ((ArrayAdapter) day.getAdapter()).getPosition(dayString);
            day.setSelection(spinnerPosition);
        }

        startTime.setOnClickListener(v -> showTimePickerDialog(startTime));
        endTime.setOnClickListener(v -> showTimePickerDialog(endTime));


        backButton.setOnClickListener(v -> finish());


        editCourseButton.setOnClickListener(v -> {
            // Validate mandatory fields
            if (courseName.getText().toString().trim().isEmpty()) {
                courseName.setError("Course name cannot be empty");
                courseName.requestFocus();
                return;
            }

            if (instructorName.getText().toString().trim().isEmpty()) {
                instructorName.setError("Instructor name cannot be empty");
                instructorName.requestFocus();
                return;
            }

            if (instructorEmail.getText().toString().trim().isEmpty()) {
                instructorEmail.setError("Instructor email cannot be empty");
                instructorEmail.requestFocus();
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(instructorEmail.getText().toString()).matches()) {
                instructorEmail.setError("Enter a valid email address");
                instructorEmail.requestFocus();
                return;
            }

            // Show saving dialog
            showSavingDialog(courseNameString, courseCodeString, instructorNameString, instructorEmailString, dayString, startTimeString, endTimeString);

            // After saving, send the result back
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                // After saving is complete, send the updated data back
                Intent resultIntent = new Intent();
                resultIntent.putExtra("CourseName", courseName.getText().toString());
                resultIntent.putExtra("Course", courseCode.getText().toString());
                resultIntent.putExtra("InstructorName", instructorName.getText().toString());
                resultIntent.putExtra("InstructorEmail", instructorEmail.getText().toString());
                resultIntent.putExtra("Day", day.getSelectedItem().toString());
                resultIntent.putExtra("StartTime", startTime.getText().toString());
                resultIntent.putExtra("EndTime", endTime.getText().toString());

                setResult(RESULT_OK, resultIntent);  // Send updated data back to CourseDetail
                finish();
            }, 1000);
        });
    }

    private void showSavingDialog(String courseNameString, String courseCodeString, String instructorNameString, String instructorEmailString, String dayString, String startTimeString, String endTimeString) {
        // Create the saving dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.updating, null);
        builder.setView(dialogView);
        builder.setCancelable(false);
        savingDialog = builder.create();
        savingDialog.show();

        // Start the data saving process after the dialog is displayed
        new Thread(() -> {
            editCourseData(courseNameString, courseCodeString, instructorNameString, instructorEmailString, dayString, startTimeString, endTimeString);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                // Check if the activity is still running
                if (!isFinishing() && savingDialog.isShowing()) {
                    savingDialog.dismiss();
                }

                // Proceed with finishing the activity only after the data has been updated
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    finish();
                }, 500);
            }, 1000);
        }).start();
    }

    private void editCourseData(String courseNameString, String courseCodeString, String instructorNameString, String instructorEmailString, String dayString, String startTimeString, String endTimeString) {
        // Collect the edited course data
        String courseNameStringUpdated = courseName.getText().toString();
        String courseCodeStringUpdated = courseCode.getText().toString();
        String instructorNameStringUpdated = instructorName.getText().toString();
        String instructorEmailStringUpdated = instructorEmail.getText().toString();
        String dayStringUpdated = day.getSelectedItem().toString();
        String startTimeStringUpdated = startTime.getText().toString();
        String endTimeStringUpdated = endTime.getText().toString();

        // Ensure the user is authenticated
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        reference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("Courses");

        DatabaseReference courseRef = reference.child(courseCodeString);

        // Update the course data in Firebase
        courseRef.child("courseName").setValue(courseNameStringUpdated);
        courseRef.child("courseCode").setValue(courseCodeStringUpdated);
        courseRef.child("instructorName").setValue(instructorNameStringUpdated);
        courseRef.child("instructorEmail").setValue(instructorEmailStringUpdated);
        courseRef.child("day").setValue(dayStringUpdated);
        courseRef.child("startTime").setValue(startTimeStringUpdated);
        courseRef.child("endTime").setValue(endTimeStringUpdated);

        updateAssignments(courseCodeStringUpdated, courseNameStringUpdated);



        new Handler(Looper.getMainLooper()).post(() -> {
            Toast.makeText(EditCourse.this, "Course updated successfully", Toast.LENGTH_SHORT).show();
        });
    }

    private void showTimePickerDialog(EditText timeEditText) {

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);


        TimePickerDialog timePickerDialog = new TimePickerDialog(
                EditCourse.this,
                (view, selectedHour, selectedMinute) -> {

                    String time = String.format("%02d:%02d", selectedHour, selectedMinute);

                    timeEditText.setText(time);
                },
                hour, minute, true); // true for 24-hour format

        timePickerDialog.show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Dismiss the dialog if it's showing
        if (savingDialog != null && savingDialog.isShowing()) {
            savingDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Dismiss the dialog if it's showing
        if (savingDialog != null && savingDialog.isShowing()) {
            savingDialog.dismiss();
        }
    }

    private void updateAssignments(String courseId, String newCourseName) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();

        DatabaseReference assignmentsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("assignments");


        assignmentsRef.keepSynced(true);

        // Query assignments by courseCode
        assignmentsRef.orderByChild("courseCode").equalTo(courseId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            // For each assignment related to this course
                            for (DataSnapshot assignmentSnapshot : snapshot.getChildren()) {
                                String assignmentId = assignmentSnapshot.getKey();

                                // Update the course name in the assignment
                                assignmentSnapshot.getRef().child("courseName").setValue(newCourseName)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("AssignmentUpdate", "Course name updated in assignment: " + assignmentId);
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e("AssignmentUpdate", "Failed to update course name in assignment", e);
                                        });
                            }
                        } else {
                            Log.d("AssignmentUpdate", "No assignments found with courseCode: " + courseId);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("AssignmentUpdate", "Error querying assignments: " + error.getMessage());
                    }
                });
    }
}