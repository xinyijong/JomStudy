package com.example.project_jomstudy;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddAssignment extends AppCompatActivity {
    EditText assignmentTitle;
    EditText dueDate;
    EditText courseName;
    EditText description;
    Button addAssignmentButton;
    ImageView backButton;
    Spinner courseCodeSpinner;

    FirebaseDatabase rootNode;
    DatabaseReference courseRef;
    DatabaseReference assignmentRef;

    private List<String> courseCodeList = new ArrayList<>();
    private ArrayAdapter<String> courseCodeAdapter;

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

        // Initialize views
        assignmentTitle = findViewById(R.id.assignmentTitle);
        courseName = findViewById(R.id.courseName);
        description = findViewById(R.id.description);
        dueDate = findViewById(R.id.duedate);
        addAssignmentButton = findViewById(R.id.addAssignmentButton);
        backButton = findViewById(R.id.backButton);
        courseCodeSpinner = findViewById(R.id.courseCode);

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

        // Set up course code spinner
        setupCourseCodeSpinner();
        courseName.setFocusable(false);
        courseName.setClickable(false);
        courseName.setTextColor(Color.GRAY);

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
                                    dueDate.setText(dayOfMonth1 + "/" + (month1 + 1) + "/" + year1 + " " +
                                            String.format("%02d:%02d", hourOfDay, minuteOfHour));
                                },
                                hour, minute, true);
                        timePickerDialog.show();
                    },
                    year, month, dayOfMonth);

            datePickerDialog.show();
        });

        addAssignmentButton.setOnClickListener(v -> {
            if (validateInputs()) {
                // Show loading dialog
                AlertDialog loadingDialog = showLoadingDialog();

                assignmentRef = rootNode.getReference("users").child(currentUser.getUid()).child("assignments");
                String courseCodeText = courseCodeSpinner.getSelectedItem().toString().trim(); // Get selected course code
                String title = assignmentTitle.getText().toString().trim();
                String assignDescription = description.getText().toString().trim();
                String dueDateText = dueDate.getText().toString().trim();
                int initialProgress = 0;
                String status = "Not Completed"; // Set initial status
                double grade = 0.0;  // Set default grade
                double weight = 0.0;

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
                                }, 1000);
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

        backButton.setOnClickListener(v -> finish());
    }

    private void setupCourseCodeSpinner() {
        // Initialize the list for the course codes
        courseCodeList.add("Select course code");


        courseCodeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, courseCodeList) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                if (position == 0) {
                    textView.setTextColor(Color.GRAY);
                } else {
                    textView.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        // Set the adapter to the Spinner
        courseCodeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseCodeSpinner.setAdapter(courseCodeAdapter);

        // Populate the course code spinner
        courseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                courseCodeList.clear();
                courseCodeList.add("Select course code");
                for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                    String courseCode = courseSnapshot.getKey();
                    if (courseCode != null) {
                        courseCodeList.add(courseCode);
                    }
                }
                courseCodeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(AddAssignment.this, "Failed to load course codes", Toast.LENGTH_SHORT).show();
            }
        });

        // Add an item selected listener to the Spinner
        courseCodeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position != 0) {
                    String selectedCourseCode = courseCodeSpinner.getSelectedItem().toString();
                    // Fetch course name based on selected course code
                    courseRef.child(selectedCourseCode).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String courseNameText = dataSnapshot.child("courseName").getValue(String.class);
                                courseName.setText(courseNameText);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(AddAssignment.this, "Error fetching course details", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // No action required
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

        if (courseCodeSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(AddAssignment.this, "Please select a course code", Toast.LENGTH_SHORT).show();
            isValid = false;
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