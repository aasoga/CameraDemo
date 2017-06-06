package com.example.camerademo.opengles.filter;

import android.content.Context;
import android.opengl.GLES20;

import com.example.camerademo.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static com.example.camerademo.opengles.filter.CameraFilter.LEVEL;

/**
 * Created by v_yanligang on 2017/5/19.
 */

public class BeautyFilter extends BaseFilter {
    private int mSingleStepOffsetLocation;
    private int mParamsLocation;
    public BeautyFilter(Context context) {
        super(BaseFilter.VERTEX_SHADERCODE, readShaderFromRaw(context, R.raw.beauty));
    }

    private static String readShaderFromRaw(Context context, int resourceId) {
        InputStream inputStream = context.getResources().openRawResource(resourceId);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String nextLine;
        StringBuilder sb = new StringBuilder();
        try {
            while ((nextLine = bufferedReader.readLine()) != null) {
                sb.append(nextLine);
                sb.append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public void init() {
        super.init();
        mSingleStepOffsetLocation = GLES20.glGetUniformLocation(mProgram, "singleStepOffset");
        mParamsLocation = GLES20.glGetUniformLocation(mProgram, "params");
        setBeautyLevel(LEVEL);
    }


    public void setBeautyLevel(int level){
        switch (level) {
            case 0:
                setFloat(mParamsLocation, 0.0f);
                break;
            case 1:
                setFloat(mParamsLocation, 1.0f);
                break;
            case 2:
                setFloat(mParamsLocation, 0.8f);
                break;
            case 3:
                setFloat(mParamsLocation,0.6f);
                break;
            case 4:
                setFloat(mParamsLocation, 0.4f);
                break;
            case 5:
                setFloat(mParamsLocation,0.33f);
                break;
            default:
                break;
        }
    }

    public void onInputSizeChanged(final int width, final int height) {
        super.onInputSizeChanged(width, height);
        setTexelSize(width, height);
    }

    private void setTexelSize(final float w, final float h) {
        setFloatVec2(mSingleStepOffsetLocation, new float[] {2.0f / w, 2.0f / h});
    }

    protected void setFloat(final int location, final float floatValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform1f(location, floatValue);
            }
        });
    }
}
