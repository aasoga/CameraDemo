package com.example.camerademo.opengles;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

import com.example.camerademo.R;
import com.example.camerademo.magic.MagicEngine;
import com.example.camerademo.opengles.filter.BeautyFilter;
import com.example.camerademo.opengles.filter.CameraFilter;
import com.example.camerademo.opengles.filter.CoolFilter;

/**
 * Created by v_yanligang on 2017/5/18.
 */

public class TakePhoto1Activity extends Activity implements View.OnClickListener {
    private static final int PERMISSION_CAMERA_REQUEST = 1;
    private CameraGlSurfaceView mGlsv;
    private MagicEngine magicEngine;
    private int mLevel = 5; // 对话框选项初始值
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPermission();
        setContentView(R.layout.activity_takephoto1);
        mGlsv = (CameraGlSurfaceView) findViewById(R.id.glsv);
        findViewById(R.id.bt_beauty).setOnClickListener(this);
        findViewById(R.id.bt_cool).setOnClickListener(this);
        MagicEngine.Builder builder = new MagicEngine.Builder();
//        magicEngine = builder
//                .build(mGlsv);
        findViewById(R.id.switch_iv).setOnClickListener(this);
        findViewById(R.id.beauty_iv).setOnClickListener(this);
        findViewById(R.id.bt_normal).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        mGlsv.bringToFront();
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
            case R.id.bt_normal:
                mGlsv.setDrawer(new CameraFilter(getApplicationContext()));
            case R.id.bt_beauty:
                mGlsv.setDrawer(new BeautyFilter(getApplicationContext()));
                break;
            case R.id.bt_cool:
                mGlsv.setDrawer(new CoolFilter(mGlsv.getContext()));
                break;
            case R.id.switch_iv:
                mGlsv.switchCamera();
                break;
            case R.id.beauty_iv:
                new AlertDialog.Builder(this).setSingleChoiceItems(new String[]{"关闭", "1", "2", "3", "4", "5"}, mLevel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mLevel = which;
                        mGlsv.onBeautyLevelChanged(which);
                        dialog.dismiss();
                    }
                }).setNegativeButton("取消", null).show();
                break;
        }
    }
}
