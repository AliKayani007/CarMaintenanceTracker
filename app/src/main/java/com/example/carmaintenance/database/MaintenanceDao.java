package com.example.carmaintenance.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.carmaintenance.models.MaintenanceItem;
import com.example.carmaintenance.models.MaintenanceSession;

import java.util.List;

@Dao
public interface MaintenanceDao {

    // Insert a new maintenance session (returns generated ID)
    @Insert
    long insertSession(MaintenanceSession session);

    // Insert multiple maintenance items linked to a session
    @Insert
    void insertItems(List<MaintenanceItem> items);

    // Get all sessions (most recent first)
    @Query("SELECT * FROM maintenance_sessions ORDER BY date DESC")
    List<MaintenanceSession> getAllSessions();

    // Get all maintenance items for a specific session
    @Query("SELECT * FROM maintenance_items WHERE sessionId = :sessionId ORDER BY id DESC")
    List<MaintenanceItem> getItemsForSession(int sessionId);

    // Get upcoming maintenance items (those that are due *after* current odometer)
    @Query("SELECT * FROM maintenance_items WHERE nextDue > :currentOdo ORDER BY nextDue ASC")
    List<MaintenanceItem> getUpcoming(int currentOdo);

    // Get overdue maintenance items (useful for dashboard or alerts)
    @Query("SELECT * FROM maintenance_items WHERE nextDue <= :currentOdo ORDER BY nextDue ASC")
    List<MaintenanceItem> getOverdue(int currentOdo);

    // Delete all sessions (will also cascade-delete items)
    @Query("DELETE FROM maintenance_sessions")
    void deleteAllSessions();

    // Delete all items (standalone)
    @Query("DELETE FROM maintenance_items")
    void deleteAllItems();

    // Combined clear function
    @Transaction
    default void clearAll() {
        deleteAllItems();
        deleteAllSessions();
    }

    // Optional: get all maintenance items across sessions (history)
    @Query("SELECT * FROM maintenance_items ORDER BY odometerDone DESC")
    List<MaintenanceItem> getAllItems();
}
