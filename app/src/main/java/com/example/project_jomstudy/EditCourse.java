package com.example.project_jomstudy;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class EditCourse extends AppCompatActivity {

    EditText courseName, courseCode, instructorName, instructorEmail, startTime, endTime;
    Spinner day;
    Button editCourseButton;
    ImageView backButton;

    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_course);

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

        // Back button action
        backButton.setOnClickListener(v -> finish());

        // Handle editing course
        editCourseButton.setOnClickListener(v -> editCourseData(courseNameString, courseCodeString, instructorNameString, instructorEmailString, dayString, startTimeString, endTimeString));
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

        // Pass the data back to the previous activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("courseName", courseNameStringUpdated);
        resultIntent.putExtra("courseCode", courseCodeStringUpdated);
        resultIntent.putExtra("instructorName", instructorNameStringUpdated);
        resultIntent.putExtra("instructorEmail", instructorEmailStringUpdated);
        resultIntent.putExtra("day", dayStringUpdated);
        resultIntent.putExtra("startTime", startTimeStringUpdated);
        resultIntent.putExtra("endTime", endTimeStringUpdated);
        setResult(RESULT_OK, resultIntent);

        Toast.makeText(EditCourse.this, "Course updated successfully", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void showTimePickerDialog(EditText timeEditText) {
        // Get the current time or default to 12:00
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // Create a TimePickerDialog
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                EditCourse.this,
                (view, selectedHour, selectedMinute) -> {
                    // Format the time to HH:mm (24-hour format)
                    String time = String.format("%02d:%02d", selectedHour, selectedMinute);
                    // Set the time in the EditText
                    timeEditText.setText(time);
                },
                hour, minute, true); // true for 24-hour format

        timePickerDialog.show();
    }
}
