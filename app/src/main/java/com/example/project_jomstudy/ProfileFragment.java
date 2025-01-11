package com.example.project_jomstudy;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProfileFragment extends Fragment {

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Find the Edit button in the layout
        Button editButton = view.findViewById(R.id.editProfileBtn);

        // Set click listener on the Edit button
        editButton.setOnClickListener(v -> {
            // Navigate to EditProfileActivity
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        // Set dummy image
        ImageView profileImageView = view.findViewById(R.id.profileImage);
        if (profileImageView != null) {
            profileImageView.setImageResource(R.drawable.baseline_account_circle_24);
        }

        // Find the Logout button
        Button logoutButton = view.findViewById(R.id.logoutBtn);

        // Set click listener on the Logout button
        logoutButton.setOnClickListener(v -> {
            // Perform logout
            logOut();
        });

        return view;
    }

    private void logOut() {
        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut();

        // Show logout success message
        if (getContext() != null) {
            Toast.makeText(getContext(), "Log out successfully", Toast.LENGTH_SHORT).show();
        }

        // Navigate to login activity
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

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String name = snapshot.child("name").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);

                // Ensure getView() is not null
                if (getView() != null) {
                    TextView nameTextView = getView().findViewById(R.id.userName);
                    TextView emailTextView = getView().findViewById(R.id.userEmail);

                    // Avoid NullPointerException
                    if (nameTextView != null) {
                        nameTextView.setText(name != null ? name : "No Name");
                    }
                    if (emailTextView != null) {
                        emailTextView.setText(email != null ? email : "No Email");
                    }
                }
            }
        }).addOnFailureListener(e -> {
            // Handle database errors
            if (getContext() != null) {
                Toast.makeText(getContext(), "Failed to fetch data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
