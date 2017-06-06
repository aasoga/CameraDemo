package com.example.camerademo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Created by v_yanligang on 2017/5/5.
 */

public class ImageHelper {

    public static final int STYLE_NEGATIVE = 1;
    public static final int STYLE_OLD = 2;
    public static final int STYLE_RELIEF = 3;

    public static Bitmap handleImageEffect(Bitmap bm, Bitmap bmp, float hue, float saturation, float lum) {
//        Bitmap bmp = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        ColorMatrix hueMatrix = new ColorMatrix();
        hueMatrix.setRotate(0,hue);
        hueMatrix.setRotate(1,hue);
        hueMatrix.setRotate(2,hue);

        ColorMatrix saturationMatrix = new ColorMatrix();
        saturationMatrix.setSaturation(saturation);

        ColorMatrix lumMatrix = new ColorMatrix();
        lumMatrix.setScale(lum,lum,lum,1);

        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.postConcat(hueMatrix);
        colorMatrix.postConcat(saturationMatrix);
        colorMatrix.postConcat(lumMatrix);

        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        canvas.drawBitmap(bm,0,0,paint);

        return bmp;
    }

    public static Bitmap handleImagePixels(Bitmap bm, int style) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        int[] oldPx = new int[width*height];
        int[] newPx = new int[width*height];
        int r, g, b, a, r1, g1, b1;
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bm.getPixels(oldPx, 0, width, 0, 0, width, height);
        switch (style) {
            case STYLE_NEGATIVE:
                for (int i = 0; i <width*height ; i++) {
                    int color = oldPx[i];
                    r = Color.red(color);
                    g = Color.green(color);
                    b = Color.blue(color);
                    a = Color.alpha(color);

                    r = 255 - r;
                    g = 255- g;
                    b = 255- b;
                    normData(r,g,b);
                    newPx[i] = Color.argb(a,r,g,b);
                }
                break;
            case STYLE_OLD:
                for (int i = 0; i <width*height ; i++) {
                    int color = oldPx[i];
                    r = Color.red(color);
                    g = Color.green(color);
                    b = Color.blue(color);
                    a = Color.alpha(color);

                    r1 = (int) (0.393*r + 0.769*g + 0.189*b);
                    g1 = (int) (0.349 * r + 0.686 * g + 0.168 * b);
                    b1 = (int) (0.272 * r + 0.534 * g + 0.131 * b);
                    normData(r1,g1,b1);
                    newPx[i] = Color.argb(a,r1,g1,b1);
                }
                break;
            case STYLE_RELIEF:
                int color = 0, colorBefore = 0;
                for (int i = 1; i < width * height; i++) {
                    colorBefore = oldPx[i - 1];
                    a = Color.alpha(colorBefore);
                    r = Color.red(colorBefore);
                    g = Color.green(colorBefore);
                    b = Color.blue(colorBefore);

                    color = oldPx[i];
                    r1 = Color.red(color);
                    g1 = Color.green(color);
                    b1 = Color.blue(color);

                    r = (r - r1 + 127);
                    g = (g - g1 + 127);
                    b = (b - b1 + 127);
                    if (r > 255) {
                        r = 255;
                    }
                    if (g > 255) {
                        g = 255;
                    }
                    if (b > 255) {
                        b = 255;
                    }
                    newPx[i] = Color.argb(a, r, g, b);
                }
                break;
            default:
                break;
        }

        bmp.setPixels(newPx, 0, width, 0, 0, width, height);
        return bmp;
    }

    private static void normData(int r, int g, int b) {
        if (r >255) {
            r = 255;
        }else if (r < 0) {
            r = 0;
        }
        if (g >255) {
            g = 255;
        }else if (g < 0) {
            g = 0;
        }
        if (b >255) {
            b = 255;
        }else if (b < 0) {
            b = 0;
        }
    }

    // 压缩图片 尺寸压缩 根据路径压缩
    public static Bitmap getCompressBitmap(String path, float desWidth, float desHeight) {

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 设为true，则返回的bm为空，只解析宽高
        BitmapFactory.decodeFile(path, options);
        options.inJustDecodeBounds = false;
        float w = options.outWidth; // 图片的宽高
        float h = options.outHeight;

        // 设置缩放比 由原宽高比上设置的宽高，所以比例越大，则图片尺寸越小
        // 由于是固定比例缩放，所以只需要对宽或高一个数据进行计算即可
        float be = 1.0f; //默认不缩放
        if (w > h && w > desWidth) { // 如果宽度更大，则用宽度比例缩放
            be = (float)(w/desWidth);
        } else if (w < h && h > desHeight) {
            be = h/desHeight;
        }
        if (be < 0) {
            be = 1;
        }
        options.inSampleSize = (int) be;
        // 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
       Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        // 修改图片尺寸
        int width = (int) (w / be);
        int height = (int) (h / be);
        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        Log.e("ImageHelper", "desWidth" + desWidth + "desHeight" + desHeight + "be" + be);
        return compressBitmap(bitmap);
    }

    // 压缩图片，质量压缩
    public static Bitmap compressBitmap(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos); // 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
//        int option = 80;
//        while (baos.toByteArray().length/1024 > 1000) {// 循环判断如果压缩后图片是否大于1000kb,大于继续压缩
//            baos.reset();
//            bitmap.compress(Bitmap.CompressFormat.JPEG, option, baos);
//            option -= 100; //每次减少100
//        }
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        Log.e("ImageHelper", "length " + baos.toByteArray().length);
        Bitmap desBitmap = BitmapFactory.decodeStream(bais);
        return desBitmap;
    }
}
