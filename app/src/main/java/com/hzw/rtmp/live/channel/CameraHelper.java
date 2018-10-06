package com.hzw.rtmp.live.channel;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class CameraHelper implements Camera.PreviewCallback, SurfaceHolder.Callback {

    public final String TAG = this.getClass().getSimpleName();

    private Activity mActivity;
    private int mHeight;
    private int mWidth;
    private int mCameraId;
    private Camera mCamera;
    private byte[] mBuffer;
    private SurfaceHolder mSurfaceHolder;
    private Camera.PreviewCallback mPreviewCallback;
    private OnChangedSizeListener mOnChangedSizeListener;

    public CameraHelper(Activity activity, int cameraId, int width, int height) {
        mActivity = activity;
        mHeight = height;
        mWidth = width;
        mCameraId = cameraId;
    }

    /**
     * 切换摄像头
     */
    public void switchCamera() {
        if (mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        } else {
            mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        }
        stopPreview();
        startPreview();
    }

    private void startPreview() {
        mCamera = Camera.open(mCameraId);
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewFormat(ImageFormat.NV21);
        setPreviewSize(parameters);
        setPreviewOrientation();
        mCamera.setParameters(parameters);
        mBuffer = new byte[mWidth * mHeight * 3 / 2];
        mCamera.addCallbackBuffer(mBuffer);
        mCamera.setPreviewCallbackWithBuffer(this);
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
            if (mOnChangedSizeListener != null) {
                mOnChangedSizeListener.onChanged(mWidth, mHeight);
            }
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setPreviewOrientation() {
        int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;

            case Surface.ROTATION_90: //横屏，左边是头部（home键在右边）
                degrees = 90;
                break;

            case Surface.ROTATION_180:
                degrees = 180;
                break;

            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraId, cameraInfo);
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (cameraInfo.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (cameraInfo.orientation - degrees + 360) % 360;
        }
        Log.i(TAG, "成像调整角度 result = " + result);
        mCamera.setDisplayOrientation(result);
    }

    private void setPreviewSize(Camera.Parameters parameters) {
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        Camera.Size size = supportedPreviewSizes.get(0);
        int m = Math.abs(size.height * size.width - mWidth * mHeight);
        supportedPreviewSizes.remove(0);
        Iterator<Camera.Size> iterator = supportedPreviewSizes.iterator();
        while (iterator.hasNext()) {
            Camera.Size next = iterator.next();
            int n = Math.abs(next.width * next.height - mWidth * mHeight);
            if (n < m) {
                m = n;
                size = next;
            }
        }
        mWidth = size.width;
        mHeight = size.height;
        Log.i(TAG, "最终宽高 width = " + mWidth + ", height = " + mHeight);
        parameters.setPreviewSize(mWidth, mHeight);
    }

    private void stopPreview() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (mPreviewCallback != null) {
            mPreviewCallback.onPreviewFrame(data, camera);
            camera.addCallbackBuffer(mBuffer);
        }
    }

    public void setPreviewDisplay(SurfaceHolder surfaceHolder) {
        mSurfaceHolder = surfaceHolder;
        mSurfaceHolder.addCallback(this);
    }

    public void setPreviewCallback(Camera.PreviewCallback previewCallback) {
        mPreviewCallback = previewCallback;
    }

    public void setOnChangedSizeListener(OnChangedSizeListener onChangedSizeListener) {
        mOnChangedSizeListener = onChangedSizeListener;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        stopPreview();
        startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public interface OnChangedSizeListener {
        void onChanged(int w, int h);
    }

}
