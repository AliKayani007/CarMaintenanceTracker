package com.example.carmaintenance;

import android.app.DatePickerDialog;
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

public class EditMaintenanceActivity extends AppCompatActivity {

    private EditText editOdometer, editCost, editNotes, editServiceDate;
    private CheckBox chkOil, chkCoolant, chkCVT, chkPlugs, chkBrakes, chkFuelFilter;
    private CheckBox chkAlignment, chkThrottleBody, chkEngineAirFilter, chkCabinAirFilter, chkBrakePads;
    private Button btnSave, btnSelectDate;
    private AppDatabase db;
    private String selectedDate;
    private int sessionId;
    private MaintenanceSession originalSession;
    private List<MaintenanceItem> originalItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_maintenance);

        // Get session ID from intent
        sessionId = getIntent().getIntExtra("sessionId", -1);
        if (sessionId == -1) {
            Toast.makeText(this, "Invalid session", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = AppDatabase.getInstance(this);

        // Initialize views
        initializeViews();

        // Load existing data
        loadExistingData();

        // Set up date picker
        setupDatePicker();

        btnSave.setOnClickListener(v -> saveMaintenance());
    }

    private void initializeViews() {
        editOdometer = findViewById(R.id.editOdometer);
        editCost = findViewById(R.id.editCost);
        editNotes = findViewById(R.id.editNotes);
        editServiceDate = findViewById(R.id.editServiceDate);

        // Maintenance checkboxes
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
    }

    private void loadExistingData() {
        // Get session data
        List<MaintenanceSession> sessions = db.maintenanceDao().getAllSessions();
        for (MaintenanceSession session : sessions) {
            if (session.id == sessionId) {
                originalSession = session;
                break;
            }
        }

        if (originalSession == null) {
            Toast.makeText(this, "Session not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get maintenance items
        originalItems = db.maintenanceDao().getItemsForSession(sessionId);

        // Populate form with existing data
        editOdometer.setText(String.valueOf(originalSession.odometer));
        editCost.setText(String.valueOf(originalSession.totalCost));
        editNotes.setText(originalSession.notes != null ? originalSession.notes : "");
        
        selectedDate = originalSession.date;
        editServiceDate.setText(selectedDate);

        // Check the maintenance items that were performed
        for (MaintenanceItem item : originalItems) {
            switch (item.itemName.toLowerCase()) {
                case "engine oil":
                    chkOil.setChecked(true);
                    break;
                case "coolant":
                    chkCoolant.setChecked(true);
                    break;
                case "cvt fluid":
                    chkCVT.setChecked(true);
                    break;
                case "spark plugs":
                    chkPlugs.setChecked(true);
                    break;
                case "brake service":
                    chkBrakes.setChecked(true);
                    break;
                case "fuel filter":
                    chkFuelFilter.setChecked(true);
                    break;
                case "wheel alignment":
                    chkAlignment.setChecked(true);
                    break;
                case "throttle body cleaning":
                    chkThrottleBody.setChecked(true);
                    break;
                case "engine air filter":
                    chkEngineAirFilter.setChecked(true);
                    break;
                case "cabin air filter":
                    chkCabinAirFilter.setChecked(true);
                    break;
                case "brake pads":
                    chkBrakePads.setChecked(true);
                    break;
            }
        }
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

        // Delete existing items first
        db.maintenanceDao().deleteSession(sessionId);

        // Create new session with same ID
        MaintenanceSession session = new MaintenanceSession(selectedDate, odometer, totalCost, notes);
        session.id = sessionId; // Keep the same ID
        long newSessionId = db.maintenanceDao().insertSession(session);

        // Insert new items
        List<MaintenanceItem> items = new ArrayList<>();

        if (chkOil.isChecked())
            items.add(new MaintenanceItem((int) newSessionId, "Engine Oil", odometer, calculateNextDue("engine oil", odometer), 0, selectedDate));
        if (chkCoolant.isChecked())
            items.add(new MaintenanceItem((int) newSessionId, "Coolant", odometer, calculateNextDue("coolant", odometer), 0, selectedDate));
        if (chkCVT.isChecked())
            items.add(new MaintenanceItem((int) newSessionId, "CVT Fluid", odometer, calculateNextDue("cvt fluid", odometer), 0, selectedDate));
        if (chkPlugs.isChecked())
            items.add(new MaintenanceItem((int) newSessionId, "Spark Plugs", odometer, calculateNextDue("spark plugs", odometer), 0, selectedDate));
        if (chkBrakes.isChecked())
            items.add(new MaintenanceItem((int) newSessionId, "Brake Service", odometer, calculateNextDue("brake service", odometer), 0, selectedDate));
        if (chkFuelFilter.isChecked())
            items.add(new MaintenanceItem((int) newSessionId, "Fuel Filter", odometer, calculateNextDue("fuel filter", odometer), 0, selectedDate));
        if (chkAlignment.isChecked())
            items.add(new MaintenanceItem((int) newSessionId, "Wheel Alignment", odometer, calculateNextDue("wheel alignment", odometer), 0, selectedDate));
        if (chkThrottleBody.isChecked())
            items.add(new MaintenanceItem((int) newSessionId, "Throttle Body Cleaning", odometer, calculateNextDue("throttle body cleaning", odometer), 0, selectedDate));
        if (chkEngineAirFilter.isChecked())
            items.add(new MaintenanceItem((int) newSessionId, "Engine Air Filter", odometer, calculateNextDue("engine air filter", odometer), 0, selectedDate));
        if (chkCabinAirFilter.isChecked())
            items.add(new MaintenanceItem((int) newSessionId, "Cabin Air Filter", odometer, calculateNextDue("cabin air filter", odometer), 0, selectedDate));
        if (chkBrakePads.isChecked())
            items.add(new MaintenanceItem((int) newSessionId, "Brake Pads", odometer, calculateNextDue("brake pads", odometer), 0, selectedDate));

        if (!items.isEmpty()) {
            db.maintenanceDao().insertItems(items);
            Toast.makeText(this, "Maintenance updated successfully", Toast.LENGTH_SHORT).show();
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
