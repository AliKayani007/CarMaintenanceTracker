package com.example.carmaintenance.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carmaintenance.R;
import com.example.carmaintenance.models.MaintenanceItem;

import java.util.List;

public class MaintenanceItemAdapter extends RecyclerView.Adapter<MaintenanceItemAdapter.ViewHolder> {

    private List<MaintenanceItem> maintenanceItems;

    public MaintenanceItemAdapter(List<MaintenanceItem> maintenanceItems) {
        this.maintenanceItems = maintenanceItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_maintenance_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MaintenanceItem item = maintenanceItems.get(position);
        
        holder.tvItemName.setText(item.itemName);
        holder.tvOdometerDone.setText("Done at: " + item.odometerDone + " km");
        holder.tvNextDue.setText("Next due: " + item.nextDue + " km");
        holder.tvDate.setText("Date: " + item.date);
    }

    @Override
    public int getItemCount() {
        return maintenanceItems != null ? maintenanceItems.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemName, tvOdometerDone, tvNextDue, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvOdometerDone = itemView.findViewById(R.id.tvOdometerDone);
            tvNextDue = itemView.findViewById(R.id.tvNextDue);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
