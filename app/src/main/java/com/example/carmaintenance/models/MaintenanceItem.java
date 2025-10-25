package com.example.carmaintenance.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "maintenance_items",
        foreignKeys = @ForeignKey(
                entity = MaintenanceSession.class,
                parentColumns = "id",
                childColumns = "sessionId",
                onDelete = ForeignKey.CASCADE
        )
)
public class MaintenanceItem {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int sessionId;
    public String itemName;
    public int odometerDone;
    public int nextDue;
    public double cost;
    public String date; // ✅ New field

    // Default constructor for Room
    public MaintenanceItem() {}

    public MaintenanceItem(int sessionId, String itemName, int odometerDone, int nextDue, double cost, String date) {
        this.sessionId = sessionId;
        this.itemName = itemName;
        this.odometerDone = odometerDone;
        this.nextDue = nextDue;
        this.cost = cost;
        this.date = date;
    }

}
