package com.example.camera_filter_app.gl;

import android.content.Context;
import android.opengl.GLES20;

import com.example.camera_filter_app.model.FilterType;
import com.example.camera_filter_app.utils.ShaderLoader;

public class ShaderProgram {
    private final Context context;
    private int programId;
    private final FilterType filterType;

    public ShaderProgram(Context context, FilterType filterType) {
        this.context = context;
        this.filterType = filterType;
        build();
    }

    private void build() {
        String vertexSrc = ShaderLoader.loadFromAssets(context, "shaders/default.vs");
        String fragmentSrc;
        switch (filterType) {
            case ORIGINAL:
                fragmentSrc = ShaderLoader.loadFromAssets(context, "shaders/original.fs");
                break;
            case GRAYSCALE:
                fragmentSrc = ShaderLoader.loadFromAssets(context, "shaders/grayscale.fs");
                break;
            case BRIGHTNESS:
                fragmentSrc = ShaderLoader.loadFromAssets(context, "shaders/brightness.fs");
                break;
            default:
                fragmentSrc = ShaderLoader.loadFromAssets(context, "shaders/original.fs");
        }

        int vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexSrc);
        int fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentSrc);

        programId = GLES20.glCreateProgram();
        GLES20.glAttachShader(programId, vertexShader);
        GLES20.glAttachShader(programId, fragmentShader);
        GLES20.glLinkProgram(programId);
    }

    private int compileShader(int type, String source) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);

        int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == 0) {
            String error = GLES20.glGetShaderInfoLog(shader);
            GLES20.glDeleteShader(shader);
            throw new RuntimeException("Shader compile failed: " + error);
        }
        return shader;
    }

    public void use() {
        GLES20.glUseProgram(programId);
    }

    public void draw() {
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    public int getProgramId() {
        return programId;
    }

    public void release() {
        if (programId != 0) {
            GLES20.glDeleteProgram(programId);
            programId = 0;
        }
    }
}