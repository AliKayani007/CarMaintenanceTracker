package com.example.carmaintenance.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "maintenance_images",
        foreignKeys = @ForeignKey(
                entity = MaintenanceSession.class,
                parentColumns = "id",
                childColumns = "sessionId",
                onDelete = ForeignKey.CASCADE
        )
)
public class MaintenanceImage {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int sessionId;
    public String imagePath;
    public String description;
    public long timestamp;

    // Default constructor for Room
    public MaintenanceImage() {}

    public MaintenanceImage(int sessionId, String imagePath, String description, long timestamp) {
        this.sessionId = sessionId;
        this.imagePath = imagePath;
        this.description = description;
        this.timestamp = timestamp;
    }
}
