package com.example.camerademo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.example.camerademo.magic.TakePhoto2Activity;
import com.example.camerademo.opengles.TakePhoto1Activity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.bt_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, TakePhotoActivity.class));
            }
        });
        findViewById(R.id.bt_select).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SelectPicActivity.class);
                intent.putExtra(SelectPicActivity.EXTRA_TYPE, SelectPicActivity.EXTRA_NATIVE);
                startActivity(intent);
            }
        });
        findViewById(R.id.bt_camera1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, TakePhoto1Activity.class));
            }
        });
        findViewById(R.id.bt_camera2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, TakePhoto2Activity.class));
            }
        });
    }
}
