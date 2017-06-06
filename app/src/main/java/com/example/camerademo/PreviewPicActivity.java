package com.example.camerademo;

import android.app.Activity;
import android.app.SharedElementCallback;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.camerademo.camera.FastBlurUtil;
import com.example.camerademo.camera.ImageLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.camerademo.SelectPicActivity.EXTRA_TYPE;

/**
 * Created by v_yanligang on 2016/11/10.
 */


public class PreviewPicActivity extends Activity {
    private ViewPager mPager;
    private List<String> mPathList = new ArrayList<>();
    private int mPosition; // 传过来的初始位置
    private boolean isBlur = false;
    private Myadapter Madapter;
    private final int VIEW_SIZE = 4;
    private int mCurrentPosition;
    private boolean isReturn;
    private Bundle msavedInstanceState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_previewpic);
        initData();
        initView();
        msavedInstanceState = savedInstanceState;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition();
            setEnterSharedElementCallback(new SharedElementCallback() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                    if(isReturn) {
                        ImageView imageView = Madapter.getmViews().get(mCurrentPosition % VIEW_SIZE);
                        if (imageView == null) {
                            names.clear();
                            sharedElements.clear();
                        } else if (mCurrentPosition != mPosition){ // 说明翻页了
                            names.clear();
                            sharedElements.clear();
                            LogUtils.e("preview onmap", "name" + imageView.getTransitionName());
                            names.add(imageView.getTransitionName());
                            sharedElements.put(imageView.getTransitionName(), imageView);
                        }
                    }
                }
            });
        } else {
//            ActivityTransition.with(getIntent()).to(mPager.getChildAt(mPosition)).start(msavedInstanceState);
        }

    }

    private void initView() {
        Madapter = new Myadapter(getApplicationContext());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(Madapter);
        final TextView bt = (TextView) findViewById(R.id.bt);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBlur = true;
                Madapter.notifyDataSetChanged();
            }
        });
        findViewById(R.id.bt_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = mPathList.get(mCurrentPosition);
                File file = new File(path);
                file.delete();
                mPathList.remove(mCurrentPosition);
                if (mPathList.size() == 0) {
                    finish();
                    return;
                }
                Madapter.notifyDataSetChanged();
            }
        });

        findViewById(R.id.edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = mPathList.get(mCurrentPosition);
                Intent intent = new Intent(PreviewPicActivity.this, ImageEditActivity.class);
                intent.putExtra(SelectPicActivity.EXTRA_FILE, path);
                startActivity(intent);
            }
        });

        // 过渡动画
        if (mPosition == 0) {// position是0时，onPageSelected不会触发
            List<ImageView> imageViews = Madapter.getmViews();
            final ImageView imageView = imageViews.get(mPosition % VIEW_SIZE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // 需要设置imagev的setTransitionName,否则会和返回的tranname对不上
                ViewCompat.setTransitionName(imageView, String.valueOf(mPosition));
                    imageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public boolean onPreDraw() {
                            imageView.getViewTreeObserver().removeOnPreDrawListener(this);
                            startPostponedEnterTransition();
                            return true;
                        }
                    });
            } else {
//                    ActivityTransition.with(getIntent()).to(imageView).start(msavedInstanceState);
            }
        }
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                // position是0时，此方法不会触发
                LogUtils.e("onPageSelected", "position" + mCurrentPosition);
                List<ImageView> imageViews = Madapter.getmViews();
                final ImageView imageView = imageViews.get(position % VIEW_SIZE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // 需要设置imagev的setTransitionName,否则会和返回的tranname对不上
                    ViewCompat.setTransitionName(imageView, String.valueOf(position));
                    if(mPosition == position) {
                        imageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                            @Override
                            public boolean onPreDraw() {
                                imageView.getViewTreeObserver().removeOnPreDrawListener(this);
                                startPostponedEnterTransition();
                                return true;
                            }
                        });
                    }
                } else {
//                    ActivityTransition.with(getIntent()).to(imageView).start(msavedInstanceState);
                }

