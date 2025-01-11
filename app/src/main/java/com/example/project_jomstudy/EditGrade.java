package com.example.project_jomstudy;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
    private Button saveGradeButton, deleteGradeButton;
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

        courseSpinner = findViewById(R.id.courseSpinner);
        assignmentSpinner = findViewById(R.id.assignmentSpinner);
        weightEditText = findViewById(R.id.weight);
        gradeEditText = findViewById(R.id.grade);
        saveGradeButton = findViewById(R.id.editGradeButton);
        backButton = findViewById(R.id.backButton);

        // Receive the passed data
        Intent intent = getIntent();
        assignmentId = intent.getStringExtra("assignmentId");  // Get the assignmentId (Firebase key)
        courseName = intent.getStringExtra("courseName");  // Get the courseName
        assignmentTitle = intent.getStringExtra("assignmentTitle");
        weight = intent.getDoubleExtra("weight", 0.0);  // Get the weight
        grade = intent.getDoubleExtra("grade", 0.0);  // Get the grade

        // Set the received data to the EditTexts
        weightEditText.setText(String.format("%.2f", weight));
        gradeEditText.setText(String.format("%.2f", grade));

        database = FirebaseDatabase.getInstance();
        coursesRef = database.getReference("Courses");
        assignmentsRef = database.getReference("Assignments");

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
                    String courseCode = courseSnapshot.getKey(); // Use the key as the course code
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
        assignmentSpinner.setEnabled(false);

        // Populate the assignment spinner based on selected course
        courseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) { // Ensure "Select course" is not selected
                    String selectedCourse = courseList.get(position);
                    loadAssignmentsForCourse(selectedCourse);
                } else {
                    assignmentList.clear();
                    assignmentList.add("Select completed assignment"); // Reset hint when no course is selected
                    assignmentAdapter.notifyDataSetChanged();
                    assignmentSpinner.setEnabled(false);  // Disable the spinner again when no course is selected
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No action needed
            }
        });
    }

    private void loadAssignmentsForCourse(String selectedCourseName) {
        int courseIndex = courseSpinner.getSelectedItemPosition() - 1; // Get the selected course index
        if (courseIndex >= 0 && courseIndex < courseCodeList.size()) {
            String courseCode = courseCodeList.get(courseIndex);

            assignmentsRef.child(courseCode).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    assignmentList.clear();  // Clear the previous data
                    assignmentKeys.clear();  // Clear the previous keys
                    assignmentList.add("Select completed assignment");  // Add the hint

                    for (DataSnapshot assignmentSnapshot : snapshot.getChildren()) {
                        String assignmentTitle = assignmentSnapshot.child("assignmentTitle").getValue(String.class);
                        String status = assignmentSnapshot.child("status").getValue(String.class);

                        AssignmentClass assignment = assignmentSnapshot.getValue(AssignmentClass.class);
                        if (assignment != null && status != null && "Done".equalsIgnoreCase(status.trim())) {
                            assignmentList.add(assignment.getAssignmentTitle());
                            assignmentKeys.add(assignmentSnapshot.getKey()); // Save the assignment key
                        }
                    }

                    assignmentAdapter.notifyDataSetChanged();  // Notify the adapter that data has changed
                    assignmentSpinner.setEnabled(false);  // Enable the spinner once assignments are loaded

                    // Autofill the assignment spinner based on the passed assignment name
                    int selectedAssignmentPosition = assignmentList.indexOf(assignmentTitle);
                    if (selectedAssignmentPosition != -1) {
                        assignmentSpinner.setSelection(selectedAssignmentPosition);
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

        // Update the grade and weight in Firebase
        assignmentsRef.child(selectedCourseCode).child(selectedAssignmentKey)
                .child("weight").setValue(weight);
        assignmentsRef.child(selectedCourseCode).child(selectedAssignmentKey)
                .child("grade").setValue(grade);

        Toast.makeText(EditGrade.this, "Grade and weight updated successfully", Toast.LENGTH_SHORT).show();
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

        // Set grade and weight to 0.00 in Firebase
        assignmentsRef.child(selectedCourseCode).child(selectedAssignmentKey)
                .child("grade").setValue(0.00);  // Set grade to 0.00
        assignmentsRef.child(selectedCourseCode).child(selectedAssignmentKey)
                .child("weight").setValue(0.00);  // Optionally set weight to 0.00

        // Notify the adapter that the data has changed
        assignmentAdapter.notifyDataSetChanged();

        // Reset the input fields to 0.00
        weightEditText.setText("0.00");
        gradeEditText.setText("0.00");

        // Show a confirmation message
        Toast.makeText(this, "Grade and weight deleted", Toast.LENGTH_SHORT).show();
    }
}