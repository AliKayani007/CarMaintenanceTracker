package com.example.carmaintenance.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "todo_maintenance")
public class TodoMaintenance {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String itemName;
    public int nextDue;
    public int odometerDone;
    public boolean isCompleted;
    public String notes;

    public TodoMaintenance() {}

    public TodoMaintenance(String itemName, int nextDue, int odometerDone, boolean isCompleted, String notes) {
        this.itemName = itemName;
        this.nextDue = nextDue;
        this.odometerDone = odometerDone;
        this.isCompleted = isCompleted;
        this.notes = notes;
    }
}

