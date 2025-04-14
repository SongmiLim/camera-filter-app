package com.example.camera_filter_app.gl;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.example.camera_filter_app.model.FilterType;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraRenderer implements GLSurfaceView.Renderer {
    public interface SurfaceTextureListener {
        void onSurfaceTextureCreated(SurfaceTexture surfaceTexture);
    }

    private final Context context;
    private int oesTextureId;
    private SurfaceTexture surfaceTexture;
    private SurfaceTextureListener listener;
    private ShaderProgram shaderProgram;

    private FilterType currentFilter = FilterType.ORIGINAL;
    private FilterType pendingFilter = FilterType.ORIGINAL;

    private FloatBuffer vertexBuffer;
    private FloatBuffer texCoordBuffer;

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

    public void setSurfaceTextureListener(SurfaceTextureListener listener) {
        this.listener = listener;
    }

    public void updateFilter(FilterType filter) {
        this.pendingFilter = filter;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        oesTextureId = createOESTexture();
        surfaceTexture = new SurfaceTexture(oesTextureId);

        shaderProgram = new ShaderProgram(context, currentFilter);

        if (listener != null) {
            listener.onSurfaceTextureCreated(surfaceTexture);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (pendingFilter != currentFilter) {
            currentFilter = pendingFilter;
            shaderProgram.release(); // 이전 셰이더 정리
            shaderProgram = new ShaderProgram(context, currentFilter);
        }

        surfaceTexture.updateTexImage();

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        shaderProgram.use();

        int posHandle = GLES20.glGetAttribLocation(shaderProgram.getProgramId(), "vPosition");
        GLES20.glEnableVertexAttribArray(posHandle);
        GLES20.glVertexAttribPointer(posHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        int texHandle = GLES20.glGetAttribLocation(shaderProgram.getProgramId(), "aTexCoord");
        GLES20.glEnableVertexAttribArray(texHandle);
        GLES20.glVertexAttribPointer(texHandle, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, oesTextureId);
        GLES20.glUniform1i(GLES20.glGetUniformLocation(shaderProgram.getProgramId(), "uTexture"), 0);

        shaderProgram.draw();

        GLES20.glDisableVertexAttribArray(posHandle);
        GLES20.glDisableVertexAttribArray(texHandle);
    }

    public void release() {
        shaderProgram.release();
        surfaceTexture.release();
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
}