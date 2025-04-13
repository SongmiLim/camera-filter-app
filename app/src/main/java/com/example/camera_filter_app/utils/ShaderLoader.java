package com.example.camera_filter_app.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ShaderLoader {
    public static String loadFromAssets(Context context, String filename) {
        StringBuilder shaderCode = new StringBuilder();
        try (InputStream is = context.getAssets().open(filename);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            String line;
            while ((line = reader.readLine()) != null) {
                shaderCode.append(line).append('\n');
            }
        } catch (IOException e) {
            Log.e("ShaderLoader", "Failed to load shader: " + filename, e);
            return "";
        }
        return shaderCode.toString();
    }
}