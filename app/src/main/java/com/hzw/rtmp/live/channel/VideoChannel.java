package com.hzw.rtmp.live.channel;

import android.app.Activity;
import android.hardware.Camera;
import android.view.SurfaceHolder;

import com.hzw.rtmp.live.LivePusher;

public class VideoChannel implements Camera.PreviewCallback, CameraHelper.OnChangedSizeListener {

    private LivePusher mLivePusher;
    private CameraHelper mCameraHelper;
    private int mBitrate;
    private int mFps;
    private boolean isLiving;

    public VideoChannel(Activity activity, LivePusher livePusher, int cameraId, int width, int height, int bitrate, int fps) {
        mLivePusher = livePusher;
        mBitrate = bitrate;
        mFps = fps;
        mCameraHelper = new CameraHelper(activity, cameraId, width, height);
        mCameraHelper.setPreviewCallback(this);
        mCameraHelper.setOnChangedSizeListener(this);
    }

    public void setPreviewDisplay(SurfaceHolder surfaceHolder) {
        mCameraHelper.setPreviewDisplay(surfaceHolder);
    }

    public void switchCamera() {
        mCameraHelper.switchCamera();
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (isLiving) {
            mLivePusher.native_pushVideo(data);
        }
    }

    @Override
    public void onChanged(int w, int h) {
        mLivePusher.native_setVideoEncodeInfo(w, h, mFps, mBitrate);
    }

    public void startLive() {
        isLiving = true;
    }

    public void stopLive() {
        isLiving = false;
    }

}
