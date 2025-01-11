package com.example.project_jomstudy;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class EditCourse extends AppCompatActivity {

    EditText courseName;
    EditText courseCode;
    EditText instructorName;
    EditText instructorEmail;
    Spinner day;
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

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.days_of_week, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        day.setAdapter(adapter);

        showData();

        startTime.setOnClickListener(v -> {
            hideKeyboard(v);

            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    EditCourse.this,
                    (view, hourOfDay, minute1) -> {
                        startTime.setText(String.format("%02d:%02d", hourOfDay, minute1));
                    },
                    hour, minute, true); // 24-hour format
            timePickerDialog.show();
        });

        // TimePicker for endTime
        endTime.setOnClickListener(v -> {
            hideKeyboard(v);

            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    EditCourse.this,
                    (view, hourOfDay, minute1) -> {
                        endTime.setText(String.format("%02d:%02d", hourOfDay, minute1));
                    },
                    hour, minute, true); // 24-hour format
            timePickerDialog.show();
        });

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
                        day.getSelectedItem().toString(),
                        startTime.getText().toString(),
                        endTime.getText().toString()
                );

                // Save the changes to Firebase
                reference.child(courseCodeString).setValue(updatedCourseData).addOnCompleteListener(task -> {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        loadingDialog.dismiss();
                        if (task.isSuccessful()) {
                            updateCourseNameInAssignments(courseCodeString, courseName.getText().toString());

                            Toast.makeText(EditCourse.this, "Course updated successfully!", Toast.LENGTH_SHORT).show();

                            // Send updated data back to CourseDetail
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("CourseName", courseName.getText().toString());
                            resultIntent.putExtra("CourseCode", courseCode.getText().toString());
                            resultIntent.putExtra("InstructorName", instructorName.getText().toString());
                            resultIntent.putExtra("InstructorEmail", instructorEmail.getText().toString());
                            resultIntent.putExtra("Day", day.getSelectedItem().toString());
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
        // Get data from Intent and set it to views
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

        // Set selected day in the Spinner
        ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) day.getAdapter();
        int position = adapter.getPosition(dayString); // Find the position of the day in the array
        day.setSelection(position); // Set the selected item in the Spinner

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

        if (day.getSelectedItem().toString().trim().isEmpty()) {
            Toast.makeText(this, "Day is required", Toast.LENGTH_SHORT).show();
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
                day.getSelectedItem().toString().equals(dayString) &&
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

    private void updateCourseNameInAssignments(String courseCode, String newCourseName) {
        // Firebase logic to update course name in related assignments
        DatabaseReference assignmentsRef = FirebaseDatabase.getInstance().getReference("Assignments").child(courseCode);

        // Now, fetch all assignments under the specific course code
        assignmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot assignmentSnapshot : snapshot.getChildren()) {
                        // Update the course name for the assignment
                        String assignmentKey = assignmentSnapshot.getKey();
                        assignmentsRef.child(assignmentKey).child("courseName").setValue(newCourseName)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Log.d("EditCourse", "Assignment updated with new course name.");
                                    } else {
                                        Log.d("EditCourse", "Failed to update assignment: " + task.getException());
                                    }
                                });
                    }
                } else {
                    Log.d("EditCourse", "No assignments found for courseCode: " + courseCode);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("EditCourse", "Failed to read assignments: " + error.getMessage());
                Toast.makeText(EditCourse.this, "Failed to update assignments", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }



}