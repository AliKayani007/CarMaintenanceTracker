package com.example.carmaintenance;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carmaintenance.adapters.MaintenanceAdapter;
import com.example.carmaintenance.database.AppDatabase;
import com.example.carmaintenance.models.MaintenanceSession;
import com.example.carmaintenance.models.MaintenanceItem;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MaintenanceHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerHistory;
    private MaintenanceAdapter historyAdapter;
    private AppDatabase db;
    private Button btnExport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance_history);

        // Initialize Views
        recyclerHistory = findViewById(R.id.recyclerHistory);
        btnExport = findViewById(R.id.btnExport);

        // Setup RecyclerView
        recyclerHistory.setLayoutManager(new LinearLayoutManager(this));

        // Database Instance
        db = AppDatabase.getInstance(this);

        // Load Data
        loadData();

        // Export button → Export maintenance records to CSV
        btnExport.setOnClickListener(v -> {
            exportToExcel();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload latest data when returning from other activities
        loadData();
    }

    public void loadData() {
        // Get all sessions (for history)
        List<MaintenanceSession> historyList = db.maintenanceDao().getAllSessions();

        // Use adapter for sessions
        historyAdapter = new MaintenanceAdapter(historyList, false, this);    // false = history mode

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
                List<com.example.carmaintenance.models.MaintenanceItem> sessionItems = db.maintenanceDao().getItemsBySessionId(session.id);
                
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
                    for (com.example.carmaintenance.models.MaintenanceItem item : sessionItems) {
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
            android.net.Uri fileUri = androidx.core.content.FileProvider.getUriForFile(this, 
                getApplicationContext().getPackageName() + ".fileprovider", file);
            
            android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
            shareIntent.setType("text/csv");
            shareIntent.putExtra(android.content.Intent.EXTRA_STREAM, fileUri);
            shareIntent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            startActivity(android.content.Intent.createChooser(shareIntent, "Share Maintenance Records"));
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
