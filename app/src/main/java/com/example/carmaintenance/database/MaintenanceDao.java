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

    // Alias for getItemsForSession (for export functionality)
    @Query("SELECT * FROM maintenance_items WHERE sessionId = :sessionId ORDER BY id DESC")
    List<MaintenanceItem> getItemsBySessionId(int sessionId);

    // Get upcoming maintenance items (only the next due date for each maintenance type)
    @Query("SELECT * FROM maintenance_items WHERE nextDue > :currentOdo AND id IN " +
           "(SELECT MAX(id) FROM maintenance_items GROUP BY itemName) ORDER BY nextDue ASC")
    List<MaintenanceItem> getUpcoming(int currentOdo);
    
    // Get the latest maintenance item for each type (for debugging/verification)
    @Query("SELECT * FROM maintenance_items WHERE id IN " +
           "(SELECT MAX(id) FROM maintenance_items GROUP BY itemName) ORDER BY itemName ASC")
    List<MaintenanceItem> getLatestForEachType();

    // Get overdue maintenance items (useful for dashboard or alerts)
    @Query("SELECT * FROM maintenance_items WHERE nextDue <= :currentOdo ORDER BY nextDue ASC")
    List<MaintenanceItem> getOverdue(int currentOdo);

    // Delete a specific session (will also cascade-delete items)
    @Query("DELETE FROM maintenance_sessions WHERE id = :sessionId")
    void deleteSession(int sessionId);

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

    // Dashboard queries
    // Get total spent across all sessions
    @Query("SELECT SUM(totalCost) FROM maintenance_sessions")
    double getTotalSpent();

    // Get the most recent session (for last service info)
    @Query("SELECT * FROM maintenance_sessions ORDER BY date DESC LIMIT 1")
    MaintenanceSession getLastService();

    // Get total number of services
    @Query("SELECT COUNT(*) FROM maintenance_sessions")
    int getTotalServices();

    // Get count of upcoming maintenance items
    @Query("SELECT COUNT(*) FROM maintenance_items WHERE nextDue > :currentOdo AND id IN " +
           "(SELECT MAX(id) FROM maintenance_items GROUP BY itemName)")
    int getUpcomingCount(int currentOdo);

    // Get count of overdue maintenance items
    @Query("SELECT COUNT(*) FROM maintenance_items WHERE nextDue <= :currentOdo")
    int getOverdueCount(int currentOdo);
}
