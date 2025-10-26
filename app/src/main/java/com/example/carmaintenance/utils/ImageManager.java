package com.example.carmaintenance.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageManager {
    private static final String TAG = "ImageManager";
    private static final String IMAGE_DIRECTORY = "maintenance_images";
    private static final int MAX_IMAGE_SIZE = 1920; // Max width/height for compression
    private static final int COMPRESSION_QUALITY = 85;

    private Context context;

    public ImageManager(Context context) {
        this.context = context;
    }

    /**
     * Creates the images directory if it doesn't exist
     */
    public File createImageDirectory() {
        File imagesDir = new File(context.getFilesDir(), IMAGE_DIRECTORY);
        if (!imagesDir.exists()) {
            imagesDir.mkdirs();
        }
        return imagesDir;
    }

    /**
     * Generates a unique filename for the image
     */
    public String generateImageFileName(int sessionId) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return String.format("session_%d_img_%s.jpg", sessionId, timestamp);
    }

    /**
     * Saves an image from URI to internal storage
     */
    public String saveImageFromUri(Uri imageUri, int sessionId) throws IOException {
        File imagesDir = createImageDirectory();
        String fileName = generateImageFileName(sessionId);
        File imageFile = new File(imagesDir, fileName);

        // Get input stream from URI
        InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
        if (inputStream == null) {
            throw new IOException("Could not open input stream for image");
        }

        // Decode and compress the image
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        inputStream.close();

        if (bitmap == null) {
            throw new IOException("Could not decode image");
        }

        // Compress and save the image
        Bitmap compressedBitmap = compressImage(bitmap);
        FileOutputStream outputStream = new FileOutputStream(imageFile);
        compressedBitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESSION_QUALITY, outputStream);
        outputStream.close();

        Log.d(TAG, "Image saved: " + imageFile.getAbsolutePath());
        return imageFile.getAbsolutePath();
    }

    /**
     * Compresses image to reduce file size
     */
    private Bitmap compressImage(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // Calculate scaling factor
        float scale = Math.min((float) MAX_IMAGE_SIZE / width, (float) MAX_IMAGE_SIZE / height);
        
        if (scale < 1) {
            int newWidth = Math.round(width * scale);
            int newHeight = Math.round(height * scale);
            return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        }
        
        return bitmap;
    }

    /**
     * Loads a bitmap from file path
     */
    public Bitmap loadImageFromPath(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                Log.d(TAG, "Loading image from: " + imagePath);
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                if (bitmap != null) {
                    Log.d(TAG, "Image loaded successfully. Size: " + bitmap.getWidth() + "x" + bitmap.getHeight());
                } else {
                    Log.e(TAG, "Failed to decode image: " + imagePath);
                }
                return bitmap;
            } else {
                Log.e(TAG, "Image file does not exist: " + imagePath);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading image: " + e.getMessage());
        }
        return null;
    }

    /**
     * Creates a thumbnail of the image
     */
    public Bitmap createThumbnail(String imagePath, int maxSize) {
        Bitmap bitmap = loadImageFromPath(imagePath);
        if (bitmap == null) return null;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float scale = Math.min((float) maxSize / width, (float) maxSize / height);
        
        if (scale < 1) {
            int newWidth = Math.round(width * scale);
            int newHeight = Math.round(height * scale);
            return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        }
        
        return bitmap;
    }

    /**
     * Deletes an image file
     */
    public boolean deleteImage(String imagePath) {
        try {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                boolean deleted = imageFile.delete();
                Log.d(TAG, "Image deleted: " + imagePath + " - " + deleted);
                return deleted;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting image: " + e.getMessage());
        }
        return false;
    }

    /**
     * Gets the file size of an image
     */
    public long getImageFileSize(String imagePath) {
        File imageFile = new File(imagePath);
        return imageFile.exists() ? imageFile.length() : 0;
    }

    /**
     * Formats file size for display
     */
    public String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    /**
     * Checks if image file exists
     */
    public boolean imageExists(String imagePath) {
        return new File(imagePath).exists();
    }
}
