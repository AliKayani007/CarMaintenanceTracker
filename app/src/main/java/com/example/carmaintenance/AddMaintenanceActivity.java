package com.example.carmaintenance;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carmaintenance.adapters.ImageAdapter;
import com.example.carmaintenance.database.AppDatabase;
import com.example.carmaintenance.models.MaintenanceItem;
import com.example.carmaintenance.models.MaintenanceSession;
import com.example.carmaintenance.models.MaintenanceImage;
import com.example.carmaintenance.utils.ImageManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.io.File;
import java.io.IOException;

public class AddMaintenanceActivity extends AppCompatActivity {

    private EditText editOdometer, editCost, editNotes, editServiceDate;
    private CheckBox chkOil, chkCoolant, chkCVT, chkPlugs, chkBrakes, chkFuelFilter;
    private CheckBox chkAlignment, chkThrottleBody, chkEngineAirFilter, chkCabinAirFilter, chkBrakePads;
    private Button btnSave, btnSelectDate, btnAddImage, btnTakePhoto;
    private TextView tvImageCount;
    private RecyclerView recyclerViewImages;
    private AppDatabase db;
    private String selectedDate;
    private ImageManager imageManager;
    private ImageAdapter imageAdapter;
    private List<String> selectedImagePaths;
    
    // Activity result launchers
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<String[]> permissionLauncher;
    private Runnable pendingAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_maintenance);

        db = AppDatabase.getInstance(this);
        imageManager = new ImageManager(this);
        selectedImagePaths = new ArrayList<>();

        initializeViews();
        setupImageRecyclerView();
        setupActivityResultLaunchers();
        setupDatePicker();

        // Set default date to today
        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        editServiceDate.setText(selectedDate);

        btnSave.setOnClickListener(v -> saveMaintenance());
        btnAddImage.setOnClickListener(v -> openGallery());
        btnTakePhoto.setOnClickListener(v -> takePhoto());
    }

    private void initializeViews() {
        editOdometer = findViewById(R.id.editOdometer);
        editCost = findViewById(R.id.editCost);
        editNotes = findViewById(R.id.editNotes);
        editServiceDate = findViewById(R.id.editServiceDate);

        // Updated maintenance checkboxes
        chkOil = findViewById(R.id.chkOil);
        chkCoolant = findViewById(R.id.chkCoolant);
        chkCVT = findViewById(R.id.chkCVT);
        chkPlugs = findViewById(R.id.chkPlugs);
        chkBrakes = findViewById(R.id.chkBrakes);
        chkFuelFilter = findViewById(R.id.chkFuelFilter);
        chkAlignment = findViewById(R.id.chkAlignment);
        chkThrottleBody = findViewById(R.id.chkThrottleBody);
        chkEngineAirFilter = findViewById(R.id.chkEngineAirFilter);
        chkCabinAirFilter = findViewById(R.id.chkCabinAirFilter);
        chkBrakePads = findViewById(R.id.chkBrakePads);

        btnSave = findViewById(R.id.btnSave);
        btnSelectDate = findViewById(R.id.btnSelectDate);
        btnAddImage = findViewById(R.id.btnAddImage);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        tvImageCount = findViewById(R.id.tvImageCount);
        recyclerViewImages = findViewById(R.id.recyclerViewImages);
    }

    private void setupImageRecyclerView() {
        imageAdapter = new ImageAdapter(imageManager, position -> {
            selectedImagePaths.remove(position);
            imageAdapter.removeImage(position);
            updateImageCount();
        });
        
        recyclerViewImages.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerViewImages.setAdapter(imageAdapter);
        updateImageCount();
    }

    private void setupActivityResultLaunchers() {
        // Gallery launcher
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            addImageFromUri(imageUri);
                        }
                    }
                }
        );

        // Camera launcher
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            addImageFromUri(imageUri);
                        }
                    }
                }
        );

        // Permission launcher
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    boolean allGranted = true;
                    for (String permission : permissions.keySet()) {
                        if (!permissions.get(permission)) {
                            allGranted = false;
                            break;
                        }
                    }
                    if (allGranted) {
                        // Permissions granted, proceed with the pending action
                        if (pendingAction != null) {
                            pendingAction.run();
                            pendingAction = null;
                        }
                    } else {
                        Toast.makeText(this, "Permissions required for image access", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void saveMaintenance() {
        String odoStr = editOdometer.getText().toString().trim();
        String costStr = editCost.getText().toString().trim();

        if (odoStr.isEmpty() || costStr.isEmpty()) {
            Toast.makeText(this, "Please enter odometer and cost", Toast.LENGTH_SHORT).show();
            return;
        }

        int odometer = Integer.parseInt(odoStr);
        double totalCost = Double.parseDouble(costStr);
        String notes = editNotes.getText().toString();

        String date = selectedDate;

        // Insert Session
        MaintenanceSession session = new MaintenanceSession(date, odometer, totalCost, notes);
        long sessionId = db.maintenanceDao().insertSession(session);

        // Insert Items
        List<MaintenanceItem> items = new ArrayList<>();

        if (chkOil.isChecked())
            items.add(new MaintenanceItem((int) sessionId, "Engine Oil", odometer, calculateNextDue("engine oil", odometer), 0, date));
        if (chkCoolant.isChecked())
            items.add(new MaintenanceItem((int) sessionId, "Coolant", odometer, calculateNextDue("coolant", odometer), 0, date));
        if (chkCVT.isChecked())
            items.add(new MaintenanceItem((int) sessionId, "CVT Fluid", odometer, calculateNextDue("cvt fluid", odometer), 0, date));
        if (chkPlugs.isChecked())
            items.add(new MaintenanceItem((int) sessionId, "Spark Plugs", odometer, calculateNextDue("spark plugs", odometer), 0, date));
        if (chkBrakes.isChecked())
            items.add(new MaintenanceItem((int) sessionId, "Brake Service", odometer, calculateNextDue("brake service", odometer), 0, date));
        if (chkFuelFilter.isChecked())
            items.add(new MaintenanceItem((int) sessionId, "Fuel Filter", odometer, calculateNextDue("fuel filter", odometer), 0, date));
        if (chkAlignment.isChecked())
            items.add(new MaintenanceItem((int) sessionId, "Wheel Alignment", odometer, calculateNextDue("wheel alignment", odometer), 0, date));
        if (chkThrottleBody.isChecked())
            items.add(new MaintenanceItem((int) sessionId, "Throttle Body Cleaning", odometer, calculateNextDue("throttle body cleaning", odometer), 0, date));
        if (chkEngineAirFilter.isChecked())
            items.add(new MaintenanceItem((int) sessionId, "Engine Air Filter", odometer, calculateNextDue("engine air filter", odometer), 0, date));
        if (chkCabinAirFilter.isChecked())
            items.add(new MaintenanceItem((int) sessionId, "Cabin Air Filter", odometer, calculateNextDue("cabin air filter", odometer), 0, date));
        if (chkBrakePads.isChecked())
            items.add(new MaintenanceItem((int) sessionId, "Brake Pads", odometer, calculateNextDue("brake pads", odometer), 0, date));

        if (!items.isEmpty()) {
            db.maintenanceDao().insertItems(items);
            
            // Save images if any
            if (!selectedImagePaths.isEmpty()) {
                saveImages((int) sessionId);
            }
            
            Toast.makeText(this, "Maintenance saved successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Please select at least one item", Toast.LENGTH_SHORT).show();
        }
    }

    private int calculateNextDue(String itemName, int currentOdo) {
        switch (itemName.toLowerCase()) {
            case "engine oil": return currentOdo + 5000;
            case "coolant":
            case "radiator coolant": return currentOdo + 60000;
            case "cvt fluid": return currentOdo + 60000;
            case "spark plugs": return currentOdo + 30000;
            case "brake service": return currentOdo + 10000;
            case "brake pads": return currentOdo + 40000;
            case "fuel filter": return currentOdo + 100000;
            case "wheel alignment": return currentOdo + 10000;
            case "throttle body cleaning": return currentOdo + 15000;
            case "engine air filter": return currentOdo + 10000;
            case "cabin air filter": return currentOdo + 10000;
            default: return currentOdo + 10000;
        }
    }

    private void setupDatePicker() {
        // Set up click listeners for date selection
        editServiceDate.setOnClickListener(v -> showDatePicker());
        btnSelectDate.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        // Parse current selected date or use today
        String[] dateParts = selectedDate.split("-");
        int year = Integer.parseInt(dateParts[0]);
        int month = Integer.parseInt(dateParts[1]) - 1; // Month is 0-based
        int day = Integer.parseInt(dateParts[2]);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", 
                            selectedYear, selectedMonth + 1, selectedDay);
                    editServiceDate.setText(selectedDate);
                },
                year, month, day
        );

        datePickerDialog.show();
    }

    private void openGallery() {
        if (checkPermissions()) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            galleryLauncher.launch(intent);
        } else {
            pendingAction = () -> {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                galleryLauncher.launch(intent);
            };
        }
    }

    private void takePhoto() {
        if (checkPermissions()) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(intent);
        } else {
            pendingAction = () -> {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraLauncher.launch(intent);
            };
        }
    }

    private boolean checkPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();
        
        // Check camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.CAMERA);
        }
        
        // Check storage permissions based on Android version
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ uses READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            // Android 12 and below use READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
        
        if (!permissionsToRequest.isEmpty()) {
            Toast.makeText(this, "Requesting permissions: " + permissionsToRequest.toString(), Toast.LENGTH_LONG).show();
            permissionLauncher.launch(permissionsToRequest.toArray(new String[0]));
            return false;
        }
        
        Toast.makeText(this, "All permissions granted!", Toast.LENGTH_SHORT).show();
        return true;
    }

    private void addImageFromUri(Uri imageUri) {
        try {
            // Generate a temporary session ID for file naming (will be updated when session is saved)
            int tempSessionId = (int) System.currentTimeMillis();
            String imagePath = imageManager.saveImageFromUri(imageUri, tempSessionId);
            
            selectedImagePaths.add(imagePath);
            imageAdapter.addImage(imagePath);
            updateImageCount();
            
            Toast.makeText(this, "Image added successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error saving image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImages(int sessionId) {
        List<MaintenanceImage> images = new ArrayList<>();
        
        for (String imagePath : selectedImagePaths) {
            // Update the image path with the correct session ID
            String newImagePath = updateImagePathForSession(imagePath, sessionId);
            
            MaintenanceImage image = new MaintenanceImage(
                sessionId,
                newImagePath,
                "", // No description for now
                System.currentTimeMillis()
            );
            images.add(image);
        }
        
        if (!images.isEmpty()) {
            db.maintenanceDao().insertImages(images);
        }
    }

    private String updateImagePathForSession(String oldPath, int sessionId) {
        try {
            File oldFile = new File(oldPath);
            File newFile = new File(oldFile.getParent(), imageManager.generateImageFileName(sessionId));
            
            if (oldFile.renameTo(newFile)) {
                return newFile.getAbsolutePath();
            }
        } catch (Exception e) {
            // If rename fails, return original path
        }
        
        return oldPath;
    }

    private void updateImageCount() {
        int count = selectedImagePaths.size();
        if (count == 0) {
            tvImageCount.setText("No images selected");
        } else if (count == 1) {
            tvImageCount.setText("1 image selected");
        } else {
            tvImageCount.setText(count + " images selected");
        }
    }
}
