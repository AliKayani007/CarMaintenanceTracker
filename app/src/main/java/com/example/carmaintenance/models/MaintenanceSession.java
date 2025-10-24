package com.example.carmaintenance.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "maintenance_sessions")
public class MaintenanceSession {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String date;
    public int odometer;
    public double totalCost;
    public String notes;

    // ✅ Optional default constructor (good practice)
    public MaintenanceSession() {}

    // ✅ Main constructor
    public MaintenanceSession(String date, int odometer, double totalCost, String notes) {
        this.date = date;
        this.odometer = odometer;
        this.totalCost = totalCost;
        this.notes = notes;
    }
}
