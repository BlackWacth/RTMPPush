package com.hzw.rtmp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.pm.PackageInfoCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hzw.rtmp.live.LivePusher;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {


    private static final int PERMISSION_CAMERA_AUDIO = 0x123;
    private LivePusher mLivePusher;

    public static final String PATH = "rtmp://www.huazw.xyz/myapp/hzw";

    public static final String[] PERMISSION = new String[]{
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };
    private List<String> mPermissionList = new ArrayList<>();
    private SurfaceView mSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSurfaceView = findViewById(R.id.sv_surface_view);
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        Log.i("hzw", "初始化宽高 width = " + width + ", height = " + height);
        mLivePusher = new LivePusher(this, width, height, 800_000, 30, Camera.CameraInfo.CAMERA_FACING_FRONT);
        mLivePusher.setPreviewDisplay(mSurfaceView.getHolder());

        checkPermission();
    }

    private void checkPermission() {
        mPermissionList.clear();
        for (int i = 0; i < PERMISSION.length; i++) {
            if (ActivityCompat.checkSelfPermission(this, PERMISSION[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(PERMISSION[i]);
            }
        }

        if (mPermissionList != null && !mPermissionList.isEmpty()) {
            String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_CAMERA_AUDIO);
        }
    }

    public void onStartLive(View view) {
        showToast(((Button) view).getText().toString());
        mLivePusher.startLive(PATH);

    }

    public void onStopLive(View view) {
        showToast(((Button) view).getText().toString());
        mLivePusher.stopLive();
    }

    public void onSwitchCamera(View view) {
        showToast(((Button) view).getText().toString());
        mLivePusher.switchCamera();
    }

    public void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CAMERA_AUDIO) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i("hzw",permissions[0]);
                mLivePusher.setPreviewDisplay(mSurfaceView.getHolder());
            }
        }

    }
}
