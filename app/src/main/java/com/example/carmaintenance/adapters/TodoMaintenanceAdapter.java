package com.example.carmaintenance.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carmaintenance.MainActivity;
import com.example.carmaintenance.R;
import com.example.carmaintenance.database.AppDatabase;
import com.example.carmaintenance.models.TodoMaintenance;

import java.util.List;

public class TodoMaintenanceAdapter extends RecyclerView.Adapter<TodoMaintenanceAdapter.ViewHolder> {

    private List<TodoMaintenance> todoList;
    private Context context;
    private AppDatabase db;

    public TodoMaintenanceAdapter(List<TodoMaintenance> todoList, Context context) {
        this.todoList = todoList;
        this.context = context;
        this.db = AppDatabase.getInstance(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_todo_maintenance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TodoMaintenance todo = todoList.get(position);
        
        holder.tvItemName.setText(todo.itemName);
        holder.tvNextDue.setText("Next due: " + todo.nextDue + " km");
        
        // Remove previous listeners to avoid triggering during binding
        holder.checkBoxCompleted.setOnCheckedChangeListener(null);
        holder.checkBoxCompleted.setChecked(todo.isCompleted);
        
        // Update completion status when checkbox is clicked
        holder.checkBoxCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            db.maintenanceDao().updateTodoStatus(todo.id, isChecked);
            // Refresh the main activity
            if (context instanceof MainActivity) {
                ((MainActivity) context).loadData();
            }
        });
        
        // Delete button
        holder.btnDelete.setOnClickListener(v -> showDeleteDialog(todo));
    }

    @Override
    public int getItemCount() {
        return todoList != null ? todoList.size() : 0;
    }

    private void showDeleteDialog(TodoMaintenance todo) {
        new AlertDialog.Builder(context)
                .setTitle("Delete To-Do")
                .setMessage("Are you sure you want to delete this to-do maintenance?\n\n" +
                        "Item: " + todo.itemName + "\n" +
                        "Next due: " + todo.nextDue + " km")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.maintenanceDao().deleteTodo(todo.id);
                    
                    // Refresh the main activity
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).loadData();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName, tvNextDue;
        CheckBox checkBoxCompleted;
        Button btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvNextDue = itemView.findViewById(R.id.tvNextDue);
            checkBoxCompleted = itemView.findViewById(R.id.checkBoxCompleted);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}

