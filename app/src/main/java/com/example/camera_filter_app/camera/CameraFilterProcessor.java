package com.example.camera_filter_app.camera;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.example.camera_filter_app.gl.CameraRenderer;
import com.example.camera_filter_app.model.FilterType;

public class CameraFilterProcessor {
    private final CameraRenderer renderer;
    private final CameraHandler handler;

    public CameraFilterProcessor(Context context, GLSurfaceView glSurfaceView) {
        renderer = new CameraRenderer(context);
        handler = new CameraHandler(context);

        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setRenderer(renderer);
        glSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    public void init() {
        handler.openCamera(renderer.getSurface());
    }

    public void setFilter(FilterType filterType) {
        renderer.updateFilter(filterType);
    }

    public void setOnRendererReadyCallback(CameraRenderer.OnRendererReadyCallback callback) {
        renderer.setOnRendererReadyCallback(callback);
    }

    public void release() {
        handler.closeCamera();
        renderer.release();
    }
}