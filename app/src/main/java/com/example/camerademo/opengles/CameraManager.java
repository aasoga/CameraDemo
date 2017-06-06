package com.example.camerademo.opengles;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import com.example.camerademo.camera.CameraInfo;

import java.io.IOException;
import java.util.List;

/**
 * Created by v_yanligang on 2017/5/16.
 */

public class CameraManager {
    private static CameraManager mInstance;
    private Camera mCamera;
    private boolean isPreview;
    private static int cameraID = 0;
    private SurfaceTexture mTexture;

    public static CameraManager getInstance() {
        if (mInstance == null) {
            mInstance = new CameraManager();
        }
        return mInstance;
    }

    public void openCamera() {
        if (mCamera == null) {
            try {
                mCamera = Camera.open(cameraID);
                setParameters();
            }catch (Exception e) {

            }
        }
    }

    public void openCamera(int cameraId) {
        if (mCamera == null) {
            try {
                mCamera = Camera.open(cameraId);
                setParameters();
            }catch (Exception e) {

            }
        }
    }

    public void switchCamera() {
        stopCamera();
        cameraID = cameraID == 0 ? 1: 0;
        openCamera(cameraID);
        startPreview(mTexture);
    }
    public Camera getCamera() {
        return mCamera;
    }

    public void startPreview(SurfaceTexture mTexture) {
        this.mTexture = mTexture;
        if (mCamera != null && !isPreview) {
            try {
                mCamera.setPreviewTexture(mTexture);
                mCamera.startPreview();
                isPreview = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopPreview() {
        if (mCamera != null && isPreview) {
            mCamera.stopPreview();
            isPreview = false;
        }
    }

    public void stopCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public boolean isPrewviewing() {
        return isPreview;
    }

    public CameraInfo getCameraInfo() {
        if(mCamera != null) {
            Camera.Size size = mCamera.getParameters().getPreviewSize();
            CameraInfo cameraInfo = new CameraInfo();
            cameraInfo.previewWidth = size.width;
            cameraInfo.previewHeight = size.height;
            Camera.CameraInfo cameraInfo1 = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraID, cameraInfo1);
            cameraInfo.orientation = cameraInfo1.orientation;

            Camera.Size pictureSize = mCamera.getParameters().getPictureSize();
            cameraInfo.pictureWidth = pictureSize.width;
            cameraInfo.pictureHeight = pictureSize.height;
            return cameraInfo;
        }
        return null;
    }
    private void setParameters() {
        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        parameters.setPreviewSize(sizes.get(0).width, sizes.get(0).height);
        List<Camera.Size> pictureSizes = parameters.getSupportedPictureSizes();
        parameters.setPictureSize(pictureSizes.get(0).width, pictureSizes.get(0).height);
        parameters.setRotation(90);
        mCamera.setParameters(parameters);
    }


}
