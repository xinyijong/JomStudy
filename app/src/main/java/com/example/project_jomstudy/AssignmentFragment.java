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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_assignment, container, false);

        recyclerView = view.findViewById(R.id.assignmentsList);
        database = FirebaseDatabase.getInstance().getReference("Assignments");
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        list = new ArrayList<>();
        adapter = new AssignmentAdapter(requireContext(), list);
        recyclerView.setAdapter(adapter);

        // Fetch assignments
        fetchAssignments();

        // Initialize the FloatingActionButton
        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddAssignment.class);
            startActivity(intent);
        });

        return view;
    }

    private void fetchAssignments() {
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear(); // Clear the previous list
                for (DataSnapshot courseSnapshot : snapshot.getChildren()) {
                    // Loop through each assignment under a course code
                    for (DataSnapshot assignmentSnapshot : courseSnapshot.getChildren()) {
                        AssignmentClass assignment = assignmentSnapshot.getValue(AssignmentClass.class);
                        if (assignment != null) {
                            String key = assignmentSnapshot.getKey(); // Get the key (assignmentId)
                            assignment.setAssignmentId(key); // Set assignmentId to the object
                            Log.d("AssignmentFragment", "Assignment Key: " + key);
                            list.add(assignment);
                        } else {
                            Log.e("AssignmentFragment", "Failed to parse assignment data");
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AssignmentFragment", "Database error: " + error.getMessage());
            }
        });
    }
}