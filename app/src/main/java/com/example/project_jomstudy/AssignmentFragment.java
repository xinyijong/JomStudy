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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;


public class AssignmentFragment extends Fragment {

    RecyclerView recyclerView;
    DatabaseReference database;
    AssignmentAdapter adapter;
    ArrayList<AssignmentClass> list;

    FloatingActionButton fab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_assignment, container, false);

        recyclerView = view.findViewById(R.id.assignmentsList);
        database = FirebaseDatabase.getInstance().getReference("Assignments");
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        list = new ArrayList<>();
        adapter = new AssignmentAdapter(requireContext(), list);
        recyclerView.setAdapter(adapter);

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear(); // Clear previous list before adding new data
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    // Get the data from the snapshot
                    Map<String, Object> assignmentData = (Map<String, Object>) dataSnapshot.getValue();

                    // Log the data to check
                    Log.d("AssignmentFragment", "Raw Data: " + assignmentData);
                    Log.d("AssignmentFragment", "Assignment Title: " + assignmentData.get("assignmentTitle"));
                    Log.d("AssignmentFragment", "Course Code: " + assignmentData.get("courseCode"));
                    Log.d("AssignmentFragment", "Course Name: " + assignmentData.get("courseName"));
                    Log.d("AssignmentFragment", "Due Date: " + assignmentData.get("dueDate"));
                    Log.d("AssignmentFragment", "Progress: " + assignmentData.get("progress"));

                    // Extract the necessary fields from the map
                    String assignmentTitle = (String) assignmentData.get("assignmentTitle");
                    String courseCode = (String) assignmentData.get("courseCode");
                    String courseName = (String) assignmentData.get("courseName");
                    String dueDate = (String) assignmentData.get("dueDate");
                    Integer progress = (Integer) assignmentData.get("progress");

                    // Log the extracted values
                    Log.d("AssignmentFragment", "Title: " + assignmentTitle + ", Code: " + courseCode);

                    // Create an AssignmentClass object with the extracted data
                    AssignmentClass assignment = new AssignmentClass();
                    assignment.setAssignmentTitle(assignmentTitle);
                    assignment.setCourseCode(courseCode);
                    assignment.setCourseName(courseName);
                    assignment.setDueDate(dueDate);
                    assignment.setProgress(progress != null ? progress : 0);

                    // Add the assignment to the list
                    list.add(assignment);
                }
                // Notify the adapter that the data has changed
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AssignmentFragment", "Database error: " + error.getMessage());
            }
        });

        // Initialize the FloatingActionButton
        fab = view.findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(v -> {
                // Navigate to the AddCourseActivity
                Intent intent = new Intent(getActivity(), AddAssignment.class);
                startActivity(intent);
            });
        }

        return view;
    }
}