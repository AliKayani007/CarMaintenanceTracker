package com.example.carmaintenance.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carmaintenance.R;
import com.example.carmaintenance.utils.ImageManager;

import java.util.List;

public class ImageDisplayAdapter extends RecyclerView.Adapter<ImageDisplayAdapter.ImageViewHolder> {
    
    private List<String> imagePaths;
    private ImageManager imageManager;
    private OnImageClickListener clickListener;

    public interface OnImageClickListener {
        void onImageClick(int position, String imagePath);
    }

    public ImageDisplayAdapter(ImageManager imageManager, OnImageClickListener clickListener) {
        this.imageManager = imageManager;
        this.clickListener = clickListener;
    }

    public void setImagePaths(List<String> imagePaths) {
        this.imagePaths = imagePaths;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_display, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imagePath = imagePaths.get(position);
        android.util.Log.d("ImageDisplayAdapter", "Binding image at position " + position + ": " + imagePath);
        
        // Load thumbnail
        Bitmap thumbnail = imageManager.createThumbnail(imagePath, 300);
        if (thumbnail != null) {
            holder.imageView.setImageBitmap(thumbnail);
            android.util.Log.d("ImageDisplayAdapter", "Thumbnail loaded successfully");
        } else {
            android.util.Log.e("ImageDisplayAdapter", "Failed to load thumbnail for: " + imagePath);
        }
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            android.util.Log.d("ImageDisplayAdapter", "Image clicked at position " + position);
            if (clickListener != null) {
                android.util.Log.d("ImageDisplayAdapter", "Calling click listener with path: " + imagePath);
                clickListener.onImageClick(position, imagePath);
            } else {
                android.util.Log.e("ImageDisplayAdapter", "Click listener is null!");
            }
        });
        
        // Also set click listener on the image view itself
        holder.imageView.setOnClickListener(v -> {
            android.util.Log.d("ImageDisplayAdapter", "ImageView clicked at position " + position);
            if (clickListener != null) {
                android.util.Log.d("ImageDisplayAdapter", "Calling click listener with path: " + imagePath);
                clickListener.onImageClick(position, imagePath);
            } else {
                android.util.Log.e("ImageDisplayAdapter", "Click listener is null!");
            }
        });
    }

    @Override
    public int getItemCount() {
        return imagePaths != null ? imagePaths.size() : 0;
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewDisplay);
        }
    }
}