//                setImage(imageView, position);

                mCurrentPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mPager.setCurrentItem(mPosition);
    }

    private void initData() {
        Intent intent = getIntent();
        mPathList = intent.getStringArrayListExtra("list");
        mPosition = intent.getIntExtra("position", 0);
        mCurrentPosition = mPosition;
    }

    @Override
    public void finishAfterTransition() {
        isReturn = true;
        Intent intent = new Intent();
        intent.putExtra(SelectPicActivity.EXTRA_CURRENT_POSITION, mCurrentPosition);
        LogUtils.e("preview", "position" + mCurrentPosition);
        setResult(RESULT_OK, intent);
        super.finishAfterTransition();
    }

    private ImageSize getPathSize(String path, ImageView imageView) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        ImageSize imageSize = new ImageSize();
        imageSize.height = options.outHeight;
        imageSize.width = options.outWidth;
        ImageSize viewSize = getImageViewSize(imageView);
        if (imageSize.height > viewSize.height || imageSize.width > viewSize.width) {
            imageSize.height = viewSize.height;
            imageSize.width = viewSize.width;
        }
        return imageSize;
    }

    private ImageSize getImageViewSize(ImageView view) {
        ImageSize imageSize = new ImageSize();
        imageSize.height = view.getHeight();
        imageSize.width = view.getWidth();
        if (imageSize.height <= 0 || imageSize.width <= 0) {
            DisplayMetrics metrics = view.getContext().getResources().getDisplayMetrics();
            imageSize.height = metrics.heightPixels;
            imageSize.width = metrics.widthPixels;
        }
        return imageSize;
    }

    public class ImageSize {
        int width;
        int height;
    }

    public void setImage(ImageView imageView, int position) {
        Log.e("onPageSelected", imageView.getHeight() + "width" + "position + {position}" + position);
        if (isBlur) {
            Log.e("Pre", "isBlur" + isBlur);
            isBlur = false;
            Bitmap bitmap = BitmapFactory.decodeFile(mPathList.get(position));
            int scaleRatio = 10;
            int blurRadius = 8;
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap,
                    bitmap.getWidth() / scaleRatio,
                    bitmap.getHeight() / scaleRatio,
                    false);
            Bitmap bitmap1 = FastBlurUtil.doBlur(scaledBitmap, 8, true);
            imageView.setImageBitmap(bitmap1);
            return;
        }
        Log.e("setImage", " {position}" + mPathList.get(position));
        ImageSize size = getPathSize(mPathList.get(position), imageView);
        ImageLoader.load(imageView, mPathList.get(position), size.width, size.height);
    }

//    @Override
//    public void onBackPressed() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            super.onBackPressed();
//        } else {
//            ActivityTransition.with(getIntent()).to(mPager.getChildAt(mPosition)).start(msavedInstanceState).exit(this);
//        }
//
//    }

    public class Myadapter extends PagerAdapter {
        private List<ImageView> mViews = new ArrayList<>();


        public Myadapter(Context context) {
            for (int i = 0; i < VIEW_SIZE; i++) {
                ImageView imageView = new ImageView(context);
                mViews.add(imageView);
            }
        }

        @Override
        public int getCount() {
            return mPathList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            LogUtils.e("destroyItem", position + "position");
            container.removeView((View) object);
        }

        public List<ImageView> getmViews() {
            return mViews;
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView imageView = mViews.get(position % VIEW_SIZE);
            LogUtils.e("instantiateItem", position + "position");
//            if (position == mPosition) { // 只设置当前位置的setTransitionName
//                ViewCompat.setTransitionName(imageView, String.valueOf(mPosition));
//            }
            imageView.setTag(R.id.tag_position,position);
            container.addView(imageView);
            setImage(imageView, position);
            return imageView;
        }

        @Override
        public int getItemPosition(Object object) {
            View view = (View) object;
            LogUtils.e("getItemPosition", "view.getTag" + (int)view.getTag(R.id.tag_position));
            if (mCurrentPosition == (int)view.getTag(R.id.tag_position) || mCurrentPosition == (int)view.getTag(R.id.tag_position) - 1) { // 当view为当前页面时，刷新页面
                return POSITION_NONE;
            } else {
                return POSITION_UNCHANGED;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        int type = getIntent().getIntExtra(EXTRA_TYPE, -1);

    }
}
