package com.example.camerademo.opengles.filter;

/**
 * Created by v_yanligang on 2017/5/18.
 */

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.example.camerademo.magic.Rotation;
import com.example.camerademo.magic.TextureRotationUtil;
import com.example.camerademo.opengles.OpenGlUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.LinkedList;

public class BaseFilter {
    public static final String VERTEX_SHADERCODE =
            "attribute vec4 position;\n" +
                    "attribute vec4 inputTextureCoordinate;\n" +
                    " \n" +
                    "varying vec2 textureCoordinate;\n" +
                    " \n" +
                    "void main()\n" +
                    "{\n" +
                    "    gl_Position = position;\n" +
                    "    textureCoordinate = inputTextureCoordinate.xy;\n" +
                    "}";

    public static final String FRAGMENT_SHADERCODE =
            "#extension GL_OES_EGL_image_external : require\n"+
                    "precision mediump float;" +
                    "varying vec2 textureCoordinate;\n" +
                    "uniform samplerExternalOES s_texture;\n" +
                    "void main() {" +
                    "  gl_FragColor = texture2D( s_texture, textureCoordinate );\n" +
                    "}";

    public FloatBuffer vertexBuffer, textureVerticesBuffer;
    public ShortBuffer drawListBuffer;
    /**
     * 着色器程序
     */
    public int mProgram;
    public int mGLAttribPosition;
    public int mGlUniformTexture;
    public int mGLAttribTextureCoordinate;
    public String mVertexShader;
    public String mFragmentShader;
    private LinkedList<Runnable> mRunOnDraw = new LinkedList<>();
    protected int mIntputWidth;
    protected int mIntputHeight;
    protected int mOutputWidth, mOutputHeight;


    public short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices

    // number of coordinates per vertex in this array
    public static final int COORDS_PER_VERTEX = 2;

    public final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    static float squareCoords[] = {
            -1.0f,  1.0f,
            -1.0f, -1.0f,
            1.0f, -1.0f,
            1.0f,  1.0f,
    };

