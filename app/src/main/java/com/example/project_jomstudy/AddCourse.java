package com.example.project_jomstudy;

import static com.google.android.material.internal.ViewUtils.hideKeyboard;

import android.app.TimePickerDialog;
import android.content.Intent;
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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class AddCourse extends AppCompatActivity {

    EditText courseName;
    EditText courseCode;
    EditText instructorName;
    EditText instructorEmail;
    Spinner day;
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

        // Initialize views
        courseName = findViewById(R.id.courseName);
        courseCode = findViewById(R.id.courseCode);
        instructorName = findViewById(R.id.instructorName);
        instructorEmail = findViewById(R.id.instructorEmail);
        day = findViewById(R.id.day);
        startTime = findViewById(R.id.startTime);
        endTime = findViewById(R.id.endTime);
        addCourseButton = findViewById(R.id.addCourseButton);


        Spinner daySpinner = findViewById(R.id.day);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.days_of_week, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(adapter);

        // Time picker for startTime
        startTime.setOnClickListener(v -> {
            // Hide the soft keyboard when clicking the EditText
            hideKeyboard(v);

            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    AddCourse.this,
                    (view, hourOfDay, minute1) -> {
                        // Format the time as "HH:mm"
                        startTime.setText(String.format("%02d:%02d", hourOfDay, minute1));
                    },
                    hour, minute, true); // 24-hour format
            timePickerDialog.show();
        });

        // Time picker for endTime
        endTime.setOnClickListener(v -> {
            // Hide the soft keyboard when clicking the EditText
            hideKeyboard(v);

            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(
                    AddCourse.this,
                    (view, hourOfDay, minute1) -> {
                        // Format the time as "HH:mm"
                        endTime.setText(String.format("%02d:%02d", hourOfDay, minute1));
                    },
                    hour, minute, true); // 24-hour format
            timePickerDialog.show();
        });

        addCourseButton.setOnClickListener(v -> {
            if (validateInputs()) {
                // Show loading dialog
                AlertDialog loadingDialog = showLoadingDialog();

                // Get current user UID
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                Log.d("FirebaseAuth", "User ID: " + userId);

                if (userId == null || userId.isEmpty()) {
                    Toast.makeText(this, "User not authenticated. Please log in.", Toast.LENGTH_SHORT).show();
                    loadingDialog.dismiss();
                    return;
                }

                rootNode = FirebaseDatabase.getInstance();
                reference = rootNode.getReference("users").child(userId).child("Courses");

                String course = courseName.getText().toString().trim();
                String code = courseCode.getText().toString().trim();
                String instructor = instructorName.getText().toString().trim();
                String instructorE = instructorEmail.getText().toString().trim();
                String days = day.getSelectedItem().toString();
                String start = startTime.getText().toString().trim();
                String end = endTime.getText().toString().trim();

                // Check if course code already exists
                reference.child(code).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            // Course code already exists
                            loadingDialog.dismiss();
                            Toast.makeText(this, "Course code is already added.", Toast.LENGTH_SHORT).show();
                        } else {
                            // Course code is available, proceed with saving
                            CourseClass courseData = new CourseClass(course, code, instructorE, instructor, days, start, end);

                            reference.child(code).setValue(courseData).addOnCompleteListener(saveTask -> {
                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                    loadingDialog.dismiss();
                                    if (saveTask.isSuccessful()) {
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
                    } else {
                        loadingDialog.dismiss();
                        Toast.makeText(this, "Error checking course code: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
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

        if (day.getSelectedItem().toString().trim().isEmpty()) {
            Toast.makeText(this, "Day is required", Toast.LENGTH_SHORT).show();
            isValid = false;
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

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
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