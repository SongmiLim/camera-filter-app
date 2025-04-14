package com.example.camera_filter_app.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.*;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;

import java.util.Collections;

public class CameraHandler {
    private final Context context;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;

    public CameraHandler(Context context) {
        this.context = context;
    }

    public void startCamera(SurfaceTexture surfaceTexture) {
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = manager.getCameraIdList()[0]; // 후면 카메라
            manager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    createSession(surfaceTexture);
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    camera.close();
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    camera.close();
                    Log.e("CameraHandler", "Camera error: " + error);
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void createSession(SurfaceTexture surfaceTexture) {
        try {
            surfaceTexture.setDefaultBufferSize(1280, 720); // 해상도 고정
            Surface surface = new Surface(surfaceTexture);
            CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            builder.addTarget(surface);

            cameraDevice.createCaptureSession(
                    Collections.singletonList(surface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            captureSession = session;
                            try {
                                session.setRepeatingRequest(builder.build(), null, null);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Log.e("CameraHandler", "Session config failed");
                        }
                    },
                    null
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void stopCamera() {
        if (captureSession != null) {
            captureSession.close();
            captureSession = null;
        }
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }
}
