package com.example.carmaintenance.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carmaintenance.EditMaintenanceActivity;
import com.example.carmaintenance.MainActivity;
import com.example.carmaintenance.MaintenanceDetailActivity;
import com.example.carmaintenance.R;
import com.example.carmaintenance.database.AppDatabase;
import com.example.carmaintenance.models.MaintenanceItem;
import com.example.carmaintenance.models.MaintenanceSession;

import java.util.List;

public class MaintenanceAdapter extends RecyclerView.Adapter<MaintenanceAdapter.ViewHolder> {

    private List<?> maintenanceList;
    private boolean isUpcoming;
    private Context context;
    private AppDatabase db;

    public MaintenanceAdapter(List<?> maintenanceList, boolean isUpcoming, Context context) {
        this.maintenanceList = maintenanceList;
        this.isUpcoming = isUpcoming;
        this.context = context;
        this.db = AppDatabase.getInstance(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_maintenance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (isUpcoming) {
            MaintenanceItem item = (MaintenanceItem) maintenanceList.get(position);
            holder.tvType.setText(item.itemName);
            holder.tvDetails.setText("Next due: " + item.nextDue + " km");
            holder.tvDate.setText("Last done at: " + item.odometerDone + " km");
        } else {
            MaintenanceSession session = (MaintenanceSession) maintenanceList.get(position);
            holder.tvType.setText("Service on " + session.date);
            holder.tvDetails.setText("Odometer: " + session.odometer + " km | Cost: Rs " + session.totalCost);
            holder.tvDate.setText(session.notes != null ? session.notes : "");
            
            // Show action buttons for history items
            holder.btnEdit.setVisibility(View.VISIBLE);
            holder.btnDelete.setVisibility(View.VISIBLE);
            
            // Add click listener for history items (view details)
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, MaintenanceDetailActivity.class);
                intent.putExtra("sessionId", session.id);
                context.startActivity(intent);
            });
            
            // Edit button
            holder.btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(context, EditMaintenanceActivity.class);
                intent.putExtra("sessionId", session.id);
                context.startActivity(intent);
            });
            
            // Delete button
            holder.btnDelete.setOnClickListener(v -> showDeleteDialog(session));
        }
    }

    @Override
    public int getItemCount() {
        return maintenanceList != null ? maintenanceList.size() : 0;
    }

    private void showDeleteDialog(MaintenanceSession session) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Service Record")
                .setMessage("Are you sure you want to delete this service record?\n\n" +
                        "Date: " + session.date + "\n" +
                        "Odometer: " + session.odometer + " km\n" +
                        "Cost: Rs " + session.totalCost)
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.maintenanceDao().deleteSession(session.id);
                    Toast.makeText(context, "Service record deleted", Toast.LENGTH_SHORT).show();
                    
                    // Refresh the main activity
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).loadData();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvDetails, tvDate;
        Button btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvType);
            tvDetails = itemView.findViewById(R.id.tvDetails);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
