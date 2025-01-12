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

    private static final int REQUEST_CODE_EDIT_ASSIGNMENT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_assignment_detail);

        binding = ActivityAssignmentDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        assignmentId = getIntent().getStringExtra("assignmentId");

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
            assignmentRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId)
                    .child("assignments")
                    .child(assignmentId);

            fetchAssignmentDetails();
        } else {
            Toast.makeText(this, "User is not authenticated", Toast.LENGTH_SHORT).show();
            finish();
        }

        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        editButton = findViewById(R.id.editButton);
        editButton.setOnClickListener(v -> passAssignmentData());

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
                    String title = snapshot.child("assignmentTitle").getValue(String.class);
                    String dueDate = snapshot.child("dueDate").getValue(String.class);
                    String courseName = snapshot.child("courseName").getValue(String.class);
                    String description = snapshot.child("description").getValue(String.class);
                    Integer progress = snapshot.child("progress").getValue(Integer.class);
                    String status = snapshot.child("status").getValue(String.class);

                    binding.assignmentTitle.setText(title);
                    binding.courseCode.setText(snapshot.child("courseCode").getValue(String.class));
                    binding.courseName.setText(courseName);
                    binding.description.setText(description);
                    binding.dueDate.setText(dueDate);

                    // Ensure you pass progress value without the percentage symbol
                    binding.progress.setText(progress != null ? progress + "%" : "0%");
                    binding.status.setText(status);
                } else {
                    Toast.makeText(AssignmentDetail.this, "Assignment not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AssignmentDetail.this, "Failed to load assignment", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void passAssignmentData() {
        String assignmentTitle = binding.assignmentTitle.getText().toString().trim();
        String dueDate = binding.dueDate.getText().toString().trim();
        String courseCode = binding.courseCode.getText().toString().trim();
        String courseName = binding.courseName.getText().toString().trim();
        String description = binding.description.getText().toString().trim();
        String status = binding.status.getText().toString().trim();
        String progressText = binding.progress.getText().toString().trim();

        // Remove '%' symbol and parse it as an integer
        int progress = 0;
        try {
            progress = Integer.parseInt(progressText.replace("%", "").trim()); // Remove '%' and parse as integer
        } catch (NumberFormatException e) {
            // Handle the error if the progress is not a valid number
            Toast.makeText(this, "Invalid progress value", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(AssignmentDetail.this, EditAssignment.class);
        intent.putExtra("assignmentId", assignmentId);
        intent.putExtra("assignmentTitle", assignmentTitle);
        intent.putExtra("dueDate", dueDate);
        intent.putExtra("courseCode", courseCode);
        intent.putExtra("courseName", courseName);
        intent.putExtra("description", description);
        intent.putExtra("status", status);
        intent.putExtra("progress", progress); // Pass progress as integer

        startActivityForResult(intent, REQUEST_CODE_EDIT_ASSIGNMENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EDIT_ASSIGNMENT && resultCode == RESULT_OK) {
            fetchAssignmentDetails();
        }
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Assignment")
                .setMessage("Are you sure you want to delete this assignment?")
                .setPositiveButton("Yes", (dialog, which) -> deleteAssignment())
                .setNegativeButton("No", null)
                .show();
    }

    private void deleteAssignment() {
        assignmentRef.removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(AssignmentDetail.this, "Assignment deleted successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AssignmentDetail.this, "Failed to delete assignment", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void markAssignmentAsDone() {
        assignmentRef.child("status").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String currentStatus = snapshot.getValue(String.class);

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