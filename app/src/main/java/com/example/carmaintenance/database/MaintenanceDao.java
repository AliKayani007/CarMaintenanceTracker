package com.example.carmaintenance.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.carmaintenance.models.MaintenanceItem;
import com.example.carmaintenance.models.MaintenanceSession;
import com.example.carmaintenance.models.MaintenanceImage;
import com.example.carmaintenance.models.TodoMaintenance;

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

    // Image-related methods
    // Insert a maintenance image
    @Insert
    long insertImage(MaintenanceImage image);

    // Insert multiple maintenance images
    @Insert
    void insertImages(List<MaintenanceImage> images);

    // Get all images for a specific session
    @Query("SELECT * FROM maintenance_images WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    List<MaintenanceImage> getImagesForSession(int sessionId);

    // Delete a specific image
    @Query("DELETE FROM maintenance_images WHERE id = :imageId")
    void deleteImage(int imageId);

    // Delete all images for a session
    @Query("DELETE FROM maintenance_images WHERE sessionId = :sessionId")
    void deleteImagesForSession(int sessionId);

    // Get image count for a session
    @Query("SELECT COUNT(*) FROM maintenance_images WHERE sessionId = :sessionId")
    int getImageCountForSession(int sessionId);

    // Todo Maintenance methods
    // Insert a new todo maintenance
    @Insert
    long insertTodo(TodoMaintenance todo);

    // Get all todo maintenances
    @Query("SELECT * FROM todo_maintenance ORDER BY nextDue ASC")
    List<TodoMaintenance> getAllTodos();

    // Get incomplete todo maintenances
    @Query("SELECT * FROM todo_maintenance WHERE isCompleted = 0 ORDER BY nextDue ASC")
    List<TodoMaintenance> getIncompleteTodos();

    // Update todo completion status
    @Query("UPDATE todo_maintenance SET isCompleted = :isCompleted WHERE id = :id")
    void updateTodoStatus(int id, boolean isCompleted);

    // Delete a todo
    @Query("DELETE FROM todo_maintenance WHERE id = :id")
    void deleteTodo(int id);

    // Delete all todos
    @Query("DELETE FROM todo_maintenance")
    void deleteAllTodos();
}
