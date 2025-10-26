package com.example.carmaintenance;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.carmaintenance.utils.ImageManager;
import com.example.carmaintenance.views.ZoomableImageView;

public class ImageViewerActivity extends AppCompatActivity {

    private ZoomableImageView imageView;
    private ImageView btnClose;
    private ImageManager imageManager;
    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        android.util.Log.d("ImageViewerActivity", "Activity created");
        
        imagePath = getIntent().getStringExtra("imagePath");
        android.util.Log.d("ImageViewerActivity", "Received image path: " + imagePath);
        
        if (imagePath == null) {
            Toast.makeText(this, "No image path provided", Toast.LENGTH_SHORT).show();
            android.util.Log.e("ImageViewerActivity", "No image path provided!");
            finish();
            return;
        }

        imageManager = new ImageManager(this);
        imageView = findViewById(R.id.imageViewFull);
        btnClose = findViewById(R.id.btnClose);

        // Load and display the image
        loadImage();

        // Set click listeners - using a flag to prevent zoom issues
        setupClickListener();
    }
    
    private void setupClickListener() {
        final long[] lastClickTime = {0};
        imageView.setOnClickListener(v -> {
            // Only close if not zooming and after a short delay
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastClickTime[0] > 300) {
                lastClickTime[0] = currentTime;
                finish();
            }
        });
        btnClose.setOnClickListener(v -> finish());
    }

    private void loadImage() {
        if (imageManager.imageExists(imagePath)) {
            Bitmap bitmap = imageManager.loadImageFromPath(imagePath);
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Image file not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
