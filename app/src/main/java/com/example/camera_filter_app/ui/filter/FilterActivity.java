package com.example.camera_filter_app.ui.filter;

import android.os.Bundle;
import android.opengl.GLSurfaceView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.camera_filter_app.R;
import com.example.camera_filter_app.gl.GLRenderController;
import com.example.camera_filter_app.model.FilterType;
import com.example.camera_filter_app.viewmodel.FilterViewModel;

public class FilterActivity extends AppCompatActivity {
    private TextView filterStatus;
    private FilterViewModel viewModel;
    private GLRenderController renderController;
    private GLSurfaceView glSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        filterStatus = findViewById(R.id.txtFilterStatus);

        glSurfaceView = findViewById(R.id.camera_view);
        glSurfaceView.setEGLContextClientVersion(2);

        renderController = new GLRenderController(getApplicationContext());
        glSurfaceView.setRenderer(renderController.getRenderer());
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        glSurfaceView.requestRender();

        viewModel = new ViewModelProvider(this).get(FilterViewModel.class);
        viewModel.getSelectedFilter().observe(this, filter -> {
            filterStatus.setText("current filter: " + filter.name());
            glSurfaceView.queueEvent(() -> renderController.updateFilter(filter));
            glSurfaceView.requestRender();
        });

        findViewById(R.id.btnOriginal).setOnClickListener(v -> viewModel.setFilter(FilterType.ORIGINAL));
        findViewById(R.id.btnGrayScale).setOnClickListener(v -> viewModel.setFilter(FilterType.GRAYSCALE));
        findViewById(R.id.btnBrightness).setOnClickListener(v -> viewModel.setFilter(FilterType.BRIGHTNESS));
    }

    @Override
    protected void onResume() {
        super.onResume();
        glSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        glSurfaceView.onPause();
    }
}
