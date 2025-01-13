package com.example.project_jomstudy;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;


public class HomeFragment extends Fragment {

    RecyclerView courseRecyclerView, assignmentRecyclerView;
    DatabaseReference courseDatabase, assignmentDatabase;
    CourseAdapter courseAdapter;
    AssignmentAdapter assignmentAdapter;
    ArrayList<CourseClass> courseList;
    ArrayList<AssignmentClass> assignmentList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        TextView dateTextView = view.findViewById(R.id.date);

        fetchUserName();

        // Set current date
        String currentDate = new SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(new Date());
        dateTextView.setText(currentDate);

        // Initialize RecyclerViews
        courseRecyclerView = view.findViewById(R.id.courseList);
        assignmentRecyclerView = view.findViewById(R.id.assignmentList);

        courseRecyclerView.setHasFixedSize(true);
        courseRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        assignmentRecyclerView.setHasFixedSize(true);
        assignmentRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Initialize lists and adapters
        courseList = new ArrayList<>();
        assignmentList = new ArrayList<>();
        courseAdapter = new CourseAdapter(requireContext(), courseList);
        assignmentAdapter = new AssignmentAdapter(requireContext(), assignmentList);

        courseRecyclerView.setAdapter(courseAdapter);
        assignmentRecyclerView.setAdapter(assignmentAdapter);

        // Fetch data from Firebase for the specific user
        fetchCourses();
        fetchAssignments();

        return view;
    }

    private String getCurrentUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            return user.getUid();
        } else {
            Log.e("HomeFragment", "User is not logged in.");
            // Optionally, redirect the user to the login screen
            return null;
        }
    }

    private void fetchCourses() {
        String userId = getCurrentUserId();
        if (userId != null) {
            courseDatabase = FirebaseDatabase.getInstance().getReference("users").child(userId).child("Courses");
            courseDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    courseList.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        CourseClass course = dataSnapshot.getValue(CourseClass.class);
                        if (course != null) {
                            courseList.add(course);
                        }
                    }
                    Collections.sort(courseList, new Comparator<CourseClass>() {
                        @Override
                        public int compare(CourseClass o1, CourseClass o2) {
                            return getDayOfWeekOrder(o1.getDay()) - getDayOfWeekOrder(o2.getDay());
                        }
                    });
                    courseAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("HomeFragment", "Failed to fetch courses: " + error.getMessage());
                }
            });
        }
    }

    private void fetchAssignments() {
        String userId = getCurrentUserId();
        if (userId != null) {
            assignmentDatabase = FirebaseDatabase.getInstance().getReference("users").child(userId).child("assignments");

            assignmentDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    assignmentList.clear();
                    for (DataSnapshot assignmentSnapshot : snapshot.getChildren()) {
                        AssignmentClass assignment = assignmentSnapshot.getValue(AssignmentClass.class);
                        if (assignment != null) {
                            assignment.setAssignmentId(assignmentSnapshot.getKey());

                            // Check if the assignment's course exists and its status is "not completed"
                            if ("not completed".equalsIgnoreCase(assignment.getStatus()) &&
                                    courseList.stream().anyMatch(course -> course.getCourseCode().equals(assignment.getCourseCode()))) {
                                assignmentList.add(assignment);
                            }
                        }
                    }

                    // Sort assignments by due date (closest first)
                    Collections.sort(assignmentList, new Comparator<AssignmentClass>() {
                        @Override
                        public int compare(AssignmentClass o1, AssignmentClass o2) {
                            return parseDate(o1.getDueDate()).compareTo(parseDate(o2.getDueDate()));
                        }
                    });

                    assignmentAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("HomeFragment", "Failed to fetch assignments: " + error.getMessage());
                }
            });
        }
    }

    private Date parseDate(String date) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return dateFormat.parse(date);
        } catch (ParseException e) {
            Log.e("HomeFragment", "Date parsing failed for date: " + date + " Error: " + e.getMessage());
            return new Date(Long.MAX_VALUE); // Return a far future date to push invalid dates to the end
        }
    }

    private int getDayOfWeekOrder(String day) {
        switch (day.toLowerCase(Locale.ROOT)) {
            case "monday": return 1;
            case "tuesday": return 2;
            case "wednesday": return 3;
            case "thursday": return 4;
            case "friday": return 5;
            default: return 8;
        }
    }

    private void fetchUserName() {
        // Get the current userId from Firebase Authentication
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Reference to the user's data in Firebase Realtime Database
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        // Retrieve data from Firebase
        userRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                // Get the name from the snapshot
                String name = snapshot.child("name").getValue(String.class);

                // Ensure getView() is not null before updating UI
                if (getView() != null) {
                    TextView nameTextView = getView().findViewById(R.id.username);


                    if (nameTextView != null) {
                        nameTextView.setText(name != null ? name : "No Name");
                    }
                }
            } else {
                Log.e("HomeFragment", "User data not found");
            }
        }).addOnFailureListener(e -> {
            // Handle potential errors
            if (getContext() != null) {
                Toast.makeText(getContext(), "Failed to fetch data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}