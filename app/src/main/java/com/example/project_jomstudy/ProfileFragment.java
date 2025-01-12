package com.example.project_jomstudy;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
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
import android.Manifest;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
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

    private Button setReminderButton;
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

        setReminderButton = view.findViewById(R.id.setReminderBtn); // Ensure the ID matches your layout
        ImageView profileImageView = view.findViewById(R.id.profileImage);
        Button editButton = view.findViewById(R.id.editProfileBtn);
        Button logoutButton = view.findViewById(R.id.logoutBtn);

        if (profileImageView != null) {
            profileImageView.setImageResource(R.drawable.baseline_account_circle_24);
        }

        selectedReminderOffset = 1 * 24 * 60 * 60 * 1000; // Default to 1 day in milliseconds

        createNotificationChannel();

        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        logoutButton.setOnClickListener(v -> logOut());

        // Spinner for reminder offset
        Spinner reminderOffsetSpinner = view.findViewById(R.id.reminderTimeSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.reminder_times, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reminderOffsetSpinner.setAdapter(adapter);

        reminderOffsetSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Set the reminder offset based on selection (in days)
                switch (position) {
                    case 0: // 1 day
                        selectedReminderOffset = 1 * 24 * 60 * 60 * 1000; // 1 day
                        break;
                    case 1: // 3 days
                        selectedReminderOffset = 3 * 24 * 60 * 60 * 1000; // 3 days
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Default to 1 day if nothing is selected
                selectedReminderOffset = 1 * 24 * 60 * 60 * 1000;
            }
        });

        setReminderButton.setOnClickListener(v -> {
            // Show a toast indicating reminders for all assignments will be set
            Toast.makeText(getContext(), "Setting reminders for all assignments", Toast.LENGTH_SHORT).show();

            // Call the method to set reminders for all assignments
            setRemindersForAllAssignments();
        });

        return view;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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

    private void setRemindersForAllAssignments() {
        // Fetch all assignments from Firebase and set reminders
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignmentsRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("assignments");

        assignmentsRef.get().addOnSuccessListener(snapshot -> {
            for (DataSnapshot assignmentSnapshot : snapshot.getChildren()) {
                String title = assignmentSnapshot.child("assignmentTitle").getValue(String.class);
                String dueDateStr = assignmentSnapshot.child("dueDate").getValue(String.class);

                if (title != null && dueDateStr != null) {
                    long dueDate = parseDueDate(dueDateStr);
                    if (dueDate >= System.currentTimeMillis()) {
                        setReminder(title, dueDateStr);
                    }
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Failed to fetch assignments: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        // Check for permission to schedule exact alarms
        checkAlarmPermission();

        long dueDate = parseDueDate(dueDateStr);
        long reminderTime = dueDate - selectedReminderOffset;

        if (reminderTime <= System.currentTimeMillis()) {
            Toast.makeText(getContext(), "Reminder time is in the past for: " + assignment, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
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
        } catch (SecurityException e) {
            Toast.makeText(getContext(), "Permission denied to schedule exact alarm.", Toast.LENGTH_SHORT).show();
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

    private void checkAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null && !alarmManager.canScheduleExactAlarms()) {
                requestPermissionLauncher.launch(Manifest.permission.SCHEDULE_EXACT_ALARM);
            }
        }
    }
}