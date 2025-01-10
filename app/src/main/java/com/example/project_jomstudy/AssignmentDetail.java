package com.example.project_jomstudy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.project_jomstudy.databinding.ActivityAssignmentDetailBinding;
import com.example.project_jomstudy.databinding.ActivityCourseDetailBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class AssignmentDetail extends AppCompatActivity {

    private ActivityAssignmentDetailBinding binding;
    private DatabaseReference assignmentRef;
    private String courseCode;
    private String assignmentId; // Unique assignment identifier

    ImageView backButton;
    Button editButton, deleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_assignment_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize view binding
        binding = ActivityAssignmentDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get intent data
        courseCode = getIntent().getStringExtra("courseCode");
        assignmentId = getIntent().getStringExtra("assignmentId"); // Get assignmentId passed from adapter

        // Initialize Firebase reference
        assignmentRef = FirebaseDatabase.getInstance().getReference("Assignments").child(courseCode).child(assignmentId);

        // Retrieve and display the assignment data
        fetchAssignmentDetails();

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void fetchAssignmentDetails() {
        assignmentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Fetch the assignment data from Firebase
                    String title = snapshot.child("assignmentTitle").getValue(String.class);
                    String dueDate = snapshot.child("dueDate").getValue(String.class);
                    String courseName = snapshot.child("courseName").getValue(String.class);
                    String description = snapshot.child("description").getValue(String.class);
                    Integer progress = snapshot.child("progress").getValue(Integer.class);

                    // Set the fetched data to the views using the binding
                    binding.assignmentTitle.setText(title);
                    binding.courseCode.setText(courseCode);
                    binding.courseName.setText(courseName);
                    binding.description.setText(description);
                    binding.dueDate.setText(dueDate);
                    binding.progress.setText(progress != null ? + progress + "%" : "0%");

                } else {
                    // Handle case when the assignment is not found
                    Toast.makeText(AssignmentDetail.this, "Assignment not found", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity if data is not found
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error in fetching data
                Toast.makeText(AssignmentDetail.this, "Failed to load assignment", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to show a delete confirmation dialog
    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Assignment")
                .setMessage("Are you sure you want to delete this assignment?")
                .setPositiveButton("Yes", (dialog, which) -> deleteAssignment())
                .setNegativeButton("No", null)
                .show();
    }

    // Method to delete the assignment from Firebase
    private void deleteAssignment() {
        assignmentRef.removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AssignmentDetail.this, "Assignment deleted successfully", Toast.LENGTH_SHORT).show();
                        finish(); // Close the activity after deletion
                    } else {
                        Toast.makeText(AssignmentDetail.this, "Failed to delete assignment", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}