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
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Spinner;


import java.util.ArrayList;
import androidx.appcompat.widget.SearchView;
import java.util.Comparator;

public class AssignmentFragment extends Fragment {

    RecyclerView recyclerView;
    DatabaseReference database;
    AssignmentAdapter adapter;
    ArrayList<AssignmentClass> list, filteredList;
    FloatingActionButton fab;
    SearchView searchView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_assignment, container, false);

        recyclerView = view.findViewById(R.id.assignmentsList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        list = new ArrayList<>();
        filteredList = new ArrayList<>();
        adapter = new AssignmentAdapter(requireContext(), filteredList);
        recyclerView.setAdapter(adapter);

        fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddAssignment.class);
            startActivity(intent);
        });

        searchView = view.findViewById(R.id.searchView);
        Spinner sortSpinner = view.findViewById(R.id.sortSpinner); // Initialize the Spinner

        fetchAssignments();
        setupSearch();
        setupSort(sortSpinner); // Pass the Spinner to the setupSort method

        return view;
    }
    private void fetchAssignments() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            database = FirebaseDatabase.getInstance().getReference("users").child(userId);

            DatabaseReference assignmentsRef = database.child("assignments");
            DatabaseReference coursesRef = database.child("Courses");

            assignmentsRef.addValueEventListener(new ValueEventListener() {
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
                    filteredList.clear();
                    filteredList.addAll(list);
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("AssignmentFragment", "Database error: " + error.getMessage());
                }
            });

            // Listen for course deletions
            coursesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ArrayList<String> existingCourseCodes = new ArrayList<>();
                    for (DataSnapshot courseSnapshot : dataSnapshot.getChildren()) {
                        String courseCode = courseSnapshot.getKey();
                        if (courseCode != null) {
                            existingCourseCodes.add(courseCode);
                        }
                    }

                    // Remove assignments with deleted course codes
                    list.removeIf(assignment -> !existingCourseCodes.contains(assignment.getCourseCode()));
                    filteredList.removeIf(assignment -> !existingCourseCodes.contains(assignment.getCourseCode()));
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("AssignmentFragment", "Error listening for course deletions: " + error.getMessage());
                }
            });
        }
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterAssignments(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterAssignments(newText);
                return true;
            }
        });
    }

    private void filterAssignments(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(list);
        } else {
            for (AssignmentClass assignment : list) {
                if (assignment.getAssignmentTitle().toLowerCase().contains(query.toLowerCase()) ||
                        assignment.getCourseName().toLowerCase().contains(query.toLowerCase()) ||
                        assignment.getCourseCode().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(assignment);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void setupSort(Spinner sortSpinner) {
        String[] options = {"Due Date", "Course Name", "Progress"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, options);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(spinnerAdapter);

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        sortByDueDate();
                        break;
                    case 1:
                        sortByCourseName();
                        break;
                    case 2:
                        sortByProgress();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
    private void sortByDueDate() {
        filteredList.sort(Comparator.comparing(AssignmentClass::getDueDate));
        adapter.notifyItemRangeChanged(0, filteredList.size()); // Notify adapter about range change
    }

    private void sortByCourseName() {
        filteredList.sort(Comparator.comparing(AssignmentClass::getCourseName));
        adapter.notifyItemRangeChanged(0, filteredList.size()); // Notify adapter about range change
    }


    private void sortByProgress() {
        filteredList.sort(Comparator.comparingInt(AssignmentClass::getProgress).reversed());
        adapter.notifyItemRangeChanged(0, filteredList.size()); // Notify adapter about range change
    }


}
