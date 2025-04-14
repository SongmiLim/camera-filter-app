package com.example.camera_filter_app.ui.filter;

import android.Manifest;
import android.content.pm.PackageManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.camera_filter_app.R;
import com.example.camera_filter_app.camera.CameraHandler;
import com.example.camera_filter_app.gl.CameraRenderer;
import com.example.camera_filter_app.model.FilterType;
import com.example.camera_filter_app.viewmodel.FilterViewModel;

public class FilterActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA_PERMISSION = 1001;

    private GLSurfaceView glSurfaceView;
    private TextView filterStatus;
    private FilterViewModel viewModel;

    private CameraRenderer cameraRenderer;
    private CameraHandler cameraHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        if (!hasCameraPermission()) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION
            );
            return;
        }

        initViews();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initViews();
            } else {
                finish();
            }
        }
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void initViews() {
        glSurfaceView = findViewById(R.id.glSurfaceView);
        glSurfaceView.setEGLContextClientVersion(2);

        cameraRenderer = new CameraRenderer(this);
        glSurfaceView.setRenderer(cameraRenderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        cameraHandler = new CameraHandler(this);
        cameraRenderer.setSurfaceTextureListener(surfaceTexture -> {
            runOnUiThread(() -> {
                cameraHandler.startCamera(surfaceTexture);
            });
        });

        filterStatus = findViewById(R.id.txtFilterStatus);
        viewModel = new ViewModelProvider(this).get(FilterViewModel.class);

        viewModel.getSelectedFilter().observe(this, filter -> {
            filterStatus.setText("Filter: " + filter.name());
        });

        findViewById(R.id.btnOriginal).setOnClickListener(v -> {
            viewModel.setFilter(FilterType.GRAYSCALE);
            cameraRenderer.updateFilter(FilterType.ORIGINAL);
        });

        findViewById(R.id.btnGrayScale).setOnClickListener(v -> {
            viewModel.setFilter(FilterType.GRAYSCALE);
            cameraRenderer.updateFilter(FilterType.GRAYSCALE);

        });

        findViewById(R.id.btnBrightness).setOnClickListener(v -> {
            viewModel.setFilter(FilterType.BRIGHTNESS);
            cameraRenderer.updateFilter(FilterType.BRIGHTNESS);
        });
    }

        @Override
        protected void onResume() {
            super.onResume();
            glSurfaceView.onResume();
        }

        @Override
        protected void onPause() {
            super.onPause();
            cameraHandler.stopCamera();
            glSurfaceView.onPause();
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            cameraRenderer.release();
        }
    }
