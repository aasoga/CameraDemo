package com.example.camerademo.opengles;

import android.opengl.GLES20;
import android.util.Log;

/**
 * Created by v_yanligang on 2017/5/27.
 */

public class OpenGlUtils {
    public static final int NO_TEXTURE = -1;
    public static final int NOT_INIT = -1;
    public static final int ON_DRAWN = 1;


    public static final float TEXTURE_NO_ROTATION[] = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
    };

    public static final float CUBE[] = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,
    };

    public static int loadProgram(String vertexSource, String fragmentSource) {
        int vertextShader;
        int fragmentShader;
        int programId;
        int[] link = new int[1];

        vertextShader = loadShader(vertexSource, GLES20.GL_VERTEX_SHADER);
        if (vertextShader == 0) {
            Log.e("Load Program", "Vertex Shader Failed");
            return 0;
        }
        fragmentShader = loadShader(fragmentSource, GLES20.GL_FRAGMENT_SHADER);
        if (fragmentShader == 0) {
            Log.e("Load Program", "Fragment Shader Failed");
            return 0;
        }
        // 创建着色器程序
        programId = GLES20.glCreateProgram();
        // 向程序中加入顶点着色器
        GLES20.glAttachShader(programId, vertextShader);
        GLES20.glAttachShader(programId, fragmentShader);
        // 链接程序
        GLES20.glLinkProgram(programId);
        GLES20.glGetProgramiv(programId, GLES20.GL_LINK_STATUS, link, 0);
        if (link[0] <= 0) {
            Log.e("Load Program", "Linking Failed");
            return 0;
        }

        GLES20.glDeleteShader(vertextShader);
        GLES20.glDeleteShader(fragmentShader);
        return programId;
    }
    private static int loadShader(String shaderCode, int type){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        // 编译shader
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e("Load Shader Failed", "Compilation\n" + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            return 0;
        }
        return shader;
    }
}
