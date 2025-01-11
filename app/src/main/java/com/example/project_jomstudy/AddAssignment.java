package com.example.project_jomstudy;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class AddAssignment extends AppCompatActivity {

    EditText assignmentTitle;
    EditText dueDate;
    EditText courseCode;
    EditText courseName;
    EditText description;
    Button addAssignmentButton;
    ImageView backButton;

    FirebaseDatabase rootNode;
    DatabaseReference courseRef;
    DatabaseReference assignmentRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_assignment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        assignmentTitle = findViewById(R.id.assignmentTitle);
        courseCode = findViewById(R.id.courseCode);
        courseName = findViewById(R.id.courseName);
        description = findViewById(R.id.description);
        dueDate = findViewById(R.id.duedate);  // The EditText for due date
        addAssignmentButton = findViewById(R.id.addAssignmentButton);

        // Initialize Firebase references
        rootNode = FirebaseDatabase.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser(); // Get the current user
        courseRef = rootNode.getReference("users").child(currentUser.getUid()).child("Courses");

        // Check if user is authenticated
        if (currentUser == null) {
            Toast.makeText(this, "You need to log in first.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Set up the DatePickerDialog when the EditText field is clicked
        dueDate.setOnClickListener(v -> {
            // Get current date and time
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            // DatePickerDialog
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    AddAssignment.this,
                    (view, year1, month1, dayOfMonth1) -> {
                        // TimePickerDialog
                        TimePickerDialog timePickerDialog = new TimePickerDialog(
                                AddAssignment.this,
                                (timeView, hourOfDay, minuteOfHour) -> {
                                    // Format the date and time as "dd/mm/yyyy hh:mm"
                                    dueDate.setText(dayOfMonth1 + "/" + (month1 + 1) + "/" + year1 + " " +
                                            String.format("%02d:%02d", hourOfDay, minuteOfHour));
                                },
                                hour, minute, true);
                        timePickerDialog.show();
                    },
                    year, month, dayOfMonth);

            datePickerDialog.show();
        });

        courseCode.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int before, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int after) {
                String courseCodeText = courseCode.getText().toString().trim();
                if (!courseCodeText.isEmpty()) {
                    fetchCourseName(courseCodeText);
                }
            }

            @Override
            public void afterTextChanged(android.text.Editable editable) {
            }
        });

        addAssignmentButton.setOnClickListener(v -> {
            if (validateInputs()) {
                // Show loading dialog
                AlertDialog loadingDialog = showLoadingDialog();

                assignmentRef = rootNode.getReference("users").child(currentUser.getUid()).child("assignments");
                String courseCodeText = courseCode.getText().toString().trim();
                String title = assignmentTitle.getText().toString().trim();
                String assignDescription = description.getText().toString().trim();
                String dueDateText = dueDate.getText().toString().trim();
                int initialProgress = 0;
                String status = "Not Completed"; // Set initial status (or leave as default, change later if needed)
                double grade = 0.0;  // Set default grade (or allow dynamic input later)
                double weight = 100.0;

                // Get the course name using the entered course code
                courseRef.child(courseCodeText).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String courseName = dataSnapshot.child("courseName").getValue(String.class);

                            // Create the Assignment object
                            AssignmentClass assignmentData = new AssignmentClass(title, dueDateText, courseCodeText, courseName, assignDescription, initialProgress, status, grade, weight);

                            // Save the assignment under the current user
                            assignmentRef.push().setValue(assignmentData).addOnCompleteListener(task -> {
                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                    loadingDialog.dismiss();
                                    if (task.isSuccessful()) {
                                        Toast.makeText(AddAssignment.this, "Assignment saved successfully!", Toast.LENGTH_SHORT).show();

                                        Intent intent = new Intent(AddAssignment.this, Home.class);
                                        intent.putExtra("navigateToFragment", "AssignmentFragment"); // Specify the fragment to show
                                        intent.putExtra("selectedTab", R.id.assignment); // Specify the tab to select
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(AddAssignment.this, "Assignment saving failed.", Toast.LENGTH_SHORT).show();
                                    }
                                }, 1000); // Delay of 1 second
                            });
                        } else {
                            loadingDialog.dismiss();
                            Toast.makeText(AddAssignment.this, "Course code not found.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        loadingDialog.dismiss();
                        Toast.makeText(AddAssignment.this, "Error retrieving course details.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
    }

    private void fetchCourseName(String courseCode) {
        // Check if courseCode is empty
        if (courseCode.isEmpty()) {
            Toast.makeText(AddAssignment.this, "Course code cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Retrieve course data from the Firebase database
        courseRef.child(courseCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Check if the course data exists in the database
                if (dataSnapshot.exists()) {
                    String courseNameText = dataSnapshot.child("courseName").getValue(String.class);

                    // If courseName exists, set it to the EditText
                    if (courseNameText != null && !courseNameText.isEmpty()) {
                        courseName.setText(courseNameText);  // Set the course name automatically
                    } else {
                        // Handle the case where the courseName does not exist
                        courseName.setText("");  // Clear the course name if it does not exist
                        Toast.makeText(AddAssignment.this, "Course name not found for this course code.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // If course code doesn't exist
                    courseName.setText("");  // Clear the course name
                    Toast.makeText(AddAssignment.this, "Course code not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error retrieving data
                Toast.makeText(AddAssignment.this, "Error retrieving course details: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        if (assignmentTitle.getText().toString().trim().isEmpty()) {
            assignmentTitle.setError("Assignment title is required");
            isValid = false;
            assignmentTitle.requestFocus();
        }

        if (courseCode.getText().toString().trim().isEmpty()) {
            courseCode.setError("Course code is required");
            isValid = false;
            courseCode.requestFocus();
        }

        if (description.getText().toString().trim().isEmpty()) {
            description.setError("Description is required");
            isValid = false;
            description.requestFocus();
        }

        if (dueDate.getText().toString().trim().isEmpty()) {
            dueDate.setError("Due date is required");
            isValid = false;
            dueDate.requestFocus();
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