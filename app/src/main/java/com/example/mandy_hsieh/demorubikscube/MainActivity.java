package com.example.mandy_hsieh.demorubikscube;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

public class MainActivity extends Activity {

    class MyGLSurfaceView extends GLSurfaceView {

        private final MyGLRenderer mRenderer;

        private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
        private float mPreviousX;
        private float mPreviousY;

        public MyGLSurfaceView(Context context){
            super(context);

            setEGLContextClientVersion(2);

            mRenderer = new MyGLRenderer();
            setRenderer(mRenderer);
            setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        }

        @Override
        public boolean onTouchEvent(MotionEvent e) {
            float x = e.getX();
            float y = e.getY();

            switch (e.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    float dx = x - mPreviousX;
                    float dy = y - mPreviousY;
                    if (y > getHeight() / 2) {
                        dx = dx * -1 ;
                    }
                    if (x < getWidth() / 2) {
                        dy = dy * -1 ;
                    }

                    if(Math.abs(dx) > Math.abs(dy)) {
                        mRenderer.mXAngle += dx * TOUCH_SCALE_FACTOR;
                    }
                    else {
                        mRenderer.mYAngle += dy * TOUCH_SCALE_FACTOR;
                    }

                    requestRender();
            }
            mPreviousX = x;
            mPreviousY = y;
            return true;
        }
    }

    private GLSurfaceView mGLView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        }

        mGLView = new MyGLSurfaceView(this);
        setContentView(mGLView);
    }
}
