package com.example.carmaintenance;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.carmaintenance.database.AppDatabase;
import com.example.carmaintenance.models.MaintenanceItem;
import com.example.carmaintenance.models.MaintenanceSession;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddMaintenanceActivity extends AppCompatActivity {

    private EditText editOdometer, editCost, editNotes;
    private CheckBox chkOil, chkCoolant, chkCVT, chkPlugs, chkBrakes, chkFuelFilter;
    private CheckBox chkAirFilter, chkACFilter, chkBrakeFluid, chkAlignment, chkBattery, chkTimingChain;
    private Button btnSave;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_maintenance);

        db = AppDatabase.getInstance(this);

        editOdometer = findViewById(R.id.editOdometer);
        editCost = findViewById(R.id.editCost);
        editNotes = findViewById(R.id.editNotes);

        // Updated maintenance checkboxes
        chkOil = findViewById(R.id.chkOil);
        chkCoolant = findViewById(R.id.chkCoolant);
        chkCVT = findViewById(R.id.chkCVT);
        chkPlugs = findViewById(R.id.chkPlugs);
        chkBrakes = findViewById(R.id.chkBrakes);
        chkFuelFilter = findViewById(R.id.chkFuelFilter);
        chkAirFilter = findViewById(R.id.chkAirFilter);
        chkACFilter = findViewById(R.id.chkACFilter);
        chkBrakeFluid = findViewById(R.id.chkBrakeFluid);
        chkAlignment = findViewById(R.id.chkAlignment);
        chkBattery = findViewById(R.id.chkBattery);
        chkTimingChain = findViewById(R.id.chkTimingChain);

        btnSave = findViewById(R.id.btnSave);

        btnSave.setOnClickListener(v -> saveMaintenance());
    }

    private void saveMaintenance() {
        String odoStr = editOdometer.getText().toString().trim();
        String costStr = editCost.getText().toString().trim();

        if (odoStr.isEmpty() || costStr.isEmpty()) {
            Toast.makeText(this, "Please enter odometer and cost", Toast.LENGTH_SHORT).show();
            return;
        }

        int odometer = Integer.parseInt(odoStr);
        double totalCost = Double.parseDouble(costStr);
        String notes = editNotes.getText().toString();

        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Insert Session
        MaintenanceSession session = new MaintenanceSession(date, odometer, totalCost, notes);
        long sessionId = db.maintenanceDao().insertSession(session);

        // Insert Items
        List<MaintenanceItem> items = new ArrayList<>();

        if (chkOil.isChecked())
            items.add(new MaintenanceItem((int) sessionId, "Engine Oil", odometer, calculateNextDue("engine oil", odometer), 0, date));
        if (chkCoolant.isChecked())
            items.add(new MaintenanceItem((int) sessionId, "Coolant", odometer, calculateNextDue("coolant", odometer), 0, date));
        if (chkCVT.isChecked())
            items.add(new MaintenanceItem((int) sessionId, "CVT Fluid", odometer, calculateNextDue("cvt fluid", odometer), 0, date));
        if (chkPlugs.isChecked())
            items.add(new MaintenanceItem((int) sessionId, "Spark Plugs", odometer, calculateNextDue("spark plugs", odometer), 0, date));
        if (chkBrakes.isChecked())
            items.add(new MaintenanceItem((int) sessionId, "Brake Service", odometer, calculateNextDue("brake service", odometer), 0, date));
        if (chkFuelFilter.isChecked())
            items.add(new MaintenanceItem((int) sessionId, "Fuel Filter", odometer, calculateNextDue("fuel filter", odometer), 0, date));
        if (chkAirFilter.isChecked())
            items.add(new MaintenanceItem((int) sessionId, "Air Filter", odometer, calculateNextDue("air filter", odometer), 0, date));
        if (chkACFilter.isChecked())
            items.add(new MaintenanceItem((int) sessionId, "AC Filter", odometer, calculateNextDue("ac filter", odometer), 0, date));
        if (chkBrakeFluid.isChecked())
            items.add(new MaintenanceItem((int) sessionId, "Brake Fluid", odometer, calculateNextDue("brake fluid", odometer), 0, date));
        if (chkAlignment.isChecked())
            items.add(new MaintenanceItem((int) sessionId, "Alignment", odometer, calculateNextDue("alignment", odometer), 0, date));
        if (chkBattery.isChecked())
            items.add(new MaintenanceItem((int) sessionId, "Battery Service", odometer, calculateNextDue("battery service", odometer), 0, date));
        if (chkTimingChain.isChecked())
            items.add(new MaintenanceItem((int) sessionId, "Timing Chain", odometer, calculateNextDue("timing chain", odometer), 0, date));

        if (!items.isEmpty()) {
            db.maintenanceDao().insertItems(items);
            Toast.makeText(this, "Maintenance saved successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Please select at least one item", Toast.LENGTH_SHORT).show();
        }
    }

    private int calculateNextDue(String itemName, int currentOdo) {
        switch (itemName.toLowerCase()) {
            case "engine oil": return currentOdo + 5000;
            case "coolant":
            case "radiator coolant": return currentOdo + 60000;
            case "cvt fluid": return currentOdo + 60000;
            case "spark plugs": return currentOdo + 30000;
            case "brake service": return currentOdo + 10000;
            case "fuel filter": return currentOdo + 100000;
            case "air filter": return currentOdo + 10000;
            case "ac filter": return currentOdo + 15000;
            case "brake fluid": return currentOdo + 40000;
            case "power steering fluid": return currentOdo + 60000;
            case "battery service": return currentOdo + 30000;
            case "timing chain": return currentOdo + 120000;
            default: return currentOdo + 10000;
        }
    }
}
