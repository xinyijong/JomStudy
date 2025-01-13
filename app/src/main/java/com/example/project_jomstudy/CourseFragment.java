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

public class CourseFragment extends Fragment {

    RecyclerView recyclerView;
    DatabaseReference database;
    CourseAdapter adapter;
    ArrayList<CourseClass> list;
    FloatingActionButton fab;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_course, container, false);

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.courseList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Initialize Firebase reference for the specific user
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        database = FirebaseDatabase.getInstance().getReference("users").child(userId).child("Courses");

        // Initialize the list and adapter
        list = new ArrayList<>();
        adapter = new CourseAdapter(requireContext(), list);
        recyclerView.setAdapter(adapter);

        // Retrieve data from the user's "Courses" node
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear(); // Clear previous list to avoid duplication
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    CourseClass course = dataSnapshot.getValue(CourseClass.class);
                    if (course != null) {
                        list.add(course);
                    }
                }
                adapter.notifyDataSetChanged(); // Notify the adapter about data changes
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CourseFragment", "Database error: " + error.getMessage());
            }
        });


        fab = view.findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(v -> {
                // Navigate to the AddCourseActivity
                Intent intent = new Intent(getActivity(), AddCourse.class);
                startActivity(intent);
            });
        }

        return view;
    }
}