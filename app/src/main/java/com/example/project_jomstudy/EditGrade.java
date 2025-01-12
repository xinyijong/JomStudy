package com.example.project_jomstudy;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class EditGrade extends AppCompatActivity {

    private Spinner courseSpinner, assignmentSpinner;
    private EditText weightEditText, gradeEditText;
    private Button saveGradeButton;
    private ImageButton backButton;

    private FirebaseDatabase database;
    private DatabaseReference coursesRef, assignmentsRef;

    private List<String> courseList = new ArrayList<>();
    private List<String> assignmentList = new ArrayList<>();
    private List<String> assignmentKeys = new ArrayList<>();
    private List<String> courseCodeList = new ArrayList<>();

    private ArrayAdapter<String> courseAdapter, assignmentAdapter;

    private String assignmentId; // Store the assignmentId passed from the adapter
    private String courseName, assignmentTitle; // Store the course name passed from the adapter
    private double weight; // Store the weight passed from the adapter
    private double grade; // Store the grade passed from the adapter
    private String userId; // Store the userId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_grade);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        courseSpinner = findViewById(R.id.courseSpinner);
        assignmentSpinner = findViewById(R.id.assignmentSpinner);
        weightEditText = findViewById(R.id.weight);
        gradeEditText = findViewById(R.id.grade);
        saveGradeButton = findViewById(R.id.editGradeButton);
        backButton = findViewById(R.id.backButton);

        // Get the current user's ID
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Receive the passed data from the adapter
        Intent intent = getIntent();
        assignmentId = intent.getStringExtra("assignmentId");
        courseName = intent.getStringExtra("courseName");
        assignmentTitle = intent.getStringExtra("assignmentTitle");
        weight = intent.getDoubleExtra("weight", 0.0);
        grade = intent.getDoubleExtra("grade", 0.0);

        // Set the received data to the EditTexts
        weightEditText.setText(String.format("%.2f", weight));
        gradeEditText.setText(String.format("%.2f", grade));

        // Initialize Firebase references
        database = FirebaseDatabase.getInstance();
        coursesRef = database.getReference("users").child(userId).child("Courses");
        assignmentsRef = database.getReference("users").child(userId).child("assignments");

        // Set up spinners and buttons
        setupCourseSpinner();
        setupAssignmentSpinner();

        saveGradeButton.setOnClickListener(v -> saveGrade());
        backButton.setOnClickListener(v -> finish());

        Button deleteGradeButton = findViewById(R.id.deleteGradeButton);
        deleteGradeButton.setOnClickListener(v -> deleteGrade());
    }

    private void setupCourseSpinner() {
        courseList.add("Select course");

        courseAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, courseList) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;  // Disable the "Select course" option
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

        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseSpinner.setAdapter(courseAdapter);
        courseSpinner.setEnabled(false);  // Make the spinner non-editable

        // Populate the course spinner
        coursesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                courseList.clear();
                courseCodeList.clear();
                courseList.add("Select course");
                for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                    String courseName = courseSnapshot.child("courseName").getValue(String.class);
                    String courseCode = courseSnapshot.getKey();
                    if (courseName != null && courseCode != null) {
                        courseList.add(courseName);
                        courseCodeList.add(courseCode);  // Store the course code here
                    }
                }
                courseAdapter.notifyDataSetChanged();

                // Set the selected course based on passed data
                int selectedCoursePosition = courseList.indexOf(courseName);
                if (selectedCoursePosition != -1) {
                    courseSpinner.setSelection(selectedCoursePosition);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(EditGrade.this, "Failed to load courses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupAssignmentSpinner() {
        assignmentList.add("Select completed assignment");

        assignmentAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, assignmentList) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;  // Disable the "Select completed assignment" option
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

        assignmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        assignmentSpinner.setAdapter(assignmentAdapter);

        // Disable the assignment spinner after populating it
        assignmentSpinner.setEnabled(false);  // Disable spinner interaction

        // Prevent dropdown opening by intercepting touch events
        assignmentSpinner.setClickable(false);  // Make the spinner unclickable
        assignmentSpinner.setOnTouchListener((v, event) -> true); // Block any touch interaction

        // Populate the assignment spinner based on selected course
        courseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) { // Ensure "Select course" is not selected
                    String selectedCourse = courseList.get(position);
                    Log.d("Course Selection", "Selected course: " + selectedCourse);
                    loadAssignmentsForCourse(selectedCourse);
                } else {
                    assignmentList.clear();
                    assignmentList.add("Select completed assignment");
                    assignmentAdapter.notifyDataSetChanged();
                    assignmentSpinner.setEnabled(false);  // Disable spinner when no course is selected
                    assignmentSpinner.setClickable(false);  // Ensure it remains non-clickable
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No action needed
            }
        });
    }

    private void loadAssignmentsForCourse(String selectedCourseName) {
        // Get the selected course index from the spinner (taking into account the first "Select course" item)
        int courseIndex = courseSpinner.getSelectedItemPosition() - 1; // Index adjusted by -1 to match courseCodeList

        if (courseIndex >= 0 && courseIndex < courseCodeList.size()) {
            // Get the selected course code
            String selectedCourseCode = courseCodeList.get(courseIndex);

            // Now use the selected course code to load assignments
            assignmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    assignmentList.clear();
                    assignmentKeys.clear();
                    assignmentList.add("Select completed assignment");

                    // Flag to check if the passed assignmentTitle is available
                    boolean isAssignmentFound = false;

                    for (DataSnapshot assignmentSnapshot : snapshot.getChildren()) {
                        String assignmentTitle = assignmentSnapshot.child("assignmentTitle").getValue(String.class);
                        String status = assignmentSnapshot.child("status").getValue(String.class);
                        String courseCode = assignmentSnapshot.child("courseCode").getValue(String.class); // Get the course_code

                        // Check if status is "Done" and the course code matches
                        if (assignmentTitle != null && status != null && "Done".equalsIgnoreCase(status.trim()) && courseCode != null && courseCode.equals(selectedCourseCode)) {
                            assignmentList.add(assignmentTitle);
                            assignmentKeys.add(assignmentSnapshot.getKey());
                        }

                        // Check if this is the assignment passed from the Intent
                        if (assignmentTitle != null && assignmentTitle.equalsIgnoreCase(EditGrade.this.assignmentTitle)) {
                            isAssignmentFound = true;
                        }
                    }

                    assignmentAdapter.notifyDataSetChanged();
                    assignmentSpinner.setEnabled(true);  // Enable the spinner once assignments are loaded

                    // If the passed assignmentTitle was found, set it in the spinner
                    if (isAssignmentFound) {
                        int selectedAssignmentPosition = assignmentList.indexOf(EditGrade.this.assignmentTitle);
                        if (selectedAssignmentPosition != -1) {
                            assignmentSpinner.setSelection(selectedAssignmentPosition);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(EditGrade.this, "Failed to load assignments", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    private void saveGrade() {
        int coursePosition = courseSpinner.getSelectedItemPosition();
        int assignmentPosition = assignmentSpinner.getSelectedItemPosition();
        String weightStr = weightEditText.getText().toString().trim();
        String gradeStr = gradeEditText.getText().toString().trim();

        // Check if any field is empty or not selected
        if (coursePosition <= 0 || assignmentPosition <= 0 || weightStr.isEmpty() || gradeStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Ensure assignmentPosition is within bounds
        if (assignmentPosition - 1 >= assignmentKeys.size()) {
            Toast.makeText(this, "Invalid assignment selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the course code and assignment key
        String selectedCourseCode = courseCodeList.get(coursePosition - 1);
        String selectedAssignmentKey = assignmentKeys.get(assignmentPosition - 1);

        // Parse weight and grade
        double weight;
        double grade;
        try {
            weight = Double.parseDouble(weightStr);
            grade = Double.parseDouble(gradeStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid weight or grade format", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading dialog
        AlertDialog loadingDialog = showLoadingDialog();

        // Reference to the "assignments" node for the specific course in Firebase
        DatabaseReference assignmentsRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId).child("assignments");

        // Fetch all assignments for the selected course
        assignmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double totalWeight = 0.0;

                // Calculate the total weight for assignments that belong to the selected course
                for (DataSnapshot assignmentSnapshot : snapshot.getChildren()) {
                    String courseCode = assignmentSnapshot.child("courseCode").getValue(String.class);
                    if (selectedCourseCode.equals(courseCode)) {
                        String assignmentKey = assignmentSnapshot.getKey();

                        // Exclude the assignment you're currently editing from the total weight calculation
                        if (!selectedAssignmentKey.equals(assignmentKey)) {
                            Double existingWeight = assignmentSnapshot.child("weight").getValue(Double.class);
                            if (existingWeight != null) {
                                totalWeight += existingWeight;  // Add the weight to totalWeight
                            }
                        }
                    }
                }

                // Check if the total weight exceeds 100 after adding the new weight
                if (totalWeight + weight > 100) {
                    Toast.makeText(EditGrade.this, "Total weight cannot exceed 100 for this course", Toast.LENGTH_SHORT).show();
                    loadingDialog.dismiss(); // Dismiss loading dialog before returning
                    return; // Prevent further actions
                }

                // Reference to the specific assignment for the user
                DatabaseReference userAssignmentsRef = FirebaseDatabase.getInstance().getReference("users")
                        .child(userId).child("assignments").child(selectedAssignmentKey);

                // Update the grade and weight in Firebase for the specific user
                userAssignmentsRef.child("weight").setValue(weight);
                userAssignmentsRef.child("grade").setValue(grade);

                // Add a delay before dismissing the loading dialog and showing a success message
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    loadingDialog.dismiss();
                    Toast.makeText(EditGrade.this, "Grade and weight updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                }, 1000);  // 1-second delay before dismissing the loading dialog and showing success

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditGrade.this, "Failed to fetch assignments", Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss(); // Dismiss the loading dialog in case of error
            }
        });
    }

    private void deleteGrade() {
        int coursePosition = courseSpinner.getSelectedItemPosition();
        int assignmentPosition = assignmentSpinner.getSelectedItemPosition();

        // Check if the assignment and course are selected
        if (coursePosition <= 0 || assignmentPosition <= 0) {
            Toast.makeText(this, "Please select a course and an assignment", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get the course code and assignment key
        String selectedCourseCode = courseCodeList.get(coursePosition - 1);
        String selectedAssignmentKey = assignmentKeys.get(assignmentPosition - 1);

        // Reset grade and weight for the specific user
        DatabaseReference userAssignmentsRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userId).child("assignments").child(selectedAssignmentKey);

        userAssignmentsRef.child("grade").setValue(0.00);  // Set grade to 0.00
        userAssignmentsRef.child("weight").setValue(0.00);  // Optionally set weight to 0.00

        // Show a confirmation message
        Toast.makeText(this, "Grade and weight deleted", Toast.LENGTH_SHORT).show();

        // Optionally reset input fields
        weightEditText.setText("0.00");
        gradeEditText.setText("0.00");
    }
    private AlertDialog showLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.updating, null);  // Use the same "saving" layout
        builder.setView(dialogView);
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.show();
        return dialog;
    }

}