package com.example.project_jomstudy;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

public class EditAssignment extends AppCompatActivity {

    private EditText assignmentTitle, dueDate, courseCode, courseName, description, progress;
    private Button editAssignmentButton;
    private ImageButton backButton;

    private DatabaseReference reference;
    private AlertDialog savingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_assignment);

        // Initialize UI components
        assignmentTitle = findViewById(R.id.assignmentTitle);
        dueDate = findViewById(R.id.duedate);
        courseCode = findViewById(R.id.courseCode);
        courseName = findViewById(R.id.courseName);
        description = findViewById(R.id.description);
        progress = findViewById(R.id.progress);
        editAssignmentButton = findViewById(R.id.editAssignmentButton);
        backButton = findViewById(R.id.backButton);

        // Get the assignment data from the previous activity (Intent)
        Intent intent = getIntent();
        String assignmentId = intent.getStringExtra("assignmentId"); // Get the unique ID
        String assignmentTitleString = intent.getStringExtra("assignmentTitle");
        String dueDateString = intent.getStringExtra("dueDate");
        String courseCodeString = intent.getStringExtra("courseCode");
        String courseNameString = intent.getStringExtra("courseName");
        String descriptionString = intent.getStringExtra("description");
        int progressValue = intent.getIntExtra("progress", 0);


        Log.d("EditAssignment", "Progress value passed: " + progressValue);

        // Autofill the fields with the values passed in the Intent
        assignmentTitle.setText(assignmentTitleString);
        dueDate.setText(dueDateString);
        courseCode.setText(courseCodeString);
        courseName.setText(courseNameString);
        description.setText(descriptionString);


        progress.setText(String.valueOf(progressValue)); // Convert progress integer to string


        courseCode.setEnabled(false);
        courseCode.setTextColor(Color.GRAY);
        courseName.setEnabled(false);
        courseName.setTextColor(Color.GRAY);


        backButton.setOnClickListener(v -> finish());

        dueDate.setOnClickListener(v -> showDateTimePicker());


        editAssignmentButton.setOnClickListener(v -> {
            showSavingDialog();
            editAssignmentData(assignmentId);
        });
    }

    private void showDateTimePicker() {
        // Get current date and time
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);


        DatePickerDialog datePickerDialog = new DatePickerDialog(
                EditAssignment.this,
                (view, year1, month1, dayOfMonth1) -> {

                    TimePickerDialog timePickerDialog = new TimePickerDialog(
                            EditAssignment.this,
                            (timeView, hourOfDay, minuteOfHour) -> {

                                dueDate.setText(dayOfMonth1 + "/" + (month1 + 1) + "/" + year1 + " " +
                                        String.format("%02d:%02d", hourOfDay, minuteOfHour));
                            },
                            hour, minute, true);
                    timePickerDialog.show();
                },
                year, month, dayOfMonth);

        datePickerDialog.show();
    }

    private void showSavingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.updating, null);
        builder.setView(dialogView);
        builder.setCancelable(false);
        savingDialog = builder.create();
        savingDialog.show();
    }

    private void editAssignmentData(String assignmentId) {
        // Collect the edited assignment data
        String updatedTitle = assignmentTitle.getText().toString().trim();
        String updatedDueDate = dueDate.getText().toString().trim();
        String updatedCourseCode = courseCode.getText().toString().trim();
        String updatedCourseName = courseName.getText().toString().trim();
        String updatedDescription = description.getText().toString().trim();
        int updatedProgress;

        // Validate required fields
        if (updatedTitle.isEmpty()) {
            if (savingDialog != null && savingDialog.isShowing()) {
                savingDialog.dismiss();
            }
            Toast.makeText(this, "Assignment title cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (updatedDescription.isEmpty()) {
            if (savingDialog != null && savingDialog.isShowing()) {
                savingDialog.dismiss();
            }
            Toast.makeText(this, "Description cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String progressText = progress.getText().toString().replace("%", "").trim();
            updatedProgress = Integer.parseInt(progressText);

            if (updatedProgress < 0 || updatedProgress > 100) {
                if (savingDialog != null && savingDialog.isShowing()) {
                    savingDialog.dismiss();
                }
                Toast.makeText(this, "Progress must be between 0 and 100", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            if (savingDialog != null && savingDialog.isShowing()) {
                savingDialog.dismiss();
            }
            Toast.makeText(this, "Please enter a valid progress value", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get Firebase reference
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid();
        reference = FirebaseDatabase.getInstance().getReference("users").child(userId).child("assignments");
        DatabaseReference assignmentRef = reference.child(assignmentId);

        // Update Firebase database
        assignmentRef.child("assignmentTitle").setValue(updatedTitle);
        assignmentRef.child("dueDate").setValue(updatedDueDate);
        assignmentRef.child("courseCode").setValue(updatedCourseCode);
        assignmentRef.child("courseName").setValue(updatedCourseName);
        assignmentRef.child("description").setValue(updatedDescription);
        assignmentRef.child("progress").setValue(updatedProgress);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (savingDialog != null && savingDialog.isShowing()) {
                savingDialog.dismiss();
            }

            // Return updated data
            Intent resultIntent = new Intent();
            resultIntent.putExtra("assignmentTitle", updatedTitle);
            resultIntent.putExtra("dueDate", updatedDueDate);
            resultIntent.putExtra("courseCode", updatedCourseCode);
            resultIntent.putExtra("courseName", updatedCourseName);
            resultIntent.putExtra("description", updatedDescription);
            resultIntent.putExtra("progress", updatedProgress);
            setResult(RESULT_OK, resultIntent);

            Toast.makeText(EditAssignment.this, "Assignment updated successfully", Toast.LENGTH_SHORT).show();
            finish();
        }, 1000);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (savingDialog != null && savingDialog.isShowing()) {
            savingDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (savingDialog != null && savingDialog.isShowing()) {
            savingDialog.dismiss();
        }
    }
}
