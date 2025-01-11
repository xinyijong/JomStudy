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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class AssignmentDetail extends AppCompatActivity {

    private ActivityAssignmentDetailBinding binding;
    private DatabaseReference assignmentRef;
    private String assignmentId; // Unique assignment identifier
    private String userId;

    ImageView backButton;
    Button editButton, deleteButton, doneButton;

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

        // Get intent data (Assignment ID passed from the adapter)
        assignmentId = getIntent().getStringExtra("assignmentId");

        // Fetch the current user ID from Firebase Authentication
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();  // Get the logged-in user's UID
            // Initialize Firebase reference to access the assignment details for the specific user
            assignmentRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId)  // Access the current user's node
                    .child("assignments")  // Access the assignments node
                    .child(assignmentId);  // Access the specific assignment node

            // Fetch the assignment details
            fetchAssignmentDetails();
        } else {
            // Handle case where the user is not authenticated
            Toast.makeText(this, "User is not authenticated", Toast.LENGTH_SHORT).show();
            finish();  // Close the activity if the user is not logged in
        }

        // Initialize UI components
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        deleteButton = findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());

        doneButton = findViewById(R.id.done_button);
        doneButton.setOnClickListener(v -> markAssignmentAsDone());
    }

    private void fetchAssignmentDetails() {
        assignmentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Retrieve assignment data from Firebase
                    String title = snapshot.child("assignmentTitle").getValue(String.class);
                    String dueDate = snapshot.child("dueDate").getValue(String.class);
                    String courseName = snapshot.child("courseName").getValue(String.class);
                    String description = snapshot.child("description").getValue(String.class);
                    Integer progress = snapshot.child("progress").getValue(Integer.class);
                    String status = snapshot.child("status").getValue(String.class);

                    // Update the UI with the fetched data
                    binding.assignmentTitle.setText(title);
                    binding.courseCode.setText(snapshot.child("courseCode").getValue(String.class)); // Display course code
                    binding.courseName.setText(courseName);
                    binding.description.setText(description);
                    binding.dueDate.setText(dueDate);
                    binding.progress.setText(progress != null ? progress + "%" : "0%");
                    binding.status.setText(status);
                } else {
                    // Handle the case when no assignment is found
                    Toast.makeText(AssignmentDetail.this, "Assignment not found", Toast.LENGTH_SHORT).show();
                    finish();  // Close the activity if assignment not found
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error while fetching data
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

    private void markAssignmentAsDone() {
        // Get the current status of the assignment from Firebase
        assignmentRef.child("status").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String currentStatus = snapshot.getValue(String.class);

                // If the status is "Done", mark it as "Not Completed"
                if ("Done".equals(currentStatus)) {
                    assignmentRef.child("status").setValue("Not Completed")
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    binding.status.setText("Not Completed");
                                    binding.status.setTextColor(getResources().getColor(android.R.color.darker_gray));
                                    Toast.makeText(AssignmentDetail.this, "Assignment marked as not completed", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(AssignmentDetail.this, "Failed to mark as not completed", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    // If the status is not "Done", change it to "Done"
                    assignmentRef.child("status").setValue("Done")
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    binding.status.setText("Done");
                                    binding.status.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                                    Toast.makeText(AssignmentDetail.this, "Assignment marked as done", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(AssignmentDetail.this, "Failed to mark as done", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AssignmentDetail.this, "Error fetching current status", Toast.LENGTH_SHORT).show();
            }
        });
    }
}