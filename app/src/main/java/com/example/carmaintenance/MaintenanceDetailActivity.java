package com.example.carmaintenance;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carmaintenance.adapters.MaintenanceItemAdapter;
import com.example.carmaintenance.database.AppDatabase;
import com.example.carmaintenance.models.MaintenanceItem;
import com.example.carmaintenance.models.MaintenanceSession;

import java.util.List;

public class MaintenanceDetailActivity extends AppCompatActivity {

    private TextView tvServiceDate, tvOdometer, tvTotalCost, tvNotes;
    private RecyclerView recyclerItems;
    private AppDatabase db;
    private int sessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance_detail);

        // Get session ID from intent
        sessionId = getIntent().getIntExtra("sessionId", -1);
        if (sessionId == -1) {
            finish();
            return;
        }

        db = AppDatabase.getInstance(this);

        // Initialize views
        tvServiceDate = findViewById(R.id.tvServiceDate);
        tvOdometer = findViewById(R.id.tvOdometer);
        tvTotalCost = findViewById(R.id.tvTotalCost);
        tvNotes = findViewById(R.id.tvNotes);
        recyclerItems = findViewById(R.id.recyclerItems);

        // Setup RecyclerView
        recyclerItems.setLayoutManager(new LinearLayoutManager(this));

        // Load and display data
        loadMaintenanceDetails();
    }

    private void loadMaintenanceDetails() {
        // Get session details
        List<MaintenanceSession> sessions = db.maintenanceDao().getAllSessions();
        MaintenanceSession session = null;
        for (MaintenanceSession s : sessions) {
            if (s.id == sessionId) {
                session = s;
                break;
            }
        }

        if (session != null) {
            // Display session info
            tvServiceDate.setText("Service Date: " + session.date);
            tvOdometer.setText("Odometer: " + session.odometer + " km");
            tvTotalCost.setText("Total Cost: Rs " + session.totalCost);
            tvNotes.setText(session.notes != null && !session.notes.isEmpty() ? 
                "Notes: " + session.notes : "No notes");

            // Get and display maintenance items
            List<MaintenanceItem> items = db.maintenanceDao().getItemsForSession(sessionId);
            MaintenanceItemAdapter adapter = new MaintenanceItemAdapter(items);
            recyclerItems.setAdapter(adapter);
        }
    }
}
