package com.example.carmaintenance;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carmaintenance.adapters.MaintenanceAdapter;
import com.example.carmaintenance.adapters.TodoMaintenanceAdapter;
import com.example.carmaintenance.database.AppDatabase;
import com.example.carmaintenance.models.MaintenanceItem;
import com.example.carmaintenance.models.MaintenanceSession;
import com.example.carmaintenance.models.TodoMaintenance;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerUpcoming;
    private RecyclerView recyclerTodo;
    private MaintenanceAdapter upcomingAdapter;
    private TodoMaintenanceAdapter todoAdapter;
    private AppDatabase db;
    private FloatingActionButton fabAdd;
    private Button btnViewHistory;
    private Button btnAddTodo;
    
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
        recyclerTodo = findViewById(R.id.recyclerTodo);
        fabAdd = findViewById(R.id.fabAdd);
        btnViewHistory = findViewById(R.id.btnViewHistory);
        btnAddTodo = findViewById(R.id.btnAddTodo);
        
        // Initialize Dashboard Views
        tvTotalSpent = findViewById(R.id.tvTotalSpent);
        tvLastServiceOdometer = findViewById(R.id.tvLastServiceOdometer);
        tvLastServiceCost = findViewById(R.id.tvLastServiceCost);
        tvTotalServices = findViewById(R.id.tvTotalServices);
        tvUpcomingCount = findViewById(R.id.tvUpcomingCount);
        tvOverdueCount = findViewById(R.id.tvOverdueCount);

        // Setup RecyclerViews
        recyclerUpcoming.setLayoutManager(new LinearLayoutManager(this));
        recyclerTodo.setLayoutManager(new LinearLayoutManager(this));

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

        // Add To-Do button → Show dialog to add to-do from upcoming maintenances
        btnAddTodo.setOnClickListener(v -> showAddTodoDialog());
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

        // Get to-do items (incomplete only)
        List<TodoMaintenance> todoList = db.maintenanceDao().getIncompleteTodos();

        // Setup to-do adapter
        todoAdapter = new TodoMaintenanceAdapter(todoList, this);
        recyclerTodo.setAdapter(todoAdapter);

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

    private void showAddTodoDialog() {
        // Get upcoming maintenances
        List<MaintenanceItem> upcomingList = db.maintenanceDao().getUpcoming(currentOdometer);
        
        if (upcomingList.isEmpty()) {
            Toast.makeText(this, "No upcoming maintenances to add as to-do", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add To-Do Maintenance");

        // Create spinner with upcoming maintenances
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_todo, null);
        Spinner spinnerMaintenance = dialogView.findViewById(R.id.spinnerMaintenance);

        List<String> maintenanceNames = new ArrayList<>();
        for (MaintenanceItem item : upcomingList) {
            maintenanceNames.add(item.itemName + " (Due at " + item.nextDue + " km)");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, maintenanceNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMaintenance.setAdapter(adapter);

        builder.setView(dialogView);

        builder.setPositiveButton("Add To-Do", (dialog, which) -> {
            int selectedPosition = spinnerMaintenance.getSelectedItemPosition();
            if (selectedPosition >= 0 && selectedPosition < upcomingList.size()) {
                MaintenanceItem selectedItem = upcomingList.get(selectedPosition);
                
                // Create todo maintenance
                TodoMaintenance todo = new TodoMaintenance(
                    selectedItem.itemName,
                    selectedItem.nextDue,
                    selectedItem.odometerDone,
                    false,
                    ""
                );

                db.maintenanceDao().insertTodo(todo);
                Toast.makeText(this, "To-do maintenance added", Toast.LENGTH_SHORT).show();
                loadData();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

}

