package com.example.carmaintenance;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.FileProvider;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carmaintenance.adapters.MaintenanceAdapter;
import com.example.carmaintenance.database.AppDatabase;
import com.example.carmaintenance.models.MaintenanceItem;
import com.example.carmaintenance.models.MaintenanceSession;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerUpcoming;
    private MaintenanceAdapter upcomingAdapter;
    private AppDatabase db;
    private FloatingActionButton fabAdd;
    private Button btnViewHistory;
    
    // Dashboard views
    private TextView tvTotalSpent, tvLastServiceOdometer, tvLastServiceCost;
    private TextView tvTotalServices, tvUpcomingCount, tvOverdueCount;

    private int currentOdometer = 34000; // Temporary value until we add a settings screen later

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Views
        recyclerUpcoming = findViewById(R.id.recyclerUpcoming);
        fabAdd = findViewById(R.id.fabAdd);
        btnViewHistory = findViewById(R.id.btnViewHistory);
        
        // Initialize Dashboard Views
        tvTotalSpent = findViewById(R.id.tvTotalSpent);
        tvLastServiceOdometer = findViewById(R.id.tvLastServiceOdometer);
        tvLastServiceCost = findViewById(R.id.tvLastServiceCost);
        tvTotalServices = findViewById(R.id.tvTotalServices);
        tvUpcomingCount = findViewById(R.id.tvUpcomingCount);
        tvOverdueCount = findViewById(R.id.tvOverdueCount);

        // Setup RecyclerView
        recyclerUpcoming.setLayoutManager(new LinearLayoutManager(this));

        // Database Instance
        db = AppDatabase.getInstance(this);

        // Load Data
        loadData();

        // Floating button → Add new maintenance record
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddMaintenanceActivity.class);
            startActivity(intent);
        });

        // View History button → Navigate to maintenance history
        btnViewHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MaintenanceHistoryActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload latest data when returning from AddMaintenanceActivity
        loadData();
    }

    public void loadData() {
        // Get upcoming items (based on odometer)
        List<MaintenanceItem> upcomingList = db.maintenanceDao().getUpcoming(currentOdometer);

        // Setup upcoming maintenance adapter
        upcomingAdapter = new MaintenanceAdapter(upcomingList, true, this);   // true = upcoming mode
        recyclerUpcoming.setAdapter(upcomingAdapter);

        // Update dashboard
        updateDashboard();
    }

    private void updateDashboard() {
        // Get dashboard data
        double totalSpent = db.maintenanceDao().getTotalSpent();
        MaintenanceSession lastService = db.maintenanceDao().getLastService();
        int totalServices = db.maintenanceDao().getTotalServices();
        int upcomingCount = db.maintenanceDao().getUpcomingCount(currentOdometer);
        int overdueCount = db.maintenanceDao().getOverdueCount(currentOdometer);

        // Update dashboard views
        tvTotalSpent.setText(String.format("PKR %.0f", totalSpent));
        tvTotalServices.setText(String.valueOf(totalServices));
        tvUpcomingCount.setText(String.valueOf(upcomingCount));
        tvOverdueCount.setText(String.valueOf(overdueCount));

        // Update last service info
        if (lastService != null) {
            tvLastServiceOdometer.setText(String.format("%d km", lastService.odometer));
            tvLastServiceCost.setText(String.format("PKR %.0f", lastService.totalCost));
        } else {
            tvLastServiceOdometer.setText("No service yet");
            tvLastServiceCost.setText("PKR 0");
        }
    }


}

