package com.example.carmaintenance.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carmaintenance.R;
import com.example.carmaintenance.utils.ImageManager;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    
    private List<String> imagePaths;
    private ImageManager imageManager;
    private OnImageDeleteListener deleteListener;

    public interface OnImageDeleteListener {
        void onImageDelete(int position);
    }

    public ImageAdapter(ImageManager imageManager, OnImageDeleteListener deleteListener) {
        this.imagePaths = new ArrayList<>();
        this.imageManager = imageManager;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_preview, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imagePath = imagePaths.get(position);
        
        // Load thumbnail
        Bitmap thumbnail = imageManager.createThumbnail(imagePath, 200);
        if (thumbnail != null) {
            holder.imageView.setImageBitmap(thumbnail);
        }
        
        // Set file size
        long fileSize = imageManager.getImageFileSize(imagePath);
        holder.textViewSize.setText(imageManager.formatFileSize(fileSize));
        
        // Set delete button click listener
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onImageDelete(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    public void addImage(String imagePath) {
        imagePaths.add(imagePath);
        notifyItemInserted(imagePaths.size() - 1);
    }

    public void removeImage(int position) {
        if (position >= 0 && position < imagePaths.size()) {
            imagePaths.remove(position);
            notifyItemRemoved(position);
        }
    }

    public List<String> getImagePaths() {
        return new ArrayList<>(imagePaths);
    }

    public void clearImages() {
        imagePaths.clear();
        notifyDataSetChanged();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textViewSize;
        ImageView btnDelete;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewThumbnail);
            textViewSize = itemView.findViewById(R.id.textViewFileSize);
            btnDelete = itemView.findViewById(R.id.btnDeleteImage);
        }
    }
}
