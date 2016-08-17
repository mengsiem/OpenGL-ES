package com.example.mandy_hsieh.demorubikscube;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Mandy_Hsieh on 2016/5/24.
 */
public class Cube {
    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
                    "uniform mat4 uMVMatrix;"+
                    "uniform mat4 uNormalMat;"+
                    "attribute vec4 vPosition;" +
                    "attribute vec4 vColor;" +
                    "attribute vec3 vNormal;"+
                    "varying vec4 varyingColor;" +
                    "varying vec3 varyingPos;"+
                    "varying vec3 varyingNormal;"+
                    "void main() {" +
                    "    varyingColor = vColor;" +
                    "    vec4 t = uNormalMat*vec4(vNormal, 0.0);"+
                    "    varyingNormal.xyz = t.xyz;"+
                    "     t = uMVMatrix*vPosition;"+
                    "    varyingPos.xyz = t.xyz;"+
                    "    gl_Position = uMVPMatrix * vPosition;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec4 varyingColor; "+
                    "varying vec3 varyingNormal;" +
                    "varying vec3 varyingPos;" +
                    "uniform vec3 lightDir; " +
                    "void main() {" +
                    "   float kd = 0.9, ks = 0.9; " +
                    "   vec4 light = vec4(1.0, 1.0, 1.0, 1.0); " +
                    "   vec3 Nn = normalize(varyingNormal); " +
                    "   vec3 Ln = normalize(lightDir); " +
                    "   vec4 diffuse = kd* light * max(dot(Nn, Ln), 0.0);" +
                    "  gl_FragColor = varyingColor*diffuse;" +
                    //"   gl_FragColor = varyingColor*diffuse + specular; " +
                    "}";

    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;
    private ShortBuffer drawListBuffer;
    private FloatBuffer normalBuffer;

    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle, mNormalHandle;
    private int mMVPMatrixHandle, mNormalMatHandle;

    private static float CUBE_W = 0.5f;
    static final int COORDS_PER_VERTEX = 3;

    static float cubeCoords[] = {
            CUBE_W, CUBE_W, CUBE_W, -CUBE_W, CUBE_W, CUBE_W, -CUBE_W, -CUBE_W, CUBE_W, // v0-v1-v2 (front)
            -CUBE_W, -CUBE_W, CUBE_W, CUBE_W, -CUBE_W, CUBE_W, CUBE_W, CUBE_W, CUBE_W, // v2-v3-v0
            CUBE_W, CUBE_W, CUBE_W, CUBE_W, -CUBE_W, CUBE_W, CUBE_W, -CUBE_W, -CUBE_W, // v0-v3-v4 (right)
            CUBE_W, -CUBE_W, -CUBE_W, CUBE_W, CUBE_W, -CUBE_W, CUBE_W, CUBE_W, CUBE_W, // v4-v5-v0
            CUBE_W, CUBE_W, CUBE_W, CUBE_W, CUBE_W, -CUBE_W, -CUBE_W, CUBE_W, -CUBE_W, // v0-v5-v6 (top)
            -CUBE_W, CUBE_W, -CUBE_W, -CUBE_W, CUBE_W, CUBE_W, CUBE_W, CUBE_W, CUBE_W, // v6-v1-v0
            -CUBE_W, CUBE_W, CUBE_W, -CUBE_W, CUBE_W, -CUBE_W, -CUBE_W, -CUBE_W, -CUBE_W, // v1-v6-v7 (left)
            -CUBE_W, -CUBE_W, -CUBE_W, -CUBE_W, -CUBE_W, CUBE_W, -CUBE_W, CUBE_W, CUBE_W, // v7-v2-v1
            -CUBE_W, -CUBE_W, -CUBE_W, CUBE_W, -CUBE_W, -CUBE_W, CUBE_W, -CUBE_W, CUBE_W, // v7-v4-v3 (bottom)
            CUBE_W, -CUBE_W, CUBE_W, -CUBE_W, -CUBE_W, CUBE_W, -CUBE_W, -CUBE_W, -CUBE_W, // v3-v2-v7
            CUBE_W, -CUBE_W, -CUBE_W, -CUBE_W, -CUBE_W, -CUBE_W, -CUBE_W, CUBE_W, -CUBE_W, // v4-v7-v6 (back)
            -CUBE_W, CUBE_W, -CUBE_W, CUBE_W, CUBE_W, -CUBE_W, CUBE_W, -CUBE_W, -CUBE_W }; // v6-v5-v4