    static float textureVertices[] = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f,
    };

    public BaseFilter() {
//        this(VERTEX_SHADERCODE, FRAGMENT_SHADERCODE);
    }
    public BaseFilter(String vertexShaderCode, String fragmentShaderCode)
    {
        this.mVertexShader = vertexShaderCode;
        this.mFragmentShader = fragmentShaderCode;
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(TextureRotationUtil.CUBE.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
//        vertexBuffer.put(squareCoords);
        vertexBuffer.put(TextureRotationUtil.CUBE).position(0);
//        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
//        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
//        dlb.order(ByteOrder.nativeOrder());
//        drawListBuffer = dlb.asShortBuffer();
//        drawListBuffer.put(drawOrder);
//        drawListBuffer.position(0);

//        ByteBuffer bb2 = ByteBuffer.allocateDirect(textureVertices.length * 4);
//        bb2.order(ByteOrder.nativeOrder());
//        textureVerticesBuffer = bb2.asFloatBuffer();
//        textureVerticesBuffer.put(textureVertices);
//        textureVerticesBuffer.position(0);

        textureVerticesBuffer = ByteBuffer.allocateDirect(TextureRotationUtil.TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        textureVerticesBuffer.put(TextureRotationUtil.getRotation(Rotation.NORMAL, false, true)).position(0);
    }

    public void init() {
        Log.e("BaseFilter", "vertexshader" + mVertexShader + "fragmentshader" + mFragmentShader);
        mProgram = OpenGlUtils.loadProgram(mVertexShader, mFragmentShader);
        // 获取着色器程序中，指定为attribute类型变量的id
        mGLAttribPosition = GLES20.glGetAttribLocation(mProgram, "position");
        // 获取着色器程序中，指定为uniform类型变量的id。
        mGlUniformTexture = GLES20.glGetUniformLocation(mProgram, "inputImageTexture");
        mGLAttribTextureCoordinate = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");
        onInitialized();
    }

    protected void onInitialized() {
    }

    public void draw(float[] mtx, int textureId)
    {
        GLES20.glUseProgram(mProgram);
        // 绑定纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);

        // get handle to vertex shader's vPosition member
        // Enable a handle to the triangle vertices
        // 允许使用顶点坐标数组
        GLES20.glEnableVertexAttribArray(mGLAttribPosition);
        // Prepare the <insert shape here> coordinate data
        // 顶点位置数据传入着色器
        GLES20.glVertexAttribPointer(mGLAttribPosition, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
        // 允许使用定点纹理数组
        GLES20.glEnableVertexAttribArray(mGLAttribTextureCoordinate);
//        textureVerticesBuffer.clear();
//        textureVerticesBuffer.put( transformTextureCoordinates( textureVertices, mtx ));
//        textureVerticesBuffer.position(0);
        // 顶点坐标传递到顶点着色器
        GLES20.glVertexAttribPointer(mGLAttribTextureCoordinate, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, textureVerticesBuffer);

        GLES20.glUniform1i(mGlUniformTexture, 0);

        onDrawArraysPre();
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mGLAttribPosition);
        GLES20.glDisableVertexAttribArray(mGLAttribTextureCoordinate);
        onDrawArraysAfter();
    }

    public int draw(final int textureId, final FloatBuffer cubeBuffer,
                           final FloatBuffer textureBuffer) {
        GLES20.glUseProgram(mProgram);
        runPendingOnDrawTasks();
        cubeBuffer.position(0);
        // 顶点位置数据传入着色器
        GLES20.glVertexAttribPointer(mGLAttribPosition, 2, GLES20.GL_FLOAT, false, 0, cubeBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribPosition);
        textureBuffer.position(0);
        // 顶点坐标传递到顶点着色器
        GLES20.glVertexAttribPointer(mGLAttribTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0,
                textureBuffer);
        GLES20.glEnableVertexAttribArray(mGLAttribTextureCoordinate);
        if (textureId != OpenGlUtils.NO_TEXTURE) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
            GLES20.glUniform1i(mGlUniformTexture, 0);
        }
        onDrawArraysPre();
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(mGLAttribPosition);
        GLES20.glDisableVertexAttribArray(mGLAttribTextureCoordinate);
        onDrawArraysAfter();
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
//        Log.e("BaseFilter", "mGLAttribPosition" + mGLAttribPosition + "mGLAttribTextureCoordinate" + mGLAttribTextureCoordinate);
        return OpenGlUtils.ON_DRAWN;
    }

    protected void onDrawArraysPre() {
    }

    protected void onDrawArraysAfter() {
    }

    private float[] transformTextureCoordinates( float[] coords, float[] matrix)
    {
        float[] result = new float[ coords.length ];
        float[] vt = new float[4];

        for ( int i = 0 ; i < coords.length ; i += 2 ) {
            float[] v = { coords[i], coords[i+1], 0 , 1  };
            Matrix.multiplyMV(vt, 0, matrix, 0, v, 0);
            result[i] = vt[0];
            result[i+1] = vt[1];
        }
        return result;
    }

    public void onInputSizeChanged(final int width, final int height) {
        mIntputWidth = width;
        mIntputHeight = height;
    }


    protected void setFloatVec2(final int location, final float[] arrayValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform2fv(location, 1, FloatBuffer.wrap(arrayValue));
            }
        });
    }

    protected void runOnDraw(final Runnable runnable) {
        synchronized (mRunOnDraw) {
            mRunOnDraw.addLast(runnable);
        }
    }

    public void onDisplaySizeChanged(final int width, final int height) {
        mOutputWidth = width;
        mOutputHeight = height;
    }

    protected void runPendingOnDrawTasks() {
        while (!mRunOnDraw.isEmpty()) {
            mRunOnDraw.removeFirst().run();
        }
    }
}
