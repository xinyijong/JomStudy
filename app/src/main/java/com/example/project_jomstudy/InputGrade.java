package com.example.project_jomstudy;

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
import androidx.annotation.NonNull;
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

public class InputGrade extends AppCompatActivity {

    private Spinner courseSpinner, assignmentSpinner;
    private EditText weightEditText, gradeEditText;
    private Button inputGradeButton;
    private ImageButton backButton;

    private FirebaseDatabase database;
    private DatabaseReference coursesRef, assignmentsRef;

    private List<String> courseList = new ArrayList<>();
    private List<String> assignmentList = new ArrayList<>();
    private List<String> assignmentKeys = new ArrayList<>(); // Store keys for selected assignments
    private List<String> courseCodeList = new ArrayList<>();
    private ArrayAdapter<String> courseAdapter, assignmentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_input_grade);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        courseSpinner = findViewById(R.id.courseSpinner);
        assignmentSpinner = findViewById(R.id.assignmentSpinner);
        weightEditText = findViewById(R.id.weight);
        gradeEditText = findViewById(R.id.grade);
        inputGradeButton = findViewById(R.id.addCourseButton);
        backButton = findViewById(R.id.backButton);

        database = FirebaseDatabase.getInstance();
        coursesRef = database.getReference("Courses");
        assignmentsRef = database.getReference("Assignments");

        setupCourseSpinner();
        setupAssignmentSpinner();

        inputGradeButton.setOnClickListener(v -> inputGrade());
        backButton.setOnClickListener(v -> finish());
    }

    private void setupCourseSpinner() {
        courseList.add("Select course");

        courseAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, courseList) {
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

        courseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        courseSpinner.setAdapter(courseAdapter);

        coursesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(InputGrade.this, "Failed to load courses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupAssignmentSpinner() {
        // Adding the hint as the first item
        assignmentList.add("Select completed assignment");

        assignmentAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, assignmentList) {
            @Override
            public boolean isEnabled(int position) {
                // Disable the hint item
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                if (position == 0) {
                    // Set the hint item text color to gray
                    textView.setTextColor(Color.GRAY);
                } else {
                    textView.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        assignmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        assignmentSpinner.setAdapter(assignmentAdapter);

        courseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) { // Ensure hint is not selected
                    String selectedCourse = courseList.get(position);
                    Log.d("Selected Course", selectedCourse); // Check if course is selected
                    loadAssignmentsForCourse(selectedCourse);
                } else {
                    assignmentList.clear();
                    assignmentList.add("Select completed assignment"); // Reset hint when no course selected
                    assignmentAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No action needed
            }
        });
    }

    private void loadAssignmentsForCourse(String selectedCourseName) {
        // Find the index of the selected course
        int courseIndex = courseSpinner.getSelectedItemPosition() - 1; // Subtract 1 to exclude the hint
        if (courseIndex >= 0 && courseIndex < courseCodeList.size()) {
            String courseCode = courseCodeList.get(courseIndex);  // Get the correct course code
            Log.d("Selected Course Code", courseCode);  // Log the course code

            assignmentsRef.child(courseCode).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    assignmentList.clear();
                    assignmentKeys.clear();
                    Log.d("Assignments", "Data fetched for course: " + courseCode);
                    for (DataSnapshot assignmentSnapshot : snapshot.getChildren()) {
                        String assignmentTitle = assignmentSnapshot.child("assignmentTitle").getValue(String.class);
                        String status = assignmentSnapshot.child("status").getValue(String.class);
                        Log.d("Raw Assignment Data", "Title: " + assignmentTitle + ", Status: " + status);

                        AssignmentClass assignment = assignmentSnapshot.getValue(AssignmentClass.class);
                        if (assignment != null && status != null && "Done".equalsIgnoreCase(status.trim())) {
                            assignmentList.add(assignment.getAssignmentTitle());
                            assignmentKeys.add(assignmentSnapshot.getKey());
                        }
                    }
                    Log.d("Assignments", "Assignments loaded: " + assignmentList.size());
                    assignmentList.add(0, "Select completed assignment"); // Reset hint
                    assignmentAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(InputGrade.this, "Failed to load assignments", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void inputGrade() {
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
        String selectedCourseCode = courseCodeList.get(coursePosition - 1); // Adjust for spinner hint
        String selectedAssignmentKey = assignmentKeys.get(assignmentPosition - 1); // Adjust for spinner hint

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

        // Log the values to verify they are correct
        Log.d("Assignment Info", "Selected Course Code: " + selectedCourseCode);
        Log.d("Assignment Info", "Assignment Key: " + selectedAssignmentKey);
        Log.d("Assignment Info", "Weight: " + weight + ", Grade: " + grade);

        // Construct the database path and update
        assignmentsRef.child(selectedCourseCode).child(selectedAssignmentKey)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            snapshot.getRef().child("weight").setValue(weight);
                            snapshot.getRef().child("grade").setValue(grade);
                            Toast.makeText(InputGrade.this, "Grade and weight updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d("Assignment Error", "Assignment not found at: " + selectedCourseCode + "/" + selectedAssignmentKey);
                            Toast.makeText(InputGrade.this, "Assignment not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(InputGrade.this, "Failed to update grade", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}