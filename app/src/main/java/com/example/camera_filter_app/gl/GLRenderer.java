package com.example.camera_filter_app.gl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.example.camera_filter_app.model.FilterType;
import com.example.camera_filter_app.utils.ShaderLoader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class GLRenderer implements GLSurfaceView.Renderer {
    private FloatBuffer vertexBuffer;
    private FloatBuffer texCoordBuffer;
    private int program = 0;

    private final Context context;
    private static final String DEFAULT_VERTEX_SHADER_PATH = "shaders/default.vs";
    private static final String DEFAULT_FRAGMENT_SHADER_PATH = "shaders/original.fs";

    private final float[] squareCoords = {
            -1f, 1f,
            -1f, -1f,
            1f, 1f,
            1f, -1f
    };

    private final float[] texCoords = {
            0f, 0f,
            0f, 1f,
            1f, 0f,
            1f, 1f
    };

    public GLRenderer(Context context) {
        this.context = context;
        initBuffers();
    }

    @Override
    public void onSurfaceCreated(javax.microedition.khronos.opengles.GL10 gl, javax.microedition.khronos.egl.EGLConfig config) {
        setShaderProgram(DEFAULT_VERTEX_SHADER_PATH, DEFAULT_FRAGMENT_SHADER_PATH);
    }

    @Override
    public void onSurfaceChanged(javax.microedition.khronos.opengles.GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(javax.microedition.khronos.opengles.GL10 gl) {
        GLES20.glClearColor(0f, 0f, 0f, 1f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(this.program);

        int posHandle = GLES20.glGetAttribLocation(this.program, "vPosition");
        GLES20.glEnableVertexAttribArray(posHandle);
        GLES20.glVertexAttribPointer(posHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        int texHandle = GLES20.glGetAttribLocation(this.program, "aTexCoord");
        GLES20.glEnableVertexAttribArray(texHandle);
        GLES20.glVertexAttribPointer(texHandle, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        GLES20.glDisableVertexAttribArray(posHandle);
        GLES20.glDisableVertexAttribArray(texHandle);
    }

    private void initBuffers() {
        vertexBuffer = ByteBuffer.allocateDirect(squareCoords.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        vertexBuffer.put(squareCoords).position(0);

        texCoordBuffer = ByteBuffer.allocateDirect(texCoords.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        texCoordBuffer.put(texCoords).position(0);
    }

    public void updateFragmentShader(FilterType type) {
        String fragFile;
        switch (type) {
            case GRAYSCALE:
                fragFile = "shaders/grayscale.fs";
                break;
            case BRIGHTNESS:
                fragFile = "shaders/brightness.fs";
                break;
            case ORIGINAL:
                fragFile = "shaders/original.fs";
                break;
            default:
                fragFile = "shaders/original.fs";
                break;
        };
        setShaderProgram(DEFAULT_VERTEX_SHADER_PATH, fragFile);
    }

    private void setShaderProgram(String vertexShaderPath, String fragmentShaderPath) {
        String vertexCode = ShaderLoader.loadFromAssets(context, vertexShaderPath);
        String fragmentCode = ShaderLoader.loadFromAssets(context, fragmentShaderPath);

        int vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexCode);
        int fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentCode);

        if (vertexShader == 0 || fragmentShader == 0) {
            Log.e("GLRenderer", "Shader compilation failed. Aborting shader program setup.");
            return;
        }

        int shaderProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(shaderProgram, vertexShader);
        GLES20.glAttachShader(shaderProgram, fragmentShader);
        GLES20.glLinkProgram(shaderProgram);

        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(shaderProgram, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == 0) {
            Log.e("GLRenderer", "Program link failed: " + GLES20.glGetProgramInfoLog(shaderProgram));
            GLES20.glDeleteProgram(shaderProgram);
            return;
        }

        this.program = shaderProgram;
        Log.d("GLRenderer", "Shader program successfully created and linked.");
    }

    private int compileShader(int type, String code) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);
        return shader;
    }
}