    // normal array
    static float cubeNormals[]  = {
            0, 0, 1,   0, 0, 1,   0, 0, 1,      // v0-v1-v2 (front)
            0, 0, 1,   0, 0, 1,   0, 0, 1,      // v2-v3-v0
            1, 0, 0,   1, 0, 0,   1, 0, 0,      // v0-v3-v4 (right)
            1, 0, 0,   1, 0, 0,   1, 0, 0,      // v4-v5-v0
            0, 1, 0,   0, 1, 0,   0, 1, 0,      // v0-v5-v6 (top)
            0, 1, 0,   0, 1, 0,   0, 1, 0,      // v6-v1-v0
            -1, 0, 0,  -1, 0, 0,  -1, 0, 0,      // v1-v6-v7 (left)
            -1, 0, 0,  -1, 0, 0,  -1, 0, 0,      // v7-v2-v1
            0,-1, 0,   0,-1, 0,   0,-1, 0,      // v7-v4-v3 (bottom)
            0,-1, 0,   0,-1, 0,   0,-1, 0,      // v3-v2-v7
            0, 0,-1,   0, 0,-1,   0, 0,-1,      // v4-v7-v6 (back)
            0, 0,-1,   0, 0,-1,   0, 0,-1 };    // v6-v5-v4

    // color array
    static final int COLORS_PER_VERTEX = 3;
    static float cubeColors[];
    static float colorFront[] = {1, 0, 0};
    static float colorBack[] = {1, .5f, 0};
    static float colorLeft[] = {0, 1, 0};
    static float colorRight[] = {0, 0, 1};
    static float colorTop[] = {1, 1, 1};
    static float colorBottom[] = {1, 1, 0};

    private short drawOrder[] = { 0, 1, 2, 0, 2, 3 };      // order to draw vertices

    private final int vertexCount = cubeCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4;       // bytes per vertex

    // set the light direction in the eye coordinate;
    float lightDir[] = {0.0f, 1.0f, 8.0f};

    public static int checkShaderError(int shader) {
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == 0) {
            GLES20.glDeleteShader(shader);
            return 1;
        }
        return 0;
    }

    public Cube() {
        ByteBuffer bb = ByteBuffer.allocateDirect(cubeCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());

        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(cubeCoords);
        vertexBuffer.position(0);

        cubeColors = new float[vertexCount * COLORS_PER_VERTEX];
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 3; j++) {
                int base = i * COLORS_PER_VERTEX + j;
                cubeColors[base] = colorFront[j];
                cubeColors[base + COLORS_PER_VERTEX * 6] = colorRight[j];
                cubeColors[base + COLORS_PER_VERTEX * 6 * 2] = colorTop[j];
                cubeColors[base + COLORS_PER_VERTEX * 6 * 3] = colorLeft[j];
                cubeColors[base + COLORS_PER_VERTEX * 6 * 4] = colorBottom[j];
                cubeColors[base + COLORS_PER_VERTEX * 6 * 5] = colorBack[j];
            }
        }

        ByteBuffer cb = ByteBuffer.allocateDirect(cubeColors.length * 4);
        cb.order(ByteOrder.nativeOrder());
        colorBuffer = cb.asFloatBuffer();
        colorBuffer.put(cubeColors);
        colorBuffer.position(0);

        // normal buffer;
        ByteBuffer bb3 = ByteBuffer.allocateDirect(cubeNormals.length * 4);
        bb3.order(ByteOrder.nativeOrder());
        normalBuffer = bb3.asFloatBuffer();
        normalBuffer.put(cubeNormals);
        normalBuffer.position(0);

        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        checkShaderError(vertexShader);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        checkShaderError(fragmentShader);
        mProgram = GLES20.glCreateProgram();                          // create empty OpenGL ES program
        GLES20.glAttachShader(mProgram,  vertexShader);        // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader);       // add the fragment shader to program
        GLES20.glLinkProgram(mProgram);                                      // creates OpenGL ES program executables
    }

    public void draw(float[] mvpMatrix, float[] normalMat, float[] mvMat) {    // pass in the calculated transformation matrix
        GLES20.glUseProgram(mProgram);
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member
        //mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        // Set color for drawing the triangle
        //GLES20.glUniform4fv(mColorHandle, 1, color, 0);
        int light = GLES20.glGetUniformLocation(mProgram, "lightDir");
        GLES20.glUniform3fv(light, 1, lightDir, 0);

        mColorHandle = GLES20.glGetAttribLocation(mProgram, "vColor");
        GLES20.glEnableVertexAttribArray(mColorHandle);
        GLES20.glVertexAttribPointer(mColorHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, colorBuffer);

        // now deal with normals
        mNormalHandle = GLES20.glGetAttribLocation(mProgram, "vNormal");
        GLES20.glEnableVertexAttribArray(mNormalHandle);
        // Prepare the normal data
        GLES20.glVertexAttribPointer(mNormalHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, normalBuffer);


        // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        mNormalMatHandle = GLES20.glGetUniformLocation(mProgram, "uNormalMat");

        int MVMatHandle = GLES20.glGetUniformLocation(mProgram, "uMVMatrix");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        GLES20.glUniformMatrix4fv(mNormalMatHandle, 1, false, normalMat, 0);
        GLES20.glUniformMatrix4fv(MVMatHandle, 1, false, mvMat, 0);

        // Draw the triangle
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mColorHandle);
        GLES20.glDisableVertexAttribArray(mNormalHandle);

    }

    public float getCubeW () {
        return CUBE_W;
    }
}
