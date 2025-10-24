package com.example.carmaintenance.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carmaintenance.R;
import com.example.carmaintenance.models.MaintenanceItem;
import com.example.carmaintenance.models.MaintenanceSession;

import java.util.List;

public class MaintenanceAdapter extends RecyclerView.Adapter<MaintenanceAdapter.ViewHolder> {

    private List<?> maintenanceList;
    private boolean isUpcoming;

    public MaintenanceAdapter(List<?> maintenanceList, boolean isUpcoming) {
        this.maintenanceList = maintenanceList;
        this.isUpcoming = isUpcoming;
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
        }
    }

    @Override
    public int getItemCount() {
        return maintenanceList != null ? maintenanceList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvDetails, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvType);
            tvDetails = itemView.findViewById(R.id.tvDetails);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }
}
