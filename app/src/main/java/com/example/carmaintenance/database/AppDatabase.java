package com.example.carmaintenance.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.carmaintenance.models.MaintenanceItem;
import com.example.carmaintenance.models.MaintenanceSession;

@Database(
        entities = {MaintenanceSession.class, MaintenanceItem.class},
        version = 4, // incremented version because maintenance items changed
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public abstract MaintenanceDao maintenanceDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "car_maintenance_db")
                    // Clears & recreates DB on schema change to avoid crash (good for development)
                    .fallbackToDestructiveMigration()
                    // For small apps this is fine, but you can move DB ops off main thread later
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }
}
