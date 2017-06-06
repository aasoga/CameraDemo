package com.example.camerademo.magic;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

import com.example.camerademo.R;
import com.example.camerademo.opengles.CameraManager;

/**
 * Created by v_yanligang on 2017/6/2.
 */

public class TakePhoto2Activity extends Activity implements View.OnClickListener{
    private static final int PERMISSION_CAMERA_REQUEST = 1;
    private MagicCameraView mGlsv;
    private MagicEngine magicEngine;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPermission();
        setContentView(R.layout.activity_takephoto2);
        mGlsv = (MagicCameraView) findViewById(R.id.glsv);
        findViewById(R.id.bt_beauty).setOnClickListener(this);
        findViewById(R.id.bt_cool).setOnClickListener(this);
        MagicEngine.Builder builder = new MagicEngine.Builder();
        magicEngine = builder
                .build(mGlsv);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGlsv.bringToFront();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGlsv.onPause();
    }

    private void getPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_CAMERA_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "获得权限", Toast.LENGTH_SHORT).show();
                CameraManager.getInstance().openCamera();
            } else {
                Toast.makeText(this, "权限被禁止", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_beauty:
//                mGlsv.setFilter(new CameraFilter(getApplicationContext()));
                break;
            case R.id.bt_cool:
                mGlsv.setFilter(MagicFilterType.COOL);
                break;
        }
    }
}
