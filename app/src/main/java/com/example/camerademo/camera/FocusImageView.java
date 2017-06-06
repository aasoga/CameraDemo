package com.example.camerademo.camera;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.camerademo.R;


/**
 * Created by v_yanligang on 2017/3/24.
 */

public class FocusImageView extends ImageView{

    private static final int DEFAULT_ID = -1;
    private int focusing_id;
    private int focusfailed_id;
    private int focussuccess_id;
    private Handler mHandler;
    private Animation mAnimation;
    private Context mContext;

    public FocusImageView(Context context) {
        super(context);
        setVisibility(View.GONE);
        mHandler = new Handler();
        mContext = context;
    }

    public FocusImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.FocusImageView);
        focusing_id = ta.getResourceId(R.styleable.FocusImageView_focus_focusing_id, DEFAULT_ID);
        focussuccess_id = ta.getResourceId(R.styleable.FocusImageView_focus_focussuccess_id, DEFAULT_ID);
        focusfailed_id = ta.getResourceId(R.styleable.FocusImageView_focus_focusfailed_id, DEFAULT_ID);
        setVisibility(View.GONE);
        mHandler = new Handler();
        mContext = context;
    }


    public void startFocus(Point point) {
        clearAnimation();
        mHandler.removeCallbacks(null);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
        layoutParams.topMargin = point.y - getHeight()/2;
        layoutParams.leftMargin = point.x - getWidth()/2;
        setLayoutParams(layoutParams);
        if (mAnimation == null) {
            mAnimation = AnimationUtils.loadAnimation(mContext, R.anim.focusview_show);
        }
        startAnimation(mAnimation);
        setVisibility(View.VISIBLE);
        setImageResource(focusing_id);
        // 防止聚焦回调没触发
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setVisibility(View.GONE);
            }
        }, 3000);
    }

    //聚焦成功的回调
    public void focusSuccess(){
        mHandler.removeCallbacks(null);
        setImageResource(focussuccess_id);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setVisibility(View.GONE);
            }
        }, 1000);
    }

    //聚焦成功的回调
    public void focusFailed(){
        mHandler.removeCallbacks(null);
        setImageResource(focusfailed_id);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setVisibility(View.GONE);
            }
        }, 1000);
    }
}
