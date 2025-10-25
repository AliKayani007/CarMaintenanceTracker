package com.example.carmaintenance;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
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

    private RecyclerView recyclerUpcoming, recyclerHistory;
    private MaintenanceAdapter upcomingAdapter, historyAdapter;
    private AppDatabase db;
    private FloatingActionButton fabAdd;
    private Button btnExport;

    private int currentOdometer = 34000; // Temporary value until we add a settings screen later

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Views
        recyclerUpcoming = findViewById(R.id.recyclerUpcoming);
        recyclerHistory = findViewById(R.id.recyclerHistory);
        fabAdd = findViewById(R.id.fabAdd);
        btnExport = findViewById(R.id.btnExport);

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

        // Export button → Export maintenance records to CSV
        btnExport.setOnClickListener(v -> {
            exportToExcel();
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

        // Get all sessions (for history)
        List<MaintenanceSession> historyList = db.maintenanceDao().getAllSessions();

        // Use separate adapters for items and sessions
        upcomingAdapter = new MaintenanceAdapter(upcomingList, true, this);   // true = upcoming mode
        historyAdapter = new MaintenanceAdapter(historyList, false, this);    // false = history mode

        recyclerUpcoming.setAdapter(upcomingAdapter);
        recyclerHistory.setAdapter(historyAdapter);
    }


    private void exportToExcel() {
        try {
            // Get all maintenance sessions
            List<MaintenanceSession> sessions = db.maintenanceDao().getAllSessions();

            // Create CSV file in app's internal storage
            String fileName = "Maintenance_Records_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".csv";
            File internalDir = new File(getExternalFilesDir(null), "exports");
            if (!internalDir.exists()) {
                internalDir.mkdirs();
            }
            File file = new File(internalDir, fileName);

            FileWriter writer = new FileWriter(file);
            
            // Write CSV header
            writer.append("Session ID,Date,Odometer,Total Cost,Notes,Item Name,Item Odometer,Next Due,Item Cost,Item Date\n");

            // Write data rows
            for (MaintenanceSession session : sessions) {
                // Get items for this session
                List<MaintenanceItem> sessionItems = db.maintenanceDao().getItemsBySessionId(session.id);
                
                if (sessionItems.isEmpty()) {
                    // If no items, create a row with just session data
                    writer.append(String.valueOf(session.id)).append(",");
                    writer.append(escapeCsv(session.date)).append(",");
                    writer.append(String.valueOf(session.odometer)).append(",");
                    writer.append(String.valueOf(session.totalCost)).append(",");
                    writer.append(escapeCsv(session.notes != null ? session.notes : "")).append(",");
                    writer.append(",,,,\n"); // Empty columns for item data
                } else {
                    // Create a row for each item
                    for (MaintenanceItem item : sessionItems) {
                        writer.append(String.valueOf(session.id)).append(",");
                        writer.append(escapeCsv(session.date)).append(",");
                        writer.append(String.valueOf(session.odometer)).append(",");
                        writer.append(String.valueOf(session.totalCost)).append(",");
                        writer.append(escapeCsv(session.notes != null ? session.notes : "")).append(",");
                        writer.append(escapeCsv(item.itemName)).append(",");
                        writer.append(String.valueOf(item.odometerDone)).append(",");
                        writer.append(String.valueOf(item.nextDue)).append(",");
                        writer.append(String.valueOf(item.cost)).append(",");
                        writer.append(escapeCsv(item.date != null ? item.date : "")).append("\n");
                    }
                }
            }

            writer.flush();
            writer.close();

            // Show success message and share the file
            Toast.makeText(this, "CSV file exported successfully: " + fileName, Toast.LENGTH_LONG).show();
            
            // Share the file so user can save it to Downloads or share it
            shareFile(file);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error exporting file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void shareFile(File file) {
        try {
            Uri fileUri = FileProvider.getUriForFile(this, 
                getApplicationContext().getPackageName() + ".fileprovider", file);
            
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/csv");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            startActivity(Intent.createChooser(shareIntent, "Share Maintenance Records"));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error sharing file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        // Escape quotes and wrap in quotes if contains comma, quote, or newline
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

}

