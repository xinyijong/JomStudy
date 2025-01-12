package com.example.project_jomstudy;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.TimeZone;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProfileFragment extends Fragment {

    private Spinner reminderSpinner, reminderTimeSpinner;
    private List<String> assignmentList = new ArrayList<>();
    private DatabaseReference userRef;
    private int selectedReminderOffset;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        reminderSpinner = view.findViewById(R.id.reminderSpinner);
        reminderTimeSpinner = view.findViewById(R.id.reminderTimeSpinner);
        Button editButton = view.findViewById(R.id.editProfileBtn);
        Button logoutButton = view.findViewById(R.id.logoutBtn);
        Button setReminderButton = view.findViewById(R.id.setReminderBtn); // Ensure the ID matches your layout
        ImageView profileImageView = view.findViewById(R.id.profileImage);

        if (profileImageView != null) {
            profileImageView.setImageResource(R.drawable.baseline_account_circle_24);
        }

        selectedReminderOffset = 24 * 60 * 60 * 1000; // Default to 1 day in milliseconds

        createNotificationChannel();

        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> logOut());

        fetchAssignments();

        reminderTimeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedReminderOffset = (position == 0) ? 24 * 60 * 60 * 1000 : 3 * 24 * 60 * 60 * 1000;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (!isGranted) {
                Toast.makeText(getContext(), "Permission denied! Cannot set reminders or notifications.", Toast.LENGTH_SHORT).show();
            }
        });

        setReminderButton.setOnClickListener(v -> {
            String selectedAssignment = reminderSpinner.getSelectedItem().toString();
            if (!"Select Assignment".equals(selectedAssignment)) {
                fetchAssignmentDueDate(selectedAssignment);
            } else {
                Toast.makeText(getContext(), "Please select an assignment.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String channelId = "assignment_reminders";
            String channelName = "Assignment Reminders";
            String channelDescription = "Notifications for assignment reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription(channelDescription);

            NotificationManager notificationManager = requireContext().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void fetchAssignments() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignmentsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("assignments");

        assignmentsRef.get().addOnSuccessListener(snapshot -> {
            assignmentList.clear();
            assignmentList.add("Select Assignment");

            for (DataSnapshot assignmentSnapshot : snapshot.getChildren()) {
                String title = assignmentSnapshot.child("assignmentTitle").getValue(String.class);
                String dueDateStr = assignmentSnapshot.child("dueDate").getValue(String.class);

                if (title != null && dueDateStr != null) {
                    long dueDate = parseDueDate(dueDateStr);
                    if (dueDate >= System.currentTimeMillis()) {
                        assignmentList.add(title);
                    }
                }
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, assignmentList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            reminderSpinner.setAdapter(adapter);

            if (assignmentList.size() == 1) {
                Toast.makeText(getContext(), "No assignments available.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to fetch assignments: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void fetchAssignmentDueDate(String selectedAssignment) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignmentsRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("assignments");

        assignmentsRef.get().addOnSuccessListener(snapshot -> {
            for (DataSnapshot assignmentSnapshot : snapshot.getChildren()) {
                String title = assignmentSnapshot.child("assignmentTitle").getValue(String.class);
                if (selectedAssignment.equals(title)) {
                    String dueDateStr = assignmentSnapshot.child("dueDate").getValue(String.class);
                    setReminder(selectedAssignment, dueDateStr);
                    break;
                }
            }
        });
    }

    private long parseDueDate(String dueDateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/M/yyyy HH:mm", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault()); // Adjust to your preferred timezone if needed
        try {
            return sdf.parse(dueDateStr).getTime();
        } catch (ParseException e) {
            Log.e("ProfileFragment", "Failed to parse due date: " + dueDateStr, e);
            return 0; // Return 0 if parsing fails
        }
    }


    private void setReminder(String assignment, String dueDateStr) {
        long dueDate = parseDueDate(dueDateStr);
        long reminderTime = dueDate - selectedReminderOffset;

        if (reminderTime <= System.currentTimeMillis()) {
            Toast.makeText(getContext(), "Reminder time is in the past!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(getContext(), ReminderReceiver.class);
        intent.putExtra("assignmentTitle", assignment);
        intent.putExtra("daysBeforeDue", String.valueOf(selectedReminderOffset / (24 * 60 * 60 * 1000)));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getContext(),
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
            Toast.makeText(getContext(), "Reminder set for: " + assignment, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Failed to set reminder. AlarmManager is null.", Toast.LENGTH_SHORT).show();
        }
    }

    private void logOut() {
        FirebaseAuth.getInstance().signOut();
        Toast.makeText(getContext(), "Log out successfully", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getActivity(), LogIn.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchUserData();
    }

    private void fetchUserData() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        userRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String name = snapshot.child("name").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);

                if (getView() != null) {
                    TextView nameTextView = getView().findViewById(R.id.userName);
                    TextView emailTextView = getView().findViewById(R.id.userEmail);

                    if (nameTextView != null) nameTextView.setText(name != null ? name : "No Name");
                    if (emailTextView != null) emailTextView.setText(email != null ? email : "No Email");
                }
            } else {
                Toast.makeText(getContext(), "User data not found.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to fetch user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
