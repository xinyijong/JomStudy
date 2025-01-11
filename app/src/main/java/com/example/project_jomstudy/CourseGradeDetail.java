package com.example.project_jomstudy;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CourseGradeDetail extends AppCompatActivity {

    RecyclerView recyclerView;
    AssignmentGradeAdapter adapter;
    ArrayList<AssignmentClass> assignmentList;
    DatabaseReference database;
    String courseCode;

    ImageView backButton;

    TextView courseCodeText, courseNameText, averageGradeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_course_grade_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.assignmentsGradeList);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        courseCodeText = findViewById(R.id.courseCode);
        courseNameText = findViewById(R.id.courseName);
        averageGradeText = findViewById(R.id.averageGrade);

        // Initialize list and adapter
        assignmentList = new ArrayList<>();
        adapter = new AssignmentGradeAdapter(this, assignmentList);
        recyclerView.setAdapter(adapter);

        // Retrieve course details from intent
        String courseCode = getIntent().getStringExtra("courseCode");
        String courseName = getIntent().getStringExtra("courseName");
        double averageGrade = getIntent().getDoubleExtra("averageGrade", 0.0); // Use double here

        // Set course details
        courseCodeText.setText(courseCode);
        courseNameText.setText(courseName);
        averageGradeText.setText(String.format("%.2f", averageGrade));

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Get current user ID from Firebase Authentication
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Firebase reference for assignments for the current user and specific course
        database = FirebaseDatabase.getInstance().getReference("users")
                .child(userId)
                .child("assignments");

        // Fetch assignments from Firebase for the specific user and course
        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                assignmentList.clear();
                for (DataSnapshot assignmentSnapshot : snapshot.getChildren()) {
                    AssignmentClass assignment = assignmentSnapshot.getValue(AssignmentClass.class);

                    // Filter assignments by "done" status
                    if (assignment != null && "Done".equals(assignment.getStatus())) {
                        assignmentList.add(assignment);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors here (e.g., log errors)
            }
        });
    }
}