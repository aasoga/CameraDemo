package com.example.camerademo.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.camerademo.LogUtils;
import com.example.camerademo.TakePhotoActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by v_yanligang on 2017/3/23.
 */

public class CameraView extends SurfaceView {

    private Camera mCamera;
    private Context mContext;
    private int mOrientation = 0;
    private FocusImageView mFocusImageView; // 聚焦图片
    private OrientationEventListener orientationEventListener;
    private ImageView mWaterIv; // 水印图片
    private ImageView mAnimateIv; // 拍照动画view
    private boolean mIsFrontCamera; // 是否打开前置摄像头，true为前置
    private FlashMode mFlashMode = FlashMode.OFF;
    private MediaRecorder mRecorder;
    private Camera.Parameters mParameters;
    private ExecutorService mExecutor;
    private TakePhotoActivity.OnPictureTakeListener mTakeListener;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                mTakeListener.onPicTakenend();
                mTakeListener = null;
            }

        }
    };

    private String mImageFolder;
    private String mThumbFolder;

    public CameraView(Context context) {
        this(context, null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        getHolder().addCallback(callBack);
        mExecutor = Executors.newSingleThreadExecutor();
        openCamera();
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        break;
                    case MotionEvent.ACTION_UP:
                        Point point = new Point((int) event.getX(), (int) event.getY());
                        onFocus(point);
                        mFocusImageView.startFocus(point);
                        break;
                }
                return true;
            }
        });

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), mContext.getPackageName());
        if (!file.exists()) {
            file.mkdir();
        }
        mImageFolder = file.getAbsolutePath();
        File temp = new File(file, "temp");
        if (!temp.exists()) {
            temp.mkdir();
        }
        mThumbFolder = temp.getAbsolutePath();
    }

    private SurfaceHolder.Callback callBack = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {

            try {
                if (mCamera == null) {
                    boolean isopen = openCamera();
                    if (!isopen) {
                        return;
                    }
                }
                startPreview(holder);
            } catch (Exception e) {
                Toast.makeText(mContext, "打开相机失败", Toast.LENGTH_SHORT).show();
            }


        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                updateOrientation();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            releaseCamera();
        }
    };

    private class FocusCallback implements Camera.AutoFocusCallback {

        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (success) {
                mFocusImageView.focusSuccess();
            } else {
                mFocusImageView.focusFailed();
            }
        }
    }

    public void setmFocusImageView(FocusImageView focusImageView) {
        this.mFocusImageView = focusImageView;
    }

    public void setWaterImage(ImageView mWaterIv) {
        this.mWaterIv = mWaterIv;
    }

    public void setAnimateIv(ImageView mAnimateIv) {
        this.mAnimateIv = mAnimateIv;
    }

    private void onFocus(Point point) {
        if (mCamera == null) {
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        //不支持设置自定义聚焦，则使用自动聚焦，返回
        LogUtils.e("cameraview", "parameters.getMaxNumFocusAreas()" + parameters.getMaxNumFocusAreas());
        if (parameters.getMaxNumFocusAreas() <= 0) {
            mCamera.autoFocus(new FocusCallback());
            return;
        }
        List<Camera.Area> areas = new ArrayList<>();
        int left = point.x - 300;
        int right = point.x + 300;
        int top = point.y + 300;
        int bottom = point.y - 300;
        Rect rect = new Rect(left, top, right, bottom);
        areas.add(new Camera.Area(rect, 100));
        parameters.setFocusAreas(areas);
        try {
            mCamera.setParameters(parameters);
        } catch (Exception e) {
//            Toast.makeText(mContext, "自定义聚焦失败", Toast.LENGTH_SHORT).show();
        }
        mCamera.autoFocus(new FocusCallback());
    }

    public boolean openCamera() {

        if (mCamera != null) {
            releaseCamera();
        }
        if (mIsFrontCamera) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                Camera.getCameraInfo(i, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    try {
                        mCamera = Camera.open(i);
                    } catch (Exception e) {
                        Toast.makeText(mContext, "打开相机失败", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }
            }
        } else {
            try {
                mCamera = Camera.open();
            } catch (Exception e) {
                Toast.makeText(mContext, "打开相机失败", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    public void startPreview(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.startPreview();
            setCameraParameters();
            try {
                mCamera.setPreviewDisplay(holder);
                updateOrientation(); // 校正相机朝向
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void releaseCamera() {
        if (mCamera != null) {
            orientationEventListener.disable();
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private void setCameraParameters() {
        //        setCameraParameters();
        Camera.Parameters mParameters = mCamera.getParameters();
        //设置图片格式
        mParameters.setPictureFormat(ImageFormat.JPEG);
        mParameters.setJpegQuality(100);
        mParameters.setJpegThumbnailQuality(100);
        //自动聚焦模式
//        mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

        Point bestPreviewSize = getBestPreviewSize(mParameters);
        mParameters.setPreviewSize(bestPreviewSize.x, bestPreviewSize.y);
        Point bestPictureSize = getBestPictureSize(mParameters);
        Log.e("photo", "bestPreviewSize" + bestPreviewSize.x + "====" + bestPreviewSize.y);
        mParameters.setPictureSize(bestPictureSize.x, bestPictureSize.y);
        mCamera.setParameters(mParameters);
        setOrientationListener();
    }

    private void setOrientationListener() {
        orientationEventListener = new OrientationEventListener(mContext) {

            @Override
            public void onOrientationChanged(int orientation) {
//                Log.e("photo", "orientation" + orientation);
                if (orientation >= 0 && orientation <= 45 || orientation > 315) {
                    orientation = 0;
                } else if (orientation > 45 && orientation <= 135) {
                    orientation = 90;
                } else if (orientation > 135 && orientation <= 225) {
                    orientation = 180;
                } else if (orientation > 225 && orientation <= 315) {
                    orientation = 270;
                } else {
                    orientation = 0;
                }
                // 如果方向没变化则返回
                if (orientation == mOrientation) {
                    return;
                }
                mOrientation = orientation;
                updateOrientation();

            }
        };
        orientationEventListener.enable();
    }

    private void updateOrientation() {
        if (mCamera != null) {
            //生成的图片需旋转90,360即0度
            Camera.Parameters parameters = mCamera.getParameters();
            int rotation = mOrientation + 90 == 360 ? 0 : mOrientation + 90;
            //前置摄像头需要对垂直方向做变换，否则拍出的照片是颠倒的
            if (mIsFrontCamera) {
                if (rotation == 90) {
                    rotation = 270;
                } else if (rotation == 270) {
                    rotation = 90;
                }
            }
            parameters.setRotation(rotation);
            mCamera.setDisplayOrientation(90);// 预览需要旋转90
            mCamera.setParameters(parameters);
        }
    }

    private Point getBestPreviewSize(Camera.Parameters parameters) {
        // 获取屏幕尺寸
        List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point mSolutionForCamera = new Point(display.getWidth(), display.getHeight());
        // 因为相机旋转了90度，所以需要宽高互换,保证宽大于高
        if (mSolutionForCamera.x < mSolutionForCamera.y) {
            mSolutionForCamera.x = display.getHeight();
            mSolutionForCamera.y = display.getWidth();
        }
        int bestX = 0;
        int bestY = 0;
        int diff = Integer.MAX_VALUE;
        for (Camera.Size size : sizes) {
            int newX = size.width;
            int newY = size.height;
            int newDiff = Math.abs(newX - mSolutionForCamera.x) + Math.abs(newY - mSolutionForCamera.y);
            if (newDiff == 0) {
                bestX = newX;
                bestY = newY;
            } else if (newDiff < diff) {
                bestX = newX;
                bestY = newY;
                diff = newDiff;
            }
        }
        if (bestX > 0 && bestY > 0) {
            return new Point(bestX, bestY);
        }
        return null;
    }


    private Point getBestPictureSize(Camera.Parameters mParameters) {
        // 获取屏幕尺寸
        List<Camera.Size> sizes = mParameters.getSupportedPictureSizes();
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point pictureSolution = new Point();
        Point point = new Point(display.getWidth(), display.getHeight());
        //宽高互换，保证宽大于高
        if (point.x < point.y) {
            pictureSolution.x = point.y;
            pictureSolution.y = point.x;
        }
        int bestX = 0;
        int bestY = 0;
        int diff = Integer.MAX_VALUE;
        for (Camera.Size size : sizes) {
            int newX = size.width;
            int newY = size.height;
            int newDiff = Math.abs(newX - pictureSolution.x) + Math.abs(newY - pictureSolution.y);
            if (newDiff == 0) {
                bestX = newX;
                bestY = newY;
            } else if (newDiff < diff) {
                bestX = newX;
                bestY = newY;
                diff = newDiff;
            }
        }
        if (bestX > 0 && bestY > 0) {
            return new Point(bestX, bestY);
        }
        return pictureSolution;
    }

    public void takePhoto(final TakePhotoActivity.OnPictureTakeListener listener) {
        this.mTakeListener = listener;
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                mTakeListener.onPicTaking();
                if (mExecutor == null || mExecutor.isShutdown()) {
                    mExecutor = Executors.newSingleThreadExecutor();
                }
                mExecutor.execute(new TakephotoRunnable(data));
                // 需要再次开启预览
                camera.startPreview();
            }

        });
    }

    class TakephotoRunnable implements Runnable {

        private byte[] data;
        public TakephotoRunnable(byte[] data) {
            this.data = data;
        }
        @Override
        public void run() {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            save(bitmap);
        }
    }
    public void takeVideo() {
        if (mCamera == null) {
            openCamera();
        }
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
        } else {
            mRecorder.reset();
        }
        mParameters = mCamera.getParameters();
        mCamera.unlock();
        mRecorder.setCamera(mCamera);
        mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        // 设置录像参数
        mRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_720P));
        // 设置输出朝向
        mRecorder.setOrientationHint(90);
        File file1 = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), mContext.getPackageName());
        if (!file1.exists()) {
            file1.mkdir();
        }
        File file = new File(file1, System.currentTimeMillis() + "temp.3gp");
        mRecorder.setOutputFile(file.getAbsolutePath());
        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopVideo() {
        try {
            if (mRecorder != null) {
                mRecorder.stop();
                mRecorder.reset();
                mRecorder.release();
                mRecorder = null;
            }
            if (mCamera != null) {
                mCamera.reconnect();
                mCamera.stopPreview();
                //设置参数为录像前的参数，不然如果录像是低配，结束录制后预览效果还是低配画面
                mCamera.setParameters(mParameters);
                //重新打开
                mCamera.startPreview();
                mParameters = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void save(Bitmap bitmap) {

        Bitmap bitmap1 = null;
        if (mWaterIv.getVisibility() == View.VISIBLE) {
            bitmap1 = ((BitmapDrawable) mWaterIv.getDrawable()).getBitmap();
        }
        Bitmap bitmap2 = watermarkBitmap(bitmap, bitmap1, null);

        // 生成缩略图
        Bitmap thumbnail = ThumbnailUtils.extractThumbnail(bitmap2, 213, 213);

        File file = new File(mImageFolder, System.currentTimeMillis() + ".jpeg");
        File temp = new File(mThumbFolder, System.currentTimeMillis() + "temp.jpeg");
        Log.e("photo", "file" + file.getAbsolutePath());
        try {
            // 存图片大图
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap2.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();

            // 存图片小图
            FileOutputStream tempOutputStream = new FileOutputStream(temp);
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 50, tempOutputStream);
            tempOutputStream.flush();
            tempOutputStream.close();
            mHandler.sendEmptyMessage(0);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private void startPictureAnim(Bitmap bitmap, final TakePhotoActivity1.OnPictureTakeListener listener) {
//        mAnimateIv.setVisibility(View.VISIBLE);
//        mAnimateIv.setImageBitmap(bitmap);
//        Animation animation = AnimationUtils.loadAnimation(mContext, com.example.cameralibrary.R.anim.tempview_show);
//        animation.setDuration(500);
//        mAnimateIv.startAnimation(animation);
//        animation.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                mAnimateIv.setVisibility(View.GONE);
//                listener.onAnimend();
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//        });
//    }

    private Bitmap watermarkBitmap(Bitmap src, Bitmap watermark, String title) {
        int width = src.getWidth();
        int height = src.getHeight();
        Bitmap newB = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newB);
        canvas.drawBitmap(src, 0, 0, null);
        Paint paint = new Paint();
        if (watermark != null) {
            int width1 = watermark.getWidth();
            int height1 = watermark.getHeight();
            paint.setAlpha(50);
            canvas.drawBitmap(watermark, width - width1 + 5, height - height1 + 5, paint);//右下角
        }
        if (title != null) {
            String familyName = "黑体";
            Typeface typeface = Typeface.create(familyName, Typeface.NORMAL);
            TextPaint textPaint = new TextPaint();
            textPaint.setColor(Color.RED);
            textPaint.setTypeface(typeface);
            textPaint.setTextSize(10);
            StaticLayout staticLayout = new StaticLayout(title, textPaint, width, Layout.Alignment.ALIGN_NORMAL, 1.0F, 0.0F, true);
            staticLayout.draw(canvas);
        }
        canvas.save();
        canvas.restore();
        return newB;
    }

    public void switchCamera() {
        mIsFrontCamera = !mIsFrontCamera;
        openCamera();
        setCameraParameters();
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(getHolder());
            } catch (IOException e) {
                e.printStackTrace();
            }
            mCamera.startPreview();
            updateOrientation();
        }
    }

    public FlashMode getFlashMode() {
        return mFlashMode;
    }

    // 设置闪光灯模式
    public void setmFlashMode(FlashMode flashMode) {
        if (mCamera == null) {
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        mFlashMode = flashMode;
        switch (flashMode) {
            case ON:
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                break;
            case OFF:
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                break;
            case AUTO:
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                break;
            case TORCH:
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                break;
        }
        mCamera.setParameters(parameters);
    }

    public void setEffect(Effect effect) {
        if (mCamera == null) {
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        switch (effect) {
            case none:
                parameters.setColorEffect(Camera.Parameters.EFFECT_NONE);
                break;
            case mono:
                parameters.setColorEffect(Camera.Parameters.EFFECT_MONO);
                break;
            case negative:
                parameters.setColorEffect(Camera.Parameters.EFFECT_NEGATIVE);
                break;
            case solarize:
                parameters.setColorEffect(Camera.Parameters.EFFECT_SOLARIZE);
                break;
            case sepia:
                parameters.setColorEffect(Camera.Parameters.EFFECT_SEPIA);
                break;
            case posetrize:
                parameters.setColorEffect(Camera.Parameters.EFFECT_POSTERIZE);
                break;
            case blackboard:
                parameters.setColorEffect(Camera.Parameters.EFFECT_BLACKBOARD);
                break;
            case whiteboard:
                parameters.setColorEffect(Camera.Parameters.EFFECT_WHITEBOARD);
                break;
            case aqua:
                parameters.setColorEffect(Camera.Parameters.EFFECT_AQUA);
                break;
        }
        mCamera.setParameters(parameters);
    }

    // 闪光灯模式
    public enum FlashMode {
        /**
         * ON:拍照时打开闪光灯
         */
        ON,
        /**
         * OFF：不打开闪光灯
         */
        OFF,
        /**
         * AUTO：系统决定是否打开闪光灯
         */
        AUTO,
        /**
         * TORCH：一直打开闪光灯
         */
        TORCH
    }

    // 相机效果
    public enum Effect {
        none,
        mono,
        negative,
        solarize,
        sepia,
        posetrize,
        blackboard,
        whiteboard,
        aqua
    }
}
