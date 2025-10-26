package com.example.carmaintenance;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carmaintenance.adapters.ImageDisplayAdapter;
import com.example.carmaintenance.adapters.MaintenanceItemAdapter;
import com.example.carmaintenance.database.AppDatabase;
import com.example.carmaintenance.models.MaintenanceItem;
import com.example.carmaintenance.models.MaintenanceSession;
import com.example.carmaintenance.models.MaintenanceImage;
import com.example.carmaintenance.utils.ImageManager;

import java.util.ArrayList;
import java.util.List;

public class MaintenanceDetailActivity extends AppCompatActivity {

    private TextView tvServiceDate, tvOdometer, tvTotalCost, tvNotes, tvImagesTitle, tvNoImages;
    private RecyclerView recyclerItems, recyclerViewImages;
    private AppDatabase db;
    private int sessionId;
    private ImageManager imageManager;
    private ImageDisplayAdapter imageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance_detail);

        // Get session ID from intent
        sessionId = getIntent().getIntExtra("sessionId", -1);
        if (sessionId == -1) {
            finish();
            return;
        }

        db = AppDatabase.getInstance(this);
        imageManager = new ImageManager(this);

        // Initialize views
        tvServiceDate = findViewById(R.id.tvServiceDate);
        tvOdometer = findViewById(R.id.tvOdometer);
        tvTotalCost = findViewById(R.id.tvTotalCost);
        tvNotes = findViewById(R.id.tvNotes);
        tvImagesTitle = findViewById(R.id.tvImagesTitle);
        tvNoImages = findViewById(R.id.tvNoImages);
        recyclerItems = findViewById(R.id.recyclerItems);
        recyclerViewImages = findViewById(R.id.recyclerViewImages);

        // Setup RecyclerViews
        recyclerItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewImages.setLayoutManager(new GridLayoutManager(this, 2));

        // Setup image adapter
        imageAdapter = new ImageDisplayAdapter(imageManager, (position, imagePath) -> {
            android.util.Log.d("MaintenanceDetail", "Image clicked! Position: " + position + ", Path: " + imagePath);
            // Open full-screen image viewer
            Intent intent = new Intent(this, ImageViewerActivity.class);
            intent.putExtra("imagePath", imagePath);
            android.util.Log.d("MaintenanceDetail", "Starting ImageViewerActivity with path: " + imagePath);
            startActivity(intent);
        });
        recyclerViewImages.setAdapter(imageAdapter);

        // Load and display data
        loadMaintenanceDetails();
    }

    private void loadMaintenanceDetails() {
        // Get session details
        List<MaintenanceSession> sessions = db.maintenanceDao().getAllSessions();
        MaintenanceSession session = null;
        for (MaintenanceSession s : sessions) {
            if (s.id == sessionId) {
                session = s;
                break;
            }
        }

        if (session != null) {
            // Display session info
            tvServiceDate.setText("Service Date: " + session.date);
            tvOdometer.setText("Odometer: " + session.odometer + " km");
            tvTotalCost.setText("Total Cost: Rs " + session.totalCost);
            tvNotes.setText(session.notes != null && !session.notes.isEmpty() ? 
                "Notes: " + session.notes : "No notes");

            // Get and display maintenance items
            List<MaintenanceItem> items = db.maintenanceDao().getItemsForSession(sessionId);
            MaintenanceItemAdapter adapter = new MaintenanceItemAdapter(items);
            recyclerItems.setAdapter(adapter);

            // Load and display images
            loadImages();
        }
    }

    private void loadImages() {
        List<MaintenanceImage> images = db.maintenanceDao().getImagesForSession(sessionId);
        android.util.Log.d("MaintenanceDetail", "Found " + images.size() + " images for session " + sessionId);
        
        if (images.isEmpty()) {
            tvImagesTitle.setVisibility(View.GONE);
            recyclerViewImages.setVisibility(View.GONE);
            tvNoImages.setVisibility(View.VISIBLE);
        } else {
            tvImagesTitle.setVisibility(View.VISIBLE);
            recyclerViewImages.setVisibility(View.VISIBLE);
            tvNoImages.setVisibility(View.GONE);
            
            List<String> imagePaths = new ArrayList<>();
            for (MaintenanceImage image : images) {
                android.util.Log.d("MaintenanceDetail", "Checking image: " + image.imagePath);
                if (imageManager.imageExists(image.imagePath)) {
                    imagePaths.add(image.imagePath);
                    android.util.Log.d("MaintenanceDetail", "Image exists and added to list");
                } else {
                    android.util.Log.d("MaintenanceDetail", "Image does not exist: " + image.imagePath);
                }
            }
            
            android.util.Log.d("MaintenanceDetail", "Total valid images: " + imagePaths.size());
            imageAdapter.setImagePaths(imagePaths);
        }
    }
}
