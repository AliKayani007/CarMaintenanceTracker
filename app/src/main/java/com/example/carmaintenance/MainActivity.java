package com.example.carmaintenance;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carmaintenance.adapters.MaintenanceAdapter;
import com.example.carmaintenance.database.AppDatabase;
import com.example.carmaintenance.models.MaintenanceItem;
import com.example.carmaintenance.models.MaintenanceRecord;
import com.example.carmaintenance.models.MaintenanceSession;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerUpcoming, recyclerHistory;
    private MaintenanceAdapter upcomingAdapter, historyAdapter;
    private AppDatabase db;
    private FloatingActionButton fabAdd;

    private int currentOdometer = 34000; // Temporary value until we add a settings screen later

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Views
        recyclerUpcoming = findViewById(R.id.recyclerUpcoming);
        recyclerHistory = findViewById(R.id.recyclerHistory);
        fabAdd = findViewById(R.id.fabAdd);

        // Setup RecyclerViews
        recyclerUpcoming.setLayoutManager(new LinearLayoutManager(this));
        recyclerHistory.setLayoutManager(new LinearLayoutManager(this));

        // Database Instance
        db = AppDatabase.getInstance(this);

        // Load Data
        loadData();

        // Floating button → Add new maintenance record
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddMaintenanceActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload latest data when returning from AddMaintenanceActivity
        loadData();
    }

    private void loadData() {
        // Get upcoming items (based on odometer)
        List<MaintenanceItem> upcomingList = db.maintenanceDao().getUpcoming(currentOdometer);

        // Get all sessions (for history)
        List<MaintenanceSession> historyList = db.maintenanceDao().getAllSessions();

        // Use separate adapters for items and sessions
        upcomingAdapter = new MaintenanceAdapter(upcomingList, true);   // true = upcoming mode
        historyAdapter = new MaintenanceAdapter(historyList, false);    // false = history mode

        recyclerUpcoming.setAdapter(upcomingAdapter);
        recyclerHistory.setAdapter(historyAdapter);
    }

}
