package com.example.carmaintenance;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
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

    private EditText editOdometer, editCost, editNotes, editServiceDate;
    private CheckBox chkOil, chkCoolant, chkCVT, chkPlugs, chkBrakes, chkFuelFilter;
    private CheckBox chkAlignment, chkThrottleBody, chkEngineAirFilter, chkCabinAirFilter, chkBrakePads;
    private Button btnSave, btnSelectDate;
    private AppDatabase db;
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_maintenance);

        db = AppDatabase.getInstance(this);

        editOdometer = findViewById(R.id.editOdometer);
        editCost = findViewById(R.id.editCost);
        editNotes = findViewById(R.id.editNotes);
        editServiceDate = findViewById(R.id.editServiceDate);

        // Updated maintenance checkboxes
        chkOil = findViewById(R.id.chkOil);
        chkCoolant = findViewById(R.id.chkCoolant);
        chkCVT = findViewById(R.id.chkCVT);
        chkPlugs = findViewById(R.id.chkPlugs);
        chkBrakes = findViewById(R.id.chkBrakes);
        chkFuelFilter = findViewById(R.id.chkFuelFilter);
        chkAlignment = findViewById(R.id.chkAlignment);
        chkThrottleBody = findViewById(R.id.chkThrottleBody);
        chkEngineAirFilter = findViewById(R.id.chkEngineAirFilter);
        chkCabinAirFilter = findViewById(R.id.chkCabinAirFilter);
        chkBrakePads = findViewById(R.id.chkBrakePads);

        btnSave = findViewById(R.id.btnSave);
        btnSelectDate = findViewById(R.id.btnSelectDate);

        // Set default date to today
        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        editServiceDate.setText(selectedDate);

        // Set up date picker
        setupDatePicker();

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

        String date = selectedDate;

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
        if (chkAlignment.isChecked())
            items.add(new MaintenanceItem((int) sessionId, "Wheel Alignment", odometer, calculateNextDue("wheel alignment", odometer), 0, date));
        if (chkThrottleBody.isChecked())
            items.add(new MaintenanceItem((int) sessionId, "Throttle Body Cleaning", odometer, calculateNextDue("throttle body cleaning", odometer), 0, date));
        if (chkEngineAirFilter.isChecked())
            items.add(new MaintenanceItem((int) sessionId, "Engine Air Filter", odometer, calculateNextDue("engine air filter", odometer), 0, date));
        if (chkCabinAirFilter.isChecked())
            items.add(new MaintenanceItem((int) sessionId, "Cabin Air Filter", odometer, calculateNextDue("cabin air filter", odometer), 0, date));
        if (chkBrakePads.isChecked())
            items.add(new MaintenanceItem((int) sessionId, "Brake Pads", odometer, calculateNextDue("brake pads", odometer), 0, date));

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
            case "brake pads": return currentOdo + 40000;
            case "fuel filter": return currentOdo + 100000;
            case "wheel alignment": return currentOdo + 10000;
            case "throttle body cleaning": return currentOdo + 15000;
            case "engine air filter": return currentOdo + 10000;
            case "cabin air filter": return currentOdo + 10000;
            default: return currentOdo + 10000;
        }
    }

    private void setupDatePicker() {
        // Set up click listeners for date selection
        editServiceDate.setOnClickListener(v -> showDatePicker());
        btnSelectDate.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        // Parse current selected date or use today
        String[] dateParts = selectedDate.split("-");
        int year = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]) - 1; // Month is 0-based
        int day = Integer.parseInt(dateParts[2]);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", 
                            selectedYear, selectedMonth + 1, selectedDay);
                    editServiceDate.setText(selectedDate);
                },
                year, month, day
        );

        datePickerDialog.show();
    }
}
