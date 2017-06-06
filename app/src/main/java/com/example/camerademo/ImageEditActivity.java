package com.example.camerademo;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by v_yanligang on 2017/5/4.
 */

public class ImageEditActivity extends Activity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "ImageEditActivity";
    private static int MAX_VALUE = 255;
    private static int MID_VALUE = 127;
    private static final int STATUS_HUE = 1;
    private static final int STATUS_SATURATION = 2;
    private static final int STATUS_LUM = 3;
    private static final int STATUS_NONE = 0;
    private static final int MSG_IMAGE = 1;
    private static final int MSG_PRIMARY = 2;
    private static final int MSG_NEGATIVE = 3;
    private static final int MSG_OLD = 4;
    private static final int MSG_RELIEF = 5;
    private static final int MSG_FILTER = 6;

    private static final int FILTER_PRIMARY = 0;
    private static final int FILTER_NEGATIVE = 1;
    private static final int FILTER_OLD = 2;
    private static final int FILTER_RELIEF = 3;
    private ImageView mIv;
    private Bitmap mBitmap;
    private Bitmap mChangedBitmap;
    private View mEffectView;
    private View mFilterView;
    private HorizontalScrollView mHsv;
    private SeekBar mSeekBar;
    private LinearLayout mLl;
    private RelativeLayout mSeekBarRl;
    private float mHue = 0 ; // 色相
    private float mSaturation =  1 ; // 饱和度
    private float mLum = 1 ; // 亮度
    private int mStatus = STATUS_NONE;
    private int mFilter = FILTER_PRIMARY;

    private CheckBox mHueTv;
    private CheckBox mSaturationTv;
    private CheckBox mLumTv;

    private ImageView primaryIv;
    private ImageView negativeIv;
    private ImageView oldIv;
    private ImageView reliefIv;
    private CheckBox primaryCb;
    private CheckBox negativeCb;
    private CheckBox oldCb;
    private CheckBox reliefCb;
    private boolean isDeal;
    private String mPath;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_IMAGE:
//                    mIv.setImageBitmap(mBitmap);
                    mIv.setImageBitmap(mBitmap);
                    isDeal = false;
                    break;
                case MSG_PRIMARY:
                    dealImage(primaryIv, ImageHelper.STYLE_NEGATIVE, MSG_NEGATIVE);
                    break;
                case MSG_NEGATIVE:
                    dealImage(negativeIv, ImageHelper.STYLE_OLD, MSG_OLD);
                    break;
                case MSG_OLD:
                    dealImage(oldIv, ImageHelper.STYLE_RELIEF, MSG_RELIEF);
                    break;
                case MSG_RELIEF:
                    reliefIv.setImageBitmap(mChangedBitmap);
                    break;
                case MSG_FILTER:
                    mIv.setImageBitmap(mChangedBitmap);
                    isDeal = false;
                    break;

            }
        }
    };

    private void dealImage(ImageView iv, final int style, final int msg) {
        iv.setImageBitmap(mChangedBitmap);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mChangedBitmap = ImageHelper.handleImagePixels(mChangedBitmap, style);
                mHandler.sendEmptyMessage(msg);

            }
        }).start();
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imageedit);
        mPath = getIntent().getStringExtra(SelectPicActivity.EXTRA_FILE);
