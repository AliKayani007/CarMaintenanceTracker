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

    public MaintenanceItem(int sessionId, String itemName, int odometerDone, int nextDue, double cost, String date) {
        this.sessionId = sessionId;
        this.itemName = itemName;
        this.odometerDone = odometerDone;
        this.nextDue = nextDue;
        this.cost = cost;
        this.date = date;
    }
    private int getIntervalKm(String itemName) {
        switch (itemName.toLowerCase()) {
            case "engine oil":
                return 5000;
            case "radiator coolant":
                return 40000;
            case "cvt fluid":
                return 40000;
            case "spark plugs":
                return 30000;
            case "brake service":
                return 20000;
            case "fuel filter":
                return 40000;
            case "air filter":
                return 10000;
            case "ac filter":
                return 15000;
            case "brake fluid":
                return 30000;
            case "alignment":
                return 10000;
            case "battery service":
                return 20000;
            case "timing chain":
                return 80000;
            default:
                return 0;
        }
    }

}
