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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

        // Firebase Authentication to get the user ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser != null ? currentUser.getUid() : null;  // Get the user ID

        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();  // Close the activity if no user is logged in
            return;
        }

        database = FirebaseDatabase.getInstance();
        coursesRef = database.getReference("users").child(userId).child("Courses");
        assignmentsRef = database.getReference("users").child(userId).child("assignments");

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

        // Fetch courses specific to the user
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
        // Clear the assignment lists to prevent duplicates
        assignmentList.clear();
        assignmentKeys.clear();

        // Loop through all assignments to find those matching the selected course
        assignmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot assignmentSnapshot : snapshot.getChildren()) {
                    String assignmentTitle = assignmentSnapshot.child("assignmentTitle").getValue(String.class);
                    String status = assignmentSnapshot.child("status").getValue(String.class);
                    String courseCode = assignmentSnapshot.child("courseCode").getValue(String.class); // Get the courseCode for the assignment

                    // Check if the courseCode matches the selected course (you may need to adjust this logic based on your course list)
                    int courseIndex = courseSpinner.getSelectedItemPosition() - 1; // Subtract 1 to account for the "Select course" hint
                    String selectedCourseCode = courseCodeList.get(courseIndex); // Get the selected course's code

                    // Only add assignments that match the selected course and have a "Done" status
                    if (selectedCourseCode != null && selectedCourseCode.equals(courseCode) && "Done".equalsIgnoreCase(status.trim())) {
                        assignmentList.add(assignmentTitle);
                        assignmentKeys.add(assignmentSnapshot.getKey());
                    }
                }

                // Add the hint item as the first item
                assignmentList.add(0, "Select completed assignment");
                assignmentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(InputGrade.this, "Failed to load assignments", Toast.LENGTH_SHORT).show();
            }
        });
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

        // Construct the correct path to the assignment (without using course code as parent)
        DatabaseReference assignmentRef = assignmentsRef.child(selectedAssignmentKey);

        // Update the grade and weight
        assignmentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    snapshot.getRef().child("weight").setValue(weight);
                    snapshot.getRef().child("grade").setValue(grade);
                    Toast.makeText(InputGrade.this, "Grade and weight updated successfully", Toast.LENGTH_SHORT).show();
                } else {
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