package com.example.gearfit_android;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BarcodeScannerActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_REQUEST = 200;
    private static final String TAG = "BarcodeScanner";
    private static final long VALIDATION_DELAY_MS = 1000; // 1 segundo de delay para validar
    private PreviewView previewView;
    private ExecutorService cameraExecutor;
    private BarcodeScanner barcodeScanner;

    private ImageView backButton;
    private Camera camera;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isBarcodeValid = false;
    private String lastBarcodeValue = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);
        setupUI();

        backButton = findViewById(R.id.back_button);
        previewView = findViewById(R.id.camera_view);
        cameraExecutor = Executors.newSingleThreadExecutor();

        // Verificar permisos de cámara
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        });


        // Configurar el escáner de códigos de barras
        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build();
        barcodeScanner = BarcodeScanning.getClient(options);
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Configurar la vista previa
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(cameraExecutor, image -> {
                    scanBarcode(image);
                });

                // Usar CameraSelector.DEFAULT_BACK_CAMERA
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                cameraProvider.unbindAll();
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
            } catch (Exception e) {
                Log.e(TAG, "Error al iniciar la cámara", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void scanBarcode(ImageProxy imageProxy) {
        @SuppressWarnings("UnsafeOptInUsageError")
        android.media.Image mediaImage = imageProxy.getImage();
        if (mediaImage == null) {
            imageProxy.close();
            return;
        }
        InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

        barcodeScanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    if (!barcodes.isEmpty()) {
                        Barcode barcode = barcodes.get(0);
                        String barcodeValue = barcode.getRawValue();

                        if (barcodeValue != null) {
                            validateBarcode(barcodeValue);
                        }
                    }else{
                        isBarcodeValid=false;
                        lastBarcodeValue="";
                        handler.removeCallbacksAndMessages(null);
                    }
                    
                    imageProxy.close();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al escanear código de barras", e);
                    imageProxy.close();
                    isBarcodeValid=false;
                    lastBarcodeValue="";
                    handler.removeCallbacksAndMessages(null);
                });
    }

    private void validateBarcode(String barcodeValue) {
        if (!isBarcodeValid) {
            if (lastBarcodeValue.equals(barcodeValue)) {
                isBarcodeValid = true;
                handler.postDelayed(() -> {
                    if (isBarcodeValid) {
                        fetchFoodDetails(barcodeValue); // Nueva función para obtener datos del alimento
                    }
                }, VALIDATION_DELAY_MS);
            } else {
                lastBarcodeValue = barcodeValue;
                isBarcodeValid = false;
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(() -> {
                    if (isBarcodeValid) {
                        fetchFoodDetails(barcodeValue);
                    }
                }, VALIDATION_DELAY_MS);
            }
        }
    }

    private void fetchFoodDetails(String barcode) {
        new Thread(() -> {
            String url = "https://world.openfoodfacts.org/api/v0/product/" + barcode + ".json";
            try {
                URL apiUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                if (jsonResponse.has("product")) {
                    JSONObject product = jsonResponse.getJSONObject("product");
                    String name = product.optString("product_name", "Desconocido");

                    runOnUiThread(() -> Toast.makeText(BarcodeScannerActivity.this, "Producto: " + name, Toast.LENGTH_LONG).show());

                    // Enviar resultado a la actividad anterior
                    sendResult(barcode);
                } else {
                    runOnUiThread(() -> Toast.makeText(BarcodeScannerActivity.this, "Producto no encontrado", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(BarcodeScannerActivity.this, "Error al obtener datos", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }


    private void sendResult(String barcodeValue) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("barcode", barcodeValue);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        handler.removeCallbacksAndMessages(null);
    }

    // Manejo de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void setupUI() {
        getWindow().setStatusBarColor(Color.parseColor("#7AB8FF"));
        getSupportActionBar().hide();
    }
}