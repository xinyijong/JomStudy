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
import com.google.firebase.auth.FirebaseUser;
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
        // Get the current authenticated user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // Reference the user's assignments in the database
            String userId = currentUser.getUid();
            // Access the assignments node for this user
            database = FirebaseDatabase.getInstance().getReference("users").child(userId).child("assignments");

            // Listen for changes in the assignments node for the user
            database.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    list.clear(); // Clear the previous list
                    for (DataSnapshot assignmentSnapshot : snapshot.getChildren()) {
                        AssignmentClass assignment = assignmentSnapshot.getValue(AssignmentClass.class);
                        if (assignment != null) {
                            String key = assignmentSnapshot.getKey(); // Get the key (assignmentId)
                            assignment.setAssignmentId(key); // Set assignmentId to the object
                            Log.d("AssignmentFragment", "Assignment Key: " + key);
                            list.add(assignment); // Add assignment to the list
                        } else {
                            Log.e("AssignmentFragment", "Failed to parse assignment data");
                        }
                    }
                    adapter.notifyDataSetChanged(); // Notify the adapter to update the UI
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
}