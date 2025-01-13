package com.example.project_jomstudy;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;

public class ProgressFragment extends Fragment {
    RecyclerView recyclerView;
    DatabaseReference database; // Reference to Firebase Database for Assignments
    CourseGradeAdapter adapter; // Adapter for CourseGrade
    ArrayList<CourseGrade> courseGradeList; // List to store CourseGrade objects
    FloatingActionButton fab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_progress, container, false);

        recyclerView = view.findViewById(R.id.courseGradeList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Initialize the list and adapter
        courseGradeList = new ArrayList<>();
        adapter = new CourseGradeAdapter(requireContext(), courseGradeList);
        recyclerView.setAdapter(adapter);

        // Get current user ID from Firebase Authentication
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Reference to the user's specific "assignments" path in Firebase Database
        database = FirebaseDatabase.getInstance().getReference("users").child(userId).child("assignments");

        // Listen for data changes in the user's assignments path
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                courseGradeList.clear();  // Clear the previous list

                // Temporary maps to store data for each course
                HashMap<String, ArrayList<Double>> gradeMap = new HashMap<>();
                HashMap<String, Double> weightMap = new HashMap<>();
                HashMap<String, String> courseNames = new HashMap<>();

                // Loop through each assignment
                for (DataSnapshot assignmentSnapshot : snapshot.getChildren()) {

                    Log.d("ProgressFragment", "Assignment Snapshot: " + assignmentSnapshot.getValue());

                    // Retrieve assignment details
                    AssignmentClass assignment = assignmentSnapshot.getValue(AssignmentClass.class);

                    if (assignment != null && "Done".equalsIgnoreCase(assignment.getStatus())) {
                        double grade = assignment.getGrade() != null ? assignment.getGrade() : 0.0;
                        double weight = assignment.getWeight() != null ? assignment.getWeight() : 0.0;

                        // Store courseName for each courseCode
                        String courseCode = assignmentSnapshot.child("courseCode").getValue(String.class);
                        String courseName = assignmentSnapshot.child("courseName").getValue(String.class);

                        Log.d("ProgressFragment", "Course Code: " + courseCode + " - Course Name Retrieved: " + courseName);

                        if (courseCode != null && !courseNames.containsKey(courseCode)) {
                            courseNames.put(courseCode, courseName); // Store courseName associated with the courseCode
                        }

                        // Initialize maps for new courses
                        if (!gradeMap.containsKey(courseCode)) {
                            gradeMap.put(courseCode, new ArrayList<>());
                            weightMap.put(courseCode, 0.0);
                        }

                        // Add weighted grade to the gradeMap
                        gradeMap.get(courseCode).add(grade * weight / 100);

                        // Update total weight for each course
                        weightMap.put(courseCode, weightMap.get(courseCode) + weight);
                    }
                }

                // Calculate the average grade for each course
                for (String courseCode : gradeMap.keySet()) {
                    ArrayList<Double> weightedGrades = gradeMap.get(courseCode);
                    double totalWeightedGrade = 0.0;

                    // Sum all weighted grades for the course
                    for (Double weightedGrade : weightedGrades) {
                        totalWeightedGrade += weightedGrade;
                    }

                    // Get total weight for the course
                    double totalWeight = weightMap.get(courseCode);
                    double averageGrade = totalWeight > 0 ? totalWeightedGrade / totalWeight * 100 : 0;


                    String formattedGrade = String.format(Locale.US, "%.2f", averageGrade);

                    // Retrieve the course name from the courseNames map
                    String courseName = courseNames.get(courseCode);

                    // Create a CourseGrade object with the formatted average grade as a double
                    CourseGrade courseGrade = new CourseGrade(courseCode, courseName, Double.parseDouble(formattedGrade));

                    // Add the course grade to the list
                    courseGradeList.add(courseGrade);
                }

                // Notify the adapter that data has changed
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ProgressFragment", "Database error: " + error.getMessage());
            }
        });

        // Initialize the FloatingActionButton
        fab = view.findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(v -> {
                // Navigate to the AddAssignment activity
                Intent intent = new Intent(getActivity(), InputGrade.class);
                startActivity(intent);
            });
        }

        // Start listening for course name changes
        listenForCourseChanges();

        return view;
    }

    private void listenForCourseChanges() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference coursesRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("Courses");

        coursesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashSet<String> currentCourseCodes = new HashSet<>();
                // Collect all current course codes from the database
                for (DataSnapshot courseSnapshot : dataSnapshot.getChildren()) {
                    String courseCode = courseSnapshot.getKey();
                    String newCourseName = courseSnapshot.child("courseName").getValue(String.class);
                    if (courseCode != null) {
                        currentCourseCodes.add(courseCode);
                        // Update the course name for the specific courseCode
                        for (CourseGrade courseGrade : courseGradeList) {
                            if (courseGrade.getCourseCode().equals(courseCode)) {
                                courseGrade.setCourseName(newCourseName);
                            }
                        }
                    }
                }

                // Remove courses from courseGradeList that are no longer in the currentCourseCodes
                courseGradeList.removeIf(courseGrade -> !currentCourseCodes.contains(courseGrade.getCourseCode()));

                // Notify the adapter that data has changed
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ProgressFragment", "Error listening for course changes: " + error.getMessage());
            }
        });
    }
}