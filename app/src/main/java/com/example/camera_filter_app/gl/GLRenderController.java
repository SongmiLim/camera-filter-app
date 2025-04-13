package com.example.camera_filter_app.gl;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.example.camera_filter_app.model.FilterType;

public class GLRenderController {
    private final GLRenderer renderer;

    public GLRenderController(Context context) {
        renderer = new GLRenderer(context);
    }

    public void updateFilter(FilterType type) {
        renderer.updateFragmentShader(type);
    }

    public GLSurfaceView.Renderer getRenderer() {
        return renderer;
    }
}
