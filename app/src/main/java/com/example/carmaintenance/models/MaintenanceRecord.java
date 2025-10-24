package com.example.carmaintenance.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "maintenance_records")
public class MaintenanceRecord {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String type;          // e.g., Engine Oil, Air Filter, etc.
    private String date;          // e.g., "2025-10-24"
    private int odometer;         // e.g., 33700
    private int nextDueKm;        // e.g., 38700
    private String notes;         // optional remarks

    public MaintenanceRecord(String type, String date, int odometer, int nextDueKm, String notes) {
        this.type = type;
        this.date = date;
        this.odometer = odometer;
        this.nextDueKm = nextDueKm;
        this.notes = notes;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getOdometer() { return odometer; }
    public void setOdometer(int odometer) { this.odometer = odometer; }

    public int getNextDueKm() { return nextDueKm; }
    public void setNextDueKm(int nextDueKm) { this.nextDueKm = nextDueKm; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
