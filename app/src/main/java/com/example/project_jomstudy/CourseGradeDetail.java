package com.example.project_jomstudy;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

        // Firebase reference for assignments
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("Assignments").child(courseCode);

        // Fetch assignments from Firebase
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
                // Handle errors here
            }
        });
    }
}
