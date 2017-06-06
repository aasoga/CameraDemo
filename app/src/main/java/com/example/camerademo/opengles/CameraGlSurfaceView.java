package com.example.camerademo.opengles;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.example.camerademo.camera.CameraInfo;
import com.example.camerademo.magic.MagicBaseView;
import com.example.camerademo.magic.MagicFilterType;
import com.example.camerademo.magic.Rotation;
import com.example.camerademo.magic.TextureRotationUtil;
import com.example.camerademo.magic.encoder.video.TextureMovieEncoder;
import com.example.camerademo.opengles.filter.BaseFilter;
import com.example.camerademo.opengles.filter.CameraFilter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by v_yanligang on 2017/5/16.
 */

public class CameraGlSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    private Context mContext;
    private SurfaceTexture mTexture;
    private int mTextureID = -1;
    private CameraFilter mCameraFilter;
    private BaseFilter mFilterDrawer;

    /**
     * 顶点坐标
     */
    protected FloatBuffer gLCubeBuffer;

    /**
     * 纹理坐标
     */
    protected FloatBuffer gLTextureBuffer;
    /**
     * 图像宽高
     */
    protected int imageWidth, imageHeight;
    /**
     * GLSurfaceView的宽高
     */
    protected int surfaceWidth, surfaceHeight;


    protected MagicBaseView.ScaleType scaleType = MagicBaseView.ScaleType.FIT_XY;

    private static TextureMovieEncoder videoEncoder = new TextureMovieEncoder();


    public CameraGlSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 创建顶点坐标和纹理坐标
        createBuffer();
        mContext = context;
        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    private void createBuffer() {
        gLCubeBuffer = ByteBuffer.allocateDirect(OpenGlUtils.CUBE.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        gLCubeBuffer.put(OpenGlUtils.CUBE).position(0);

        gLTextureBuffer = ByteBuffer.allocateDirect(OpenGlUtils.TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        gLTextureBuffer.put(OpenGlUtils.TEXTURE_NO_ROTATION).position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        mTextureID = createTextureID();
        mTexture = new SurfaceTexture(mTextureID);
        mTexture.setOnFrameAvailableListener(this);

        if (mCameraFilter == null) {
            mCameraFilter = new CameraFilter(mContext);
            mCameraFilter.init();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        GLES20.glViewport(0,0, width, height);
        surfaceWidth = width;
        surfaceHeight = height;
        if (CameraManager.getInstance().getCamera() == null) {
            CameraManager.getInstance().openCamera();
        }
//        if (!CameraManager.getInstance().isPrewviewing()) {
            CameraManager.getInstance().startPreview(mTexture);
//        }
        CameraInfo info = CameraManager.getInstance().getCameraInfo();
        if(info.orientation == 90 || info.orientation == 270){
            imageWidth = info.previewHeight;
            imageHeight = info.previewWidth;
        }else{
            imageWidth = info.previewWidth;
            imageHeight = info.previewHeight;
        }
        adjustSize(info.orientation, info.isFront, true);
        mCameraFilter.onInputSizeChanged(imageWidth,imageHeight);
        if (mFilterDrawer != null) {
            mFilterDrawer.onDisplaySizeChanged(surfaceWidth, surfaceHeight);
            mFilterDrawer.onInputSizeChanged(imageWidth, imageHeight);
        }
    }

    /**
     * 首先第一个一定是绘制与SurfaceTexture绑定的外部纹理处理后的无滤镜效果，之后的操作与第一个一样，都是绘制到纹理。
     * 首先与之前相同传入纹理id，并重新绑定到对应的缓冲区对象GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffers[i])，
     * 之后draw对应的纹理id。若不是最后一个滤镜，需要解绑缓冲区，下一个滤镜的新的纹理id即上一个滤镜的缓冲区对象所对应的纹理id，同样执行上述步骤，直到最后一个滤镜。
     * @param gl
     */
    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
//        GLES20.glClearColor(1.0f,1.0f,1.0f,1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT| GLES20.GL_DEPTH_BUFFER_BIT);
        mTexture.updateTexImage();
        float[] mtx = new float[16];
        mTexture.getTransformMatrix(mtx);
        mCameraFilter.setTextureTransformMatrix(mtx);
        int id = mTextureID;
//        Log.e("CameraGlSurfaceView", "mTextureID" + mTextureID);
        if (mFilterDrawer == null) {
            mCameraFilter.draw(mTextureID, gLCubeBuffer, gLTextureBuffer);
        } else {
            id = mCameraFilter.onDrawToTexture(mTextureID);

            mFilterDrawer.draw(id, gLCubeBuffer, gLTextureBuffer);
        }
        videoEncoder.setTextureId(id);
        videoEncoder.frameAvailable(mTexture);
    }

    @Override
    public void onPause() {
        super.onPause();

        CameraManager.getInstance().stopCamera();
    }

    public void setDrawer(BaseFilter drawer) {
        this.mFilterDrawer = drawer;
        videoEncoder.setFilter(MagicFilterType.COOL);
        queueEvent(new Runnable() {
            @Override
            public void run() { // 着色器依赖当前渲染的线程，渲染对象运行在一个独立的渲染线程中，调用queueEvent即可
                mFilterDrawer.init();
                mCameraFilter.initCameraFrameBuffer(imageWidth,imageHeight);
                // 缺这句会黑屏
                mCameraFilter.onDisplaySizeChanged(surfaceWidth, surfaceHeight);
                mFilterDrawer.onDisplaySizeChanged(surfaceWidth, surfaceHeight);
                mFilterDrawer.onInputSizeChanged(imageWidth, imageHeight);
                requestRender();
            }
        });

        requestRender();

    }
    private int createTextureID() {
        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        return texture[0];
    }


    public static int getExternalOESTextureID(){
        int[] texture = new int[1];
        GLES20.glGenTextures(1, texture, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        return texture[0];
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        this.requestRender();
    }

    protected void adjustSize(int rotation, boolean flipHorizontal, boolean flipVertical){
        float[] textureCords = TextureRotationUtil.getRotation(Rotation.fromInt(rotation),
                flipHorizontal, flipVertical);
        float[] cube = TextureRotationUtil.CUBE;
        float ratio1 = (float)surfaceWidth / imageWidth;
        float ratio2 = (float)surfaceHeight / imageHeight;
        float ratioMax = Math.max(ratio1, ratio2);
        int imageWidthNew = Math.round(imageWidth * ratioMax);
        int imageHeightNew = Math.round(imageHeight * ratioMax);

        float ratioWidth = imageWidthNew / (float)surfaceWidth;
        float ratioHeight = imageHeightNew / (float)surfaceHeight;

        if(scaleType == MagicBaseView.ScaleType.CENTER_INSIDE){
            cube = new float[]{
                    TextureRotationUtil.CUBE[0] / ratioHeight, TextureRotationUtil.CUBE[1] / ratioWidth,
                    TextureRotationUtil.CUBE[2] / ratioHeight, TextureRotationUtil.CUBE[3] / ratioWidth,
                    TextureRotationUtil.CUBE[4] / ratioHeight, TextureRotationUtil.CUBE[5] / ratioWidth,
                    TextureRotationUtil.CUBE[6] / ratioHeight, TextureRotationUtil.CUBE[7] / ratioWidth,
            };
        }else if(scaleType == MagicBaseView.ScaleType.FIT_XY){

        }else if(scaleType == MagicBaseView.ScaleType.CENTER_CROP){
            float distHorizontal = (1 - 1 / ratioWidth) / 2;
            float distVertical = (1 - 1 / ratioHeight) / 2;
            textureCords = new float[]{
                    addDistance(textureCords[0], distVertical), addDistance(textureCords[1], distHorizontal),
                    addDistance(textureCords[2], distVertical), addDistance(textureCords[3], distHorizontal),
                    addDistance(textureCords[4], distVertical), addDistance(textureCords[5], distHorizontal),
                    addDistance(textureCords[6], distVertical), addDistance(textureCords[7], distHorizontal),
            };
        }
        gLCubeBuffer.clear();
        gLCubeBuffer.put(cube).position(0);
        gLTextureBuffer.clear();
        gLTextureBuffer.put(textureCords).position(0);
    }


    private float addDistance(float coordinate, float distance) {
        return coordinate == 0.0f ? distance : 1 - distance;
    }

    public enum  ScaleType{
        CENTER_INSIDE,
        CENTER_CROP,
        FIT_XY;
    }

    public void switchCamera() {
        CameraManager.getInstance().switchCamera();
    }

    public void onBeautyLevelChanged(int level) {
        mCameraFilter.onBeautyLevelChanged(level);
    }
}
