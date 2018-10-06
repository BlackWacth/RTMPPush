package com.hzw.rtmp.live;

import android.app.Activity;
import android.view.SurfaceHolder;

import com.hzw.rtmp.live.channel.AudioChannel;
import com.hzw.rtmp.live.channel.VideoChannel;

public class LivePusher {

    static {
        System.loadLibrary("native-lib");
    }

    private VideoChannel mVideoChannel;
    private AudioChannel mAudioChannel;


    public LivePusher(Activity activity, int width, int height, int bitrate, int fps, int cameraId) {
        native_init();
        mVideoChannel = new VideoChannel(activity, this, cameraId, width, height, bitrate, fps);
        mAudioChannel = new AudioChannel();
    }

    public void switchCamera() {
        mVideoChannel.switchCamera();
    }

    public void startLive(String path) {
        native_start(path);
        mVideoChannel.startLive();
        mAudioChannel.startLive();
    }

    public void stopLive() {
        mVideoChannel.stopLive();
        mAudioChannel.stopLive();
        native_stop();
    }

    public void setPreviewDisplay(SurfaceHolder holder) {
        mVideoChannel.setPreviewDisplay(holder);
    }

    public native void native_init();

    public native void native_start(String path);

    public native void native_setVideoEncodeInfo(int w, int h, int fps, int bitrate);

    public native void native_pushVideo(byte[] data);

    public native void native_stop();

    public native void native_release();
}