//        mBitmap = BitmapFactory.decodeFile(mPath);
//        Log.e(TAG, "width + height" + mBitmap.getWidth() + "" + mBitmap.getHeight());
//        mChangedBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        initView();
    }

    private void initView() {
        mIv = (ImageView) findViewById(R.id.iv);
//        mIv.setImageBitmap(mBitmap);
        mIv.post(new Runnable() {
            @Override
            public void run() {
                mBitmap = ImageHelper.getCompressBitmap(mPath, mIv.getWidth(), mIv.getHeight());
                mHandler.sendEmptyMessage(MSG_IMAGE);
                mChangedBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            }
        });

        TextView effectTv = (TextView) findViewById(R.id.tv_effect);
        TextView filterTv = (TextView) findViewById(R.id.tv_filter);

        effectTv.setOnClickListener(this);
        filterTv.setOnClickListener(this);

        mHsv = (HorizontalScrollView) findViewById(R.id.hsv);
        mLl = (LinearLayout) findViewById(R.id.ll);
        mSeekBar = (SeekBar) findViewById(R.id.seekBar);
        mSeekBarRl = (RelativeLayout) findViewById(R.id.seekbar_rl);
        findViewById(R.id.seekbar_cancel).setOnClickListener(this);
        findViewById(R.id.seekbar_confirm).setOnClickListener(this);

        mSeekBar.setMax(MAX_VALUE);
        mSeekBar.setProgress(MID_VALUE);
        mSeekBar.setOnSeekBarChangeListener(this);

        if (mEffectView == null) {
            mEffectView = View.inflate(getApplicationContext(), R.layout.item_activity_effect, null);
            mHueTv = (CheckBox) mEffectView.findViewById(R.id.hue_tv);
            mHueTv.setOnClickListener(this);
            mSaturationTv = (CheckBox) mEffectView.findViewById(R.id.saturation_tv);
            mSaturationTv.setOnClickListener(this);
            mLumTv = (CheckBox) mEffectView.findViewById(R.id.bright_tv);
            mLumTv.setOnClickListener(this);
        }

        if (mFilterView == null) {
            mFilterView = View.inflate(getApplicationContext(), R.layout.item_activity_filter, null);
            primaryIv = (ImageView) mFilterView.findViewById(R.id.primary_iv);
            negativeIv = (ImageView) mFilterView.findViewById(R.id.negative_iv);
            oldIv = (ImageView) mFilterView.findViewById(R.id.old_iv);
            reliefIv = (ImageView) mFilterView.findViewById(R.id.relief_iv);

            primaryCb = (CheckBox) mFilterView.findViewById(R.id.primary_cb);
            negativeCb = (CheckBox) mFilterView.findViewById(R.id.negative_cb);
            oldCb = (CheckBox) mFilterView.findViewById(R.id.old_cb);
            reliefCb = (CheckBox) mFilterView.findViewById(R.id.relief_cb);

            mFilterView.findViewById(R.id.fl_primary).setOnClickListener(this);
            mFilterView.findViewById(R.id.fl_negative).setOnClickListener(this);
            mFilterView.findViewById(R.id.fl_old).setOnClickListener(this);
            mFilterView.findViewById(R.id.fl_relief).setOnClickListener(this);

        }
    }

    @Override
    public void onClick(View v) {
        if (mHueTv != null) {
            Log.e(TAG, "hue checked" + mHueTv.isChecked());
        }
        switch (v.getId()) {
            case R.id.tv_effect:

                if (!mEffectView.isShown()) {
                    mHsv.removeAllViews();
                    mHsv.addView(mEffectView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    startAnimate(mEffectView);

                } else {
                    mHsv.removeView(mEffectView);
                }
                break;
            case R.id.tv_filter:

                if (!mFilterView.isShown()) {
                    mHsv.removeAllViews();
                    mHsv.addView(mFilterView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    startAnimate(mFilterView);
                } else {
                    mHsv.removeView(mFilterView);
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mChangedBitmap = ImageHelper.getCompressBitmap(mPath, dip2px(getApplicationContext(), 100f), dip2px(getApplicationContext(), 100f));
                        mHandler.sendEmptyMessage(MSG_PRIMARY);

                    }
                }).start();
                break;
            case R.id.hue_tv:

                // 走onclick事件的时候checkbox属性已经被更改了
                onEffectClicked(STATUS_HUE);
                if (mHueTv.isChecked()) {
                    mSaturationTv.setChecked(false);
                    mLumTv.setChecked(false);
                }
                mSeekBar.setProgress((int) (mHue/180*MID_VALUE + MID_VALUE));
                break;
            case R.id.saturation_tv:
                onEffectClicked(STATUS_SATURATION);
                if (mSaturationTv.isChecked()) {
                    mHueTv.setChecked(false);
                    mLumTv.setChecked(false);
                }
                mSeekBar.setProgress((int) (mSaturation*MID_VALUE));
                break;
            case R.id.bright_tv:
                onEffectClicked(STATUS_LUM);
                if (mLumTv.isChecked()) {
                    mSaturationTv.setChecked(false);
                    mHueTv.setChecked(false);
                }
                mSeekBar.setProgress((int) (mLum*MID_VALUE));
                break;
            case R.id.seekbar_cancel:
                mSeekBarRl.setVisibility(View.GONE);
                mLl.setVisibility(View.VISIBLE);
                mHue = 0;
                mSaturation = 1;
                mLum = 1;
                break;
            case R.id.seekbar_confirm:
                mSeekBarRl.setVisibility(View.GONE);
                mLl.setVisibility(View.VISIBLE);

                break;
            case R.id.fl_primary:

                if (!primaryCb.isChecked()) {
                    primaryCb.setChecked(true);
                    negativeCb.setChecked(false);
                    oldCb.setChecked(false);
                    reliefCb.setChecked(false);
                }
                mIv.setImageBitmap(mBitmap);
                break;
            case R.id.fl_negative:

                if (!negativeCb.isChecked()) {
                    negativeCb.setChecked(true);
                    primaryCb.setChecked(false);
                    oldCb.setChecked(false);
                    reliefCb.setChecked(false);
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mChangedBitmap = ImageHelper.handleImagePixels(mBitmap, ImageHelper.STYLE_NEGATIVE);
                        mHandler.sendEmptyMessage(MSG_FILTER);

                    }
                }).start();
                break;
            case R.id.fl_old:

                if (!oldCb.isChecked()) {
                    oldCb.setChecked(true);
                    negativeCb.setChecked(false);
                    primaryCb.setChecked(false);
                    reliefCb.setChecked(false);
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mChangedBitmap = ImageHelper.handleImagePixels(mBitmap, ImageHelper.STYLE_OLD);
                        mHandler.sendEmptyMessage(MSG_FILTER);

                    }
                }).start();
                break;
            case R.id.fl_relief:

                if (!reliefCb.isChecked()) {
                    reliefCb.setChecked(true);
                    negativeCb.setChecked(false);
                    oldCb.setChecked(false);
                    primaryCb.setChecked(false);
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mChangedBitmap = ImageHelper.handleImagePixels(mBitmap, ImageHelper.STYLE_RELIEF);
                        mHandler.sendEmptyMessage(MSG_FILTER);

                    }
                }).start();
                break;
        }

    }


    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        switch (mStatus) {
            case STATUS_HUE:
                mHue = (progress - MID_VALUE)*1.0f/MID_VALUE*180;
                break;
            case STATUS_SATURATION:
                mSaturation = progress*1.0f/MID_VALUE;
                break;
            case STATUS_LUM:
                mLum = progress*1.0f/MID_VALUE;
                break;
        }
        if (!isDeal) {
            isDeal = true;
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    ImageHelper.handleImageEffect(mBitmap, mChangedBitmap, mHue, mSaturation, mLum);
                    mHandler.sendEmptyMessage(MSG_FILTER);

                }
            });
            thread.start();
        }
//        mIv.setImageBitmap(mChangedBitmap);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private void onEffectClicked(int status) {
        if (mSeekBarRl.getVisibility() == View.GONE) {
            startAnimate(mSeekBarRl);
            mLl.setVisibility(View.GONE);
        } else if (mStatus == status){
            mSeekBarRl.setVisibility(View.GONE);
            startAnimate(mLl);
        }
        mStatus = status;
    }

    public int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    private void startAnimate(final View view) {
        view.setVisibility(View.INVISIBLE);
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", view.getHeight(), 0);
        animator.setDuration(200);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();
    }
}
