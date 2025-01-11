package com.example.project_jomstudy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.project_jomstudy.databinding.ActivityHomeBinding;

public class Home extends AppCompatActivity {

    ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Optional: Only keep if you're using edge-to-edge display features
        EdgeToEdge.enable(this);

        View bottomAppBar = findViewById(R.id.bottomAppBar);

        // Optional: Remove this if not needed
        ViewCompat.setOnApplyWindowInsetsListener(bottomAppBar, (v, insets) -> {
            bottomAppBar.setPadding(0, 0, 0, 0);
            return insets;
        });

        // Set initial fragment if it's the first launch
        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment());  // This fragment will be shown first
        }

        Intent intent = getIntent();
        if (intent != null) {
            String fragmentToNavigate = intent.getStringExtra("navigateToFragment");
            if ("CourseFragment".equals(fragmentToNavigate)) {
                // Load the CourseFragment
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_layout, new CourseFragment()) // Replace with your actual fragment container ID
                        .commit();
            } else if ("AssignmentFragment".equals(fragmentToNavigate)) {
                // Load the AssignmentFragment
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_layout, new AssignmentFragment()) // Replace with your actual fragment container ID
                        .commit();
            }else if ("ProgressFragment".equals(fragmentToNavigate)) {
                // Load the AssignmentFragment
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_layout, new ProgressFragment()) // Replace with your actual fragment container ID
                        .commit();
            }

            // Update the selected tab
            int selectedTab = intent.getIntExtra("selectedTab", R.id.home); // Default to home tab
            binding.bottomNavigationView.setSelectedItemId(selectedTab);
        }

        // Handle bottom navigation item selection
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home) {
                replaceFragment(new HomeFragment());
            } else if (item.getItemId() == R.id.course) {
                replaceFragment(new CourseFragment());
            } else if (item.getItemId() == R.id.assignment) {
                replaceFragment(new AssignmentFragment());
            } else if (item.getItemId() == R.id.user) {
                replaceFragment(new ProfileFragment());
            } else if (item.getItemId() == R.id.progress) {
                replaceFragment(new ProgressFragment());
            }
            return true;
        });
    }

    // Replace fragment method
    private void replaceFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);  // R.id.frame_layout is the container for fragments
        fragmentTransaction.commit();
    }
}
