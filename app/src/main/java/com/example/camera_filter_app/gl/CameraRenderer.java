package com.example.camera_filter_app.gl;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.view.Surface;

import com.example.camera_filter_app.model.FilterType;
import com.example.camera_filter_app.utils.ShaderLoader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraRenderer implements GLSurfaceView.Renderer {
    private final Context context;
    private FilterType currentFilter = FilterType.ORIGINAL;
    private FilterType pendingFilter = null;

    private FloatBuffer vertexBuffer;
    private FloatBuffer texCoordBuffer;
    private int program;
    private int oesTextureId;
    private SurfaceTexture cameraSurfaceTexture;
    private Surface surface;

    private OnRendererReadyCallback callback;

    private final float[] squareCoords = {
            -1f, 1f, -1f, -1f, 1f, 1f, 1f, -1f
    };

    private final float[] texCoords = {
            0f, 0f, 0f, 1f, 1f, 0f, 1f, 1f
    };

    public CameraRenderer(Context context) {
        this.context = context;
        initBuffers();
    }

    public void updateFilter(FilterType type) {
        pendingFilter = type;
    }

    public Surface getSurface() {
        return surface;
    }

    public void setOnRendererReadyCallback(OnRendererReadyCallback callback) {
        this.callback = callback;
    }

    public void release() {
        if (surface != null) surface.release();
    }

    public interface OnRendererReadyCallback {
        void onRendererReady();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        oesTextureId = createOESTexture();
        cameraSurfaceTexture = new SurfaceTexture(oesTextureId);
        cameraSurfaceTexture.setDefaultBufferSize(1280, 720);

        surface = new Surface(cameraSurfaceTexture);
        setShader(currentFilter);

        if (callback != null) {
            callback.onRendererReady();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (pendingFilter != null && pendingFilter != currentFilter) {
            setShader(pendingFilter);
            currentFilter = pendingFilter;
            pendingFilter = null;
        }

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        cameraSurfaceTexture.updateTexImage();

        GLES20.glUseProgram(program);

        int posHandle = GLES20.glGetAttribLocation(program, "vPosition");
        GLES20.glEnableVertexAttribArray(posHandle);
        GLES20.glVertexAttribPointer(posHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        int texHandle = GLES20.glGetAttribLocation(program, "aTexCoord");
        GLES20.glEnableVertexAttribArray(texHandle);
        GLES20.glVertexAttribPointer(texHandle, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oesTextureId);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(program, "uTexture"), 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(posHandle);
        GLES20.glDisableVertexAttribArray(texHandle);
    }

    private void initBuffers() {
        vertexBuffer = ByteBuffer.allocateDirect(squareCoords.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.put(squareCoords).position(0);

        texCoordBuffer = ByteBuffer.allocateDirect(texCoords.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        texCoordBuffer.put(texCoords).position(0);
    }

    private int createOESTexture() {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        int textureId = textures[0];

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        return textureId;
    }

    private void setShader(FilterType type) {
        String vertex = ShaderLoader.loadFromAssets(context, "shaders/default.vs");
        String fragment;

        switch (type) {
            case GRAYSCALE:
                fragment = ShaderLoader.loadFromAssets(context, "shaders/grayscale.fs");
                break;
            case BRIGHTNESS:
                fragment = ShaderLoader.loadFromAssets(context, "shaders/brightness.fs");
                break;
            default:
                fragment = ShaderLoader.loadFromAssets(context, "shaders/original.fs");
                break;
        }

        int vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertex);
        int fragShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragment);

        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragShader);
        GLES20.glLinkProgram(program);
    }

    private int compileShader(int type, String code) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);
        return shader;
    }
}