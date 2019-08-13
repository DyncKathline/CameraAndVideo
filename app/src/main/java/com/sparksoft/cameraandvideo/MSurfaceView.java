package com.sparksoft.cameraandvideo;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class MSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Camera.PreviewCallback {

    public Camera mCamera;
    public MSurfaceView(Context context) {
        super(context);
        init();
    }

    public MSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    protected MSurfaceView init() {
        mCamera = Camera.open();
        mCamera.setDisplayOrientation(90);
        SurfaceHolder mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.setKeepScreenOn(true);
        return this;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        Config.logd("MSurface setVisibility ： " + visibility);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Config.logd("MSurface  created.");
        if(mCamera == null) {
            mCamera = Camera.open();
            mCamera.setDisplayOrientation(90);
        }
        try {
            mCamera.setPreviewCallback(this);
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        Config.logd("MSurface onAttachedToWindow");
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Config.logd("MSurface  surfaceChanged ： " + width + " " + height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Config.logd("MSurface  surfaceDestroyed：");
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        Config.logd("MSurface  onPreviewFrame ： ");
    }
}
