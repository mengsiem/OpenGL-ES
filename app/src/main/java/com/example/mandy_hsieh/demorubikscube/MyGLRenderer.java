package com.example.mandy_hsieh.demorubikscube;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Mandy_Hsieh on 2016/4/20.
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {

    private static int LAYER = 3;
    private Cube[][][] mCube = new Cube[LAYER][LAYER][LAYER];

    public volatile float mXAngle;
    public volatile float mYAngle;

    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    private final float[] mNormalMatrix = new float[16];
    private final float[] mRotationMatrixX = new float[16];
    private final float[] mRotationMatrixY = new float[16];
    private final float[] mTempMatrix = new float[16];

    private float[] mEye = {-3, 2, 6};
    private float[] mUp = {0, 1, 0};

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        GLES20.glEnable(GL10.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GL10.GL_LEQUAL);

        for (int i = 0; i<LAYER; i++) {
            for (int j=0; j<LAYER; j++) {
                for (int k=0; k<LAYER; k++) {
                    mCube[i][j][k] = new Cube();
                }
            }
        }
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        Matrix.setLookAtM(mViewMatrix, 0, mEye[0], mEye[1], mEye[2], 0f, 0f, 0f, mUp[0], mUp[1], mUp[2]);
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        Matrix.setRotateM(mRotationMatrixX, 0, mXAngle, 0, 1.0f, 0f);
        Matrix.setRotateM(mRotationMatrixY, 0, mYAngle, 1.0f, 0, 0);

        // Rotation
        Matrix.multiplyMM(mTempMatrix,  0,  mRotationMatrixX, 0, mRotationMatrixY,  0);

        Matrix.multiplyMM(mMVPMatrix, 0, mMVPMatrix, 0, mTempMatrix, 0);
        Matrix.multiplyMM(mViewMatrix, 0, mViewMatrix, 0, mTempMatrix, 0);

        //Normal matrix = transpose(inv(modelview))
        Matrix.invertM(mTempMatrix, 0, mViewMatrix, 0);
        Matrix.transposeM(mNormalMatrix, 0, mTempMatrix, 0);

        drawCube();
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {

        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;

        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 1, 20);
    }

    // compile GLSL code prior to using it in OpenGL ES environment
    public static int loadShader(int type, String shaderCode) {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    private void drawCube(){
        float[] finalMVPMatrix = new float[16];
        float tx, ty, tz;
        float offset = 1.05f;

        tx = -offset;
        for (int i = 0; i<LAYER; i++) {
            ty = -offset;
            for (int j=0; j<LAYER; j++) {
                tz = -offset;
                for (int k=0; k<LAYER; k++) {
                    Matrix.translateM(finalMVPMatrix, 0, mMVPMatrix, 0, tx, ty, tz);
                    mCube[i][j][k].draw(finalMVPMatrix, mNormalMatrix, mViewMatrix);
                    tz += offset;
                }
                ty += offset;
            }
            tx += offset;
        }
    }
}
