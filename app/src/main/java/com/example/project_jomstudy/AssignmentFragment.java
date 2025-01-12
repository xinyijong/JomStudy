package com.example.project_jomstudy;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_assignment, container, false);

        recyclerView = view.findViewById(R.id.assignmentsList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        list = new ArrayList<>();
        adapter = new AssignmentAdapter(requireContext(), list);
        recyclerView.setAdapter(adapter);

        // Initialize the FloatingActionButton
        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddAssignment.class);
            startActivity(intent);
        });

        // Fetch assignments for the current user
        fetchAssignments();

        return view;
    }

    private void fetchAssignments() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            database = FirebaseDatabase.getInstance().getReference("users").child(userId).child("assignments");

            database.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    list.clear();
                    for (DataSnapshot assignmentSnapshot : snapshot.getChildren()) {
                        AssignmentClass assignment = assignmentSnapshot.getValue(AssignmentClass.class);
                        if (assignment != null) {
                            assignment.setAssignmentId(assignmentSnapshot.getKey());
                            list.add(assignment);
                        }
                    }
                    adapter.notifyDataSetChanged(); // Ensure the adapter refreshes
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("AssignmentFragment", "Database error: " + error.getMessage());
                }
            });
        } else {
            Log.e("AssignmentFragment", "No user is authenticated");
        }
    }

    private void listenForCourseChanges() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            DatabaseReference coursesRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId)
                    .child("Courses");

            coursesRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                    // Course added
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                    // When a course is updated
                    String courseId = dataSnapshot.getKey();
                    String newCourseName = dataSnapshot.child("courseName").getValue(String.class);

                    if (newCourseName != null) {
                        updateAssignments(courseId, newCourseName);  // Trigger assignment update
                    }
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {}

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("CourseChangeListener", "Error listening for course changes: " + databaseError.getMessage());
                }
            });
        }
    }

    private void updateAssignments(String courseId, String newCourseName) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            DatabaseReference assignmentsRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId)
                    .child("assignments");

            // Keep this reference synced in real-time
            assignmentsRef.keepSynced(true);

            // Query assignments by courseCode
            assignmentsRef.orderByChild("courseCode").equalTo(courseId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                // For each assignment related to this course
                                for (DataSnapshot assignmentSnapshot : snapshot.getChildren()) {
                                    String assignmentId = assignmentSnapshot.getKey();
                                    Log.d("AssignmentUpdate", "Updating assignment with ID: " + assignmentId);

                                    // Update the course name in the assignment
                                    assignmentSnapshot.getRef().child("courseName").setValue(newCourseName)
                                            .addOnSuccessListener(aVoid -> {
                                                Log.d("AssignmentUpdate", "Course name updated in assignment: " + assignmentId);
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("AssignmentUpdate", "Failed to update course name in assignment", e);
                                            });
                                }

                                // After updating, refresh the assignments list to make sure the UI reflects changes
                                fetchAssignments();  // This is to reload assignments in the fragment
                            } else {
                                Log.d("AssignmentUpdate", "No assignments found with courseCode: " + courseId);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e("AssignmentUpdate", "Error querying assignments: " + error.getMessage());
                        }
                    });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        listenForCourseChanges(); // Start listening for course changes
    }
}